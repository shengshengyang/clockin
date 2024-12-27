package com.example.clockin.controller;

import com.example.clockin.dto.ClockInEvent;
import com.example.clockin.dto.ClockInResult;
import com.example.clockin.dto.UserDTO;
import com.example.clockin.exception.ApiException;
import com.example.clockin.exception.SysCode;
import com.example.clockin.model.AttendanceRecord;
import com.example.clockin.model.Shift;
import com.example.clockin.model.ShiftPeriod;
import com.example.clockin.model.User;
import com.example.clockin.repo.AttendanceRecordRepository;
import com.example.clockin.repo.UserRepository;
import com.example.clockin.util.JwtUtil;
import com.example.clockin.util.UserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyMessageFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/api")
public class ApiController {

    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final ReplyingKafkaTemplate<String, ClockInEvent, ClockInResult> replyingKafkaTemplate;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final UserRepository userRepository;
    private final UserUtil userUtil;

    @Autowired
    public ApiController(AuthenticationManager authenticationManager,
                         UserDetailsService userDetailsService,
                         JwtUtil jwtUtil,
                         ReplyingKafkaTemplate<String, ClockInEvent, ClockInResult> replyingKafkaTemplate,
                         AttendanceRecordRepository attendanceRecordRepository,
                         UserRepository userRepository, UserUtil userUtil) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.replyingKafkaTemplate = replyingKafkaTemplate;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.userRepository = userRepository;
        this.userUtil = userUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        logger.info("Login attempt for user: {}", username);

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String jwt = jwtUtil.generateToken(userDetails.getUsername());

        Map<String, String> response = new HashMap<>();
        response.put("username", userDetails.getUsername());
        response.put("roles", userDetails.getAuthorities().toString());
        response.put("token", jwt);

        logger.info("Login successful for user: {}", username);

        return ResponseEntity.ok(response);
    }

    // find user by jwttoken
    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getUser(@RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        response.put("user", userUtil.getCurrentUser(token).orElseThrow(() -> new ApiException(SysCode.USER_NOT_FOUND)));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/clock-in")
    public String clockIn(@RequestBody Map<String, Double> location, Principal principal) throws ExecutionException, InterruptedException, TimeoutException {
        String username = principal.getName();
        double latitude = location.get("latitude");
        double longitude = location.get("longitude");

        logger.info("Clock-in attempt for user: {} at location: ({}, {})", username, latitude, longitude);

        ClockInEvent event = new ClockInEvent(username, latitude, longitude);

        Message<ClockInEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, "clock-in-request-topic")
                .setHeader(KafkaHeaders.REPLY_TOPIC, "clock-in-response-topic")
                .build();


        RequestReplyMessageFuture<String, ClockInEvent> future = replyingKafkaTemplate.sendAndReceive(message);
        Message<?> responseMessage = future.get(10, TimeUnit.SECONDS);

        if (responseMessage.getPayload() instanceof ClockInResult result) {
            logger.info("Clock-in successful for user: {}", username);
            return result.getMessage();
        } else {
            logger.error("Unexpected response type for user: {}", username);
            throw new ApiException(SysCode.UNEXPECTED_RESPONSE_TYPE, "Unexpected response type");
        }

    }

    @GetMapping("/records")
    public ResponseEntity<Map<String, Object>> getAttendanceRecords(Principal principal) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        logger.info("Fetching attendance records for user: {}", principal.getName());

        List<AttendanceRecord> records;
        if (isAdmin) {
            records = attendanceRecordRepository.findAll();
        } else {
            String username = principal.getName();
            User user = userRepository.findByUsername(username);
            records = attendanceRecordRepository.findByUserOrderByClockInTimeDesc(user);
        }

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

        return ResponseEntity.ok().body(response);
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
