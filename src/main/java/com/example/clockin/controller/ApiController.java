package com.example.clockin.controller;

import com.example.clockin.dto.ClockInEvent;
import com.example.clockin.dto.ClockInResult;
import com.example.clockin.model.AttendanceRecord;
import com.example.clockin.model.Shift;
import com.example.clockin.model.ShiftPeriod;
import com.example.clockin.model.User;
import com.example.clockin.repo.AttendanceRecordRepository;
import com.example.clockin.repo.UserRepository;
import com.example.clockin.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ReplyingKafkaTemplate<String, ClockInEvent, ClockInResult> replyingKafkaTemplate;

    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 提供 /api/login 接口，
     * 傳入 username & password，若驗證成功，就回傳一組 JWT Token。
     */
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        // 1. 驗證使用者帳號與密碼
        //    這會由 Spring Security 裝置裡的 DaoAuthenticationProvider / UserDetailsService 等進行校驗
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        // 2. 若驗證成功（沒拋出異常），可從 userDetailsService 取得使用者資料
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // 3. 產生 JWT Token
        String jwt = jwtUtil.generateToken(userDetails.getUsername());

        // 4. 回傳給前端
        Map<String, String> response = new HashMap<>();
        response.put("token", jwt);
        return response;
    }

    /**
     * 打卡 (POST /api/attendance/clock-in)
     * 以 JSON 傳入 { "latitude": 25.123, "longitude": 121.456 }
     */
    @PostMapping("/clock-in")
    public String clockIn(@RequestBody Map<String, Double> location, Principal principal) throws Exception {
        // 取得當前使用者
        String username = principal.getName();
        double latitude = location.get("latitude");
        double longitude = location.get("longitude");

        // 創建打卡事件
        ClockInEvent event = new ClockInEvent(username, latitude, longitude);

        // 使用 Kafka Request-Reply 模式：指定要發往的 topic 與等待回覆的 topic
        Message<ClockInEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, "clock-in-request-topic")
                .setHeader(KafkaHeaders.REPLY_TOPIC, "clock-in-response-topic")
                .build();

        // 發送請求並等待回覆
        RequestReplyMessageFuture<String, ClockInEvent> future = replyingKafkaTemplate.sendAndReceive(message);

        // 設置超時時間，避免無限等待
        Message<ClockInResult> response = (Message<ClockInResult>) future.get(10, TimeUnit.SECONDS);
        ClockInResult result = response.getPayload();

        // 回傳 Kafka 返回的結果描述
        return result.getMessage();
    }

    /**
     * 查詢考勤記錄 (GET /api/attendance/records)
     * 角色為 ADMIN 時可查詢所有使用者記錄，否則只查詢自己
     */
    @GetMapping("/records")
    public Map<String, Object> getAttendanceRecords(Principal principal) {
        // 取得當前使用者的權限
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        // 根據角色決定查詢對象
        List<AttendanceRecord> records;
        if (isAdmin) {
            // ADMIN 可查詢全部
            records = attendanceRecordRepository.findAll();
        } else {
            // 一般使用者僅查詢自己的
            String username = principal.getName();
            User user = userRepository.findByUsername(username);
            records = attendanceRecordRepository.findByUserOrderByClockInTimeDesc(user);
        }

        // 格式化回傳資料
        List<Map<String, Object>> formattedRecords = records.stream().map(record -> {
            Map<String, Object> formattedRecord = new HashMap<>();
            formattedRecord.put("id", record.getId());
            formattedRecord.put("username", record.getUser().getUsername());
            formattedRecord.put("clockInTime", record.getClockInTime().toString());
            formattedRecord.put("status", calculateStatus(record));
            return formattedRecord;
        }).collect(Collectors.toList());

        // 模擬分頁資訊
        Map<String, Object> response = new HashMap<>();
        response.put("page", 1);             // 假設目前頁碼
        response.put("total", 1);            // 總頁數
        response.put("records", formattedRecords.size()); // 總筆數
        response.put("data", formattedRecords);           // 主要資料

        return response;
    }

    /**
     * 計算狀態（準時、遲到、早退、無班別）
     */
    private String calculateStatus(AttendanceRecord record) {
        User user = record.getUser();
        Shift shift = user.getShift();

        if (shift == null || shift.getPeriods() == null || shift.getPeriods().isEmpty()) {
            return "無班別";
        }

        LocalTime clockInTime = record.getClockInTime().toLocalTime();

        // 逐段判斷
        for (ShiftPeriod period : shift.getPeriods()) {
            LocalTime start = period.getStartTime();
            LocalTime end = period.getEndTime();
            // 檢查打卡時間是否在該班別時段內
            if (!clockInTime.isBefore(start) && !clockInTime.isAfter(end)) {
                // 如果在允許遲到前打卡
                if (clockInTime.isBefore(start.plusMinutes(period.getAllowedLateMinutes()))
                        || clockInTime.equals(start)) {
                    return "準時";
                } else {
                    return "遲到";
                }
            }
        }
        // 若全都不符合，即視為早退或不在工作時段
        return "早退";
    }
}
