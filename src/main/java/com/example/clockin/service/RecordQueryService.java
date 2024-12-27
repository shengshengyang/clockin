package com.example.clockin.service;

import com.example.clockin.model.AttendanceRecord;
import com.example.clockin.model.User;
import com.example.clockin.repo.AttendanceRecordRepository;
import com.example.clockin.repo.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecordQueryService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final UserRepository userRepository;

    public RecordQueryService(AttendanceRecordRepository attendanceRecordRepository,
                              UserRepository userRepository) {
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.userRepository = userRepository;
    }

    /**
     * 提供給 Facade 呼叫，回傳多筆紀錄的資料結構
     * @param username 當前用戶 (若非 admin，即只可看自己的紀錄)
     * @param isAdmin 是否具有 ADMIN 權限
     * @return 多筆考勤紀錄 (經整理後)
     */
    public List<Map<String, Object>> getRecords(String username, boolean isAdmin) {
        List<AttendanceRecord> records;

        // 1. 查詢 DB
        if (isAdmin) {
            // 管理員 -> 查詢全部
            records = attendanceRecordRepository.findAll();
        } else {
            // 一般使用者 -> 只查詢自己的
            User user = userRepository.findByUsername(username);
            records = attendanceRecordRepository.findByUserOrderByClockInTimeDesc(user);
        }

        // 2. 格式化資料
        return records.stream().map(record -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", record.getId());
            map.put("username", record.getUser().getUsername());
            map.put("clockInTime", record.getClockInTime().toString());
            map.put("status", calculateStatus(record));
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 計算考勤狀態 (跟你的原始程式裡的邏輯一致)
     */
    private String calculateStatus(AttendanceRecord record) {
        if (record.getUser().getShift() == null
                || record.getUser().getShift().getPeriods() == null
                || record.getUser().getShift().getPeriods().isEmpty()) {
            return "無班別";
        }
        var shift = record.getUser().getShift();
        var clockInTime = record.getClockInTime().toLocalTime();

        // 遍歷時段，判斷遲到/準時/早退
        return shift.getPeriods().stream()
                .map(period -> {
                    if (!clockInTime.isBefore(period.getStartTime()) && !clockInTime.isAfter(period.getEndTime())) {
                        // 在該時段範圍內
                        LocalTime lateThreshold = period.getStartTime().plusMinutes(period.getAllowedLateMinutes());
                        if (clockInTime.isBefore(lateThreshold) || clockInTime.equals(period.getStartTime())) {
                            return "準時";
                        } else {
                            return "遲到";
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("早退");
    }
}
