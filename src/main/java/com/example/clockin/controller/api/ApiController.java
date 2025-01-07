package com.example.clockin.controller.api;

import com.example.clockin.dto.*;
import com.example.clockin.exception.ApiException;
import com.example.clockin.exception.SysCode;
import com.example.clockin.model.AttendanceRecord;
import com.example.clockin.model.Shift;
import com.example.clockin.model.ShiftPeriod;
import com.example.clockin.model.User;
import com.example.clockin.repo.AttendanceRecordRepository;
import com.example.clockin.repo.ShiftRepository;
import com.example.clockin.repo.UserRepository;
import com.example.clockin.service.AttendanceService;
import com.example.clockin.service.LoginService;
import com.example.clockin.service.MailService;
import com.example.clockin.util.UserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final UserRepository userRepository;
    private final UserUtil userUtil;
    private final LoginService loginService;
    private final ShiftRepository shiftRepository;
    private final Random random = new Random();
    private final MailService mailService;
    @Value("${front.end.url}")
    private String frontEndUrl;
    private final AttendanceService attendanceService;

    @Autowired
    public ApiController(AttendanceRecordRepository attendanceRecordRepository,
                         UserRepository userRepository, UserUtil userUtil, LoginService loginService, ShiftRepository shiftRepository, MailService mailService, AttendanceService attendanceService) {
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.userRepository = userRepository;
        this.userUtil = userUtil;
        this.loginService = loginService;
        this.shiftRepository = shiftRepository;
        this.mailService = mailService;
        this.attendanceService = attendanceService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest loginRequest) {
        Map<String, String> response = loginService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    // find user by jwttoken
    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getUser(@RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        User user = userUtil.getCurrentUser(token).orElseThrow(() -> new ApiException(SysCode.USER_NOT_FOUND));
        response.put("user", userUtil.convertToDTO(user));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/user")
    public ResponseEntity<Map<String, Object>> updateUser(@RequestHeader("Authorization") String token,
                                                          @RequestBody UserDTO userDTO) {
        User user = userUtil.getCurrentUser(token).orElseThrow(() -> new ApiException(SysCode.USER_NOT_FOUND));

        // Update user fields from UserDTO
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setName(userDTO.getUsername()); // Assuming name is the same as username
        if (userDTO.getShiftId() != null) {
            Shift shift = shiftRepository.findById(userDTO.getShiftId())
                    .orElseThrow(() -> new ApiException(SysCode.SHIFT_NOT_FOUND));
            user.setShift(shift);
        }

        try {
            userRepository.save(user);
        } catch (Exception e) {
            throw new ApiException(SysCode.UPDATE_FAILED, "Failed to update user", e);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("user", userUtil.convertToDTO(user));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/clock-in")
    public String clockIn(@RequestBody Map<String, Double> location, Principal principal) {
        String username = principal.getName();
        double latitude = location.get("latitude");
        double longitude = location.get("longitude");

        ClockInEvent event = new ClockInEvent(username, latitude, longitude);

        return  attendanceService.processClockIn(event);
    }

    @GetMapping("/records")
    public ResponseEntity<Map<String, Object>> getAttendanceRecords(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Principal principal
    ) {
        // 1. 解析使用者權限
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        logger.info("Fetching attendance records for user: {}", principal.getName());

        // 2. 將 startDate, endDate 字串轉換為 LocalDateTime
        // 如果沒傳值，就預設抓所有資料；若需要更複雜的預設值，可自行調整
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        try {
            if (startDate != null && !startDate.isEmpty()) {
                // 將日期字串 "2023-01-01" 轉為當日 00:00:00
                startDateTime = LocalDate.parse(startDate).atStartOfDay();
            }
            if (endDate != null && !endDate.isEmpty()) {
                // 將日期字串 "2023-01-02" 轉為同日 23:59:59
                LocalDate parsedEnd = LocalDate.parse(endDate);
                endDateTime = parsedEnd.atTime(LocalTime.of(23, 59, 59));
            }
        } catch (DateTimeParseException e) {
            logger.error("Date parse error: {}", e.getMessage());
            // 也可以拋錯給前端知悉
        }

        // 3. 依照是否為管理員，帶入不同的查詢條件
        List<AttendanceRecord> records;
        if (isAdmin) {
            if (startDateTime != null && endDateTime != null) {
                // 管理員查詢所有使用者，並且時間範圍限定
                records = attendanceRecordRepository.findAllByClockInTimeBetween(startDateTime, endDateTime);
            } else {
                // 沒有傳遞時間，就查全部
                records = attendanceRecordRepository.findAll();
            }
        } else {
            // 一般使用者
            String username = principal.getName();
            User user = userRepository.findByUsername(username);
            if (startDateTime != null && endDateTime != null) {
                records = attendanceRecordRepository.findByUserAndClockInTimeBetweenOrderByClockInTimeDesc(
                        user, startDateTime, endDateTime
                );
            } else {
                records = attendanceRecordRepository.findByUserOrderByClockInTimeDesc(user);
            }
        }

        // 4. 格式化查詢結果
        List<Map<String, Object>> formattedRecords = records.stream().map(ar -> {
            Map<String, Object> fm = new HashMap<>();
            fm.put("id", ar.getId());
            fm.put("username", ar.getUser().getUsername());
            fm.put("clockInTime", ar.getClockInTime().toString());
            fm.put("status", calculateStatus(ar));
            return fm;
        }).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("page", 1);
        response.put("total", 1);
        response.put("records", formattedRecords.size());
        response.put("data", formattedRecords);

        logger.info("Attendance records fetched for user: {}", principal.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        if (email == null || email.isEmpty()) {
            throw new ApiException(SysCode.MISSING_PARAMETER, "Email parameter is missing", null);
        }
        logger.info("Forgot password request for email: {}", email);

        User user = userRepository.findByEmail(email);
        if (user == null) {
            logger.error("User not found for email: {}", email);
            throw new ApiException(SysCode.USER_NOT_FOUND, "User not found");
        }

        // Generate a reset code
        String resetCode = generateResetCode();

        // Send email with password reset code
        sendResetEmail(email, resetCode);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Password reset code sent to email");
        return ResponseEntity.ok(response);
    }

    private String generateResetCode() {
        // Generate a 6-digit reset code
        return String.format("%06d", random.nextInt(999999));
    }

    private void sendResetEmail(String email, String resetCode) {
        String resetPasswordLink = frontEndUrl + "/reset-password?email=" + email + "&code=" + resetCode;
        Collection<String> receivers = Collections.singletonList(email);
        mailService.sendEmail(EmailType.PASSWORD_RESET, receivers, resetPasswordLink);
    }

    private String calculateStatus(AttendanceRecord ar) {
        User user = ar.getUser();
        Shift shift = user.getShift();
        if (shift == null || shift.getPeriods() == null || shift.getPeriods().isEmpty()) {
            return "無班別";
        }

        LocalTime clockInTime = ar.getClockInTime().toLocalTime();
        for (ShiftPeriod period : shift.getPeriods()) {
            LocalTime start = period.getStartTime();
            LocalTime end = period.getEndTime();
            if (!clockInTime.isBefore(start) && !clockInTime.isAfter(end)) {
                if (clockInTime.isBefore(start.plusMinutes(period.getAllowedLateMinutes()))
                        || clockInTime.equals(start)) {
                    return "準時";
                } else {
                    return "遲到";
                }
            }
        }
        return "早退";
    }


}
