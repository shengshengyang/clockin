package com.example.clockin.controller;

import com.example.clockin.dto.ClockInEvent;
import com.example.clockin.dto.ClockInResult;
import com.example.clockin.model.AttendanceRecord;
import com.example.clockin.model.Shift;
import com.example.clockin.model.ShiftPeriod;
import com.example.clockin.model.User;
import com.example.clockin.repo.AttendanceRecordRepository;
import com.example.clockin.repo.UserRepository;
import com.example.clockin.service.AttendanceService;
import com.example.clockin.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.requestreply.RequestReplyMessageFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/attendance")
public class AttendanceController {

private final AttendanceService attendanceService;
private final AttendanceRecordRepository attendanceRecordRepository;
private final UserRepository userRepository;
private final UserUtil util;
private final KafkaTemplate<String, ClockInEvent> kafkaTemplate;
private final ReplyingKafkaTemplate<String, ClockInEvent, ClockInResult> replyingKafkaTemplate;

public AttendanceController(AttendanceService attendanceService,
                            AttendanceRecordRepository attendanceRecordRepository,
                            UserRepository userRepository,
                            UserUtil util,
                            KafkaTemplate<String, ClockInEvent> kafkaTemplate,
                            ReplyingKafkaTemplate<String, ClockInEvent, ClockInResult> replyingKafkaTemplate) {
    this.attendanceService = attendanceService;
    this.attendanceRecordRepository = attendanceRecordRepository;
    this.userRepository = userRepository;
    this.util = util;
    this.kafkaTemplate = kafkaTemplate;
    this.replyingKafkaTemplate = replyingKafkaTemplate;
}

    @GetMapping("/clock-in")
    public String clockInPage(Model model, Principal principal) {
        User user = util.getCurrentUser();
        // 獲取公司的經緯度，傳遞給前端地圖顯示
        double companyLat = attendanceService.getCompanyLatitude();
        double companyLng = attendanceService.getCompanyLongitude();
        model.addAttribute("companyLat", companyLat);
        model.addAttribute("companyLng", companyLng);
        model.addAttribute("user", user);

        return "clock-in";
    }


    @PostMapping("/clock-in")
    @ResponseBody
    public String clockIn(@RequestBody Map<String, Double> location, Principal principal) throws Exception {
        String username = principal.getName();
        double latitude = location.get("latitude");
        double longitude = location.get("longitude");

        // 創建打卡事件
        ClockInEvent event = new ClockInEvent(username, latitude, longitude);

        // 創建消息並設置回覆主題
        org.springframework.messaging.Message<ClockInEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, "clock-in-request-topic")
                .setHeader(KafkaHeaders.REPLY_TOPIC, "clock-in-response-topic")
                .build();

        // 發送請求並等待回覆
        RequestReplyMessageFuture<String, ClockInEvent> future = replyingKafkaTemplate.sendAndReceive(message);

        // 設置超時時間，避免無限等待
        org.springframework.messaging.Message<ClockInResult> response = (org.springframework.messaging.Message<ClockInResult>) future.get(10, TimeUnit.SECONDS);
        ClockInResult result = response.getPayload();

        return result.getMessage();
    }

    // 獲取所有考勤記錄
    @GetMapping("/records")
    @ResponseBody
    public Map<String, Object> getAttendanceRecords(Principal principal) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        List<AttendanceRecord> records;
        if (isAdmin) {
            // 如果是管理員，獲取所有記錄
            records = attendanceRecordRepository.findAll();
        } else {
            // 如果是普通用戶，僅獲取自己的記錄
            String username = principal.getName();
            User user = userRepository.findByUsername(username);
            records = attendanceRecordRepository.findByUserOrderByClockInTimeDesc(user);
        }

        // 將嵌套的 user 對象展開，格式化數據
        List<Map<String, Object>> formattedRecords = records.stream().map(record -> {
            Map<String, Object> formattedRecord = new HashMap<>();
            formattedRecord.put("id", record.getId());
            formattedRecord.put("username", record.getUser().getUsername());
            formattedRecord.put("clockInTime", record.getClockInTime().toString());

            // 計算狀態
            String status = calculateStatus(record);
            formattedRecord.put("status", status);

            return formattedRecord;
        }).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("page", 1);  // 假設只返回一頁
        response.put("total", 1); // 總頁數
        response.put("records", formattedRecords.size());  // 總記錄數
        response.put("data", formattedRecords);  // 返回的數據

        return response;
    }


    // 計算狀態的方法
    private String calculateStatus(AttendanceRecord attendanceRecord) {
        User user = attendanceRecord.getUser();
        Shift shift = user.getShift();

        if (shift == null || shift.getPeriods() == null || shift.getPeriods().isEmpty()) {
            return "無班別";
        }

        LocalTime clockInTime = attendanceRecord.getClockInTime().toLocalTime();

        // 遍歷 Shift 的每個 ShiftPeriod 來判斷狀態
        for (ShiftPeriod period : shift.getPeriods()) {
            LocalTime periodStartTime = period.getStartTime();
            LocalTime periodEndTime = period.getEndTime();

            // 檢查打卡時間是否在班別段的範圍內
            if (!clockInTime.isBefore(periodStartTime) && !clockInTime.isAfter(periodEndTime)) {
                // 準時判斷
                if (clockInTime.isBefore(periodStartTime.plusMinutes(period.getAllowedLateMinutes())) || clockInTime.equals(periodStartTime)) {
                    return "準時";
                }
                // 遲到判斷
                else if (clockInTime.isBefore(periodEndTime)) {
                    return "遲到";
                }
            }
        }

        // 若打卡時間不符合任何班別段則視為早退
        return "早退";
    }

    // 新增打卡紀錄的 API
    @PostMapping("/add")
    @ResponseBody
    public String addAttendanceRecord(@RequestParam("username") String username,
                                      @RequestParam("clockInTime") String clockInTime,
                                      @RequestParam("latitude") Double latitude,
                                      @RequestParam("longitude") Double longitude) {
        User user = userRepository.findByUsername(username);

        if (user != null) {
            AttendanceRecord attendanceRecord = new AttendanceRecord();
            attendanceRecord.setUser(user);
            attendanceRecord.setClockInTime(LocalDateTime.parse(clockInTime));
            attendanceRecordRepository.save(attendanceRecord);
            return "新增成功";
        } else {
            return "用戶未找到";
        }
    }

    // 更新打卡紀錄的 API
    @PostMapping("/update")
    @ResponseBody
    public String updateAttendanceRecord(@RequestParam("id") Integer id,
                                         @RequestParam("clockInTime") String clockInTime) {
        System.out.println("收到的打卡時間：" + clockInTime);  // 用於調試

        Optional<AttendanceRecord> recordOpt = attendanceRecordRepository.findById(id);

        if (recordOpt.isPresent()) {
            AttendanceRecord attendanceRecord = recordOpt.get();

            // 嘗試解析時間
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                LocalDateTime parsedTime = LocalDateTime.parse(clockInTime, formatter);

                attendanceRecord.setClockInTime(parsedTime); // 更新打卡時間
                attendanceRecordRepository.save(attendanceRecord);
                return "更新成功";
            } catch (DateTimeParseException e) {
                return "日期時間格式錯誤: " + clockInTime;
            }
        } else {
            return "記錄未找到";
        }
    }

    // 刪除打卡紀錄的 API
    @DeleteMapping("/delete")
    @ResponseBody
    public String deleteAttendanceRecord(@RequestParam("id") Integer id) {
        attendanceRecordRepository.deleteById(id);
        return "刪除成功";
    }
}
