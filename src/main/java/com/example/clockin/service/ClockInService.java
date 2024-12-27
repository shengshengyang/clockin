package com.example.clockin.service;

import com.example.clockin.dto.ClockInRequest;
import com.example.clockin.model.AttendanceRecord;
import com.example.clockin.model.User;
import com.example.clockin.repo.AttendanceRecordRepository;
import com.example.clockin.repo.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ClockInService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final UserRepository userRepository;

    public ClockInService(AttendanceRecordRepository attendanceRecordRepository,
                          UserRepository userRepository) {
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.userRepository = userRepository;
    }

    /**
     * 執行打卡邏輯
     * @param request 來自 Controller 傳入的打卡參數 (如 lat, lng)
     * @param username 當前使用者 (從 Principal 或 SecurityContextHolder 拿)
     */
    public String clockIn(ClockInRequest request, String username) {
        // 1. 找到用戶
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return "用戶不存在: " + username;
        }

        // 2. 建立打卡記錄 (存 DB)
        AttendanceRecord record = new AttendanceRecord();
        record.setUser(user);
        record.setClockInTime(LocalDateTime.now());
        attendanceRecordRepository.save(record);

        // 3. 回傳結果，可自訂訊息
        return "打卡成功!";
    }
}
