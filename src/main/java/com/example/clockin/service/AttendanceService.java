package com.example.clockin.service;

import com.example.clockin.dto.ClockInEvent;
import com.example.clockin.dto.ClockInResult;
import com.example.clockin.exception.ApiException;
import com.example.clockin.exception.SysCode;
import com.example.clockin.model.AttendanceRecord;
import com.example.clockin.model.Company;
import com.example.clockin.model.User;
import com.example.clockin.repo.AttendanceRecordRepository;
import com.example.clockin.repo.CompanyRepository;
import com.example.clockin.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyMessageFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class AttendanceService {
    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ReplyingKafkaTemplate<String, ClockInEvent, ClockInResult> replyingKafkaTemplate;

    Logger logger = LoggerFactory.getLogger(AttendanceService.class);


   public String processClockIn(ClockInEvent event) {
    try {
        // 1. 檢查用戶是否存在
        User user = userRepository.findByUsername(event.getUsername());
        if (user == null) {
            return "用戶不存在：" + event.getUsername();
        }
        logger.info("Clock-in attempt for user: {} at location: ({}, {})", event.getUsername(), event.getLatitude(), event.getLongitude());
        // 2. 創建消息並設置回覆主題
        org.springframework.messaging.Message<ClockInEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, "clock-in-request-topic")
                .setHeader(KafkaHeaders.REPLY_TOPIC, "clock-in-response-topic")
                .build();

        RequestReplyMessageFuture<String, ClockInEvent> future = replyingKafkaTemplate.sendAndReceive(message);
        Message<?> responseMessage = future.get(10, TimeUnit.SECONDS);

        if (responseMessage.getPayload() instanceof ClockInResult result) {
            logger.info("Clock-in successful for user: {}", event.getUsername());
            return result.getMessage();
        } else {
            logger.error("Unexpected response type for user: {}", event.getUsername());
            throw new ApiException(SysCode.UNEXPECTED_RESPONSE_TYPE, "Unexpected response type");
        }
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
        logger.error("Error processing clock-in for user: {}", event.getUsername(), e);
        throw new ApiException(SysCode.CLOCK_IN_FAILED, "Clock-in process failed", e);
    }
}


    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public boolean handleClockIn(String username, double latitude, double longitude) {
        double companyLat = getCompanyLatitude();
        double companyLng = getCompanyLongitude();
        double distance = calculateDistance(latitude, longitude, companyLat, companyLng);

        double maxDistance = companyRepository.findFirstByOrderByIdAsc().getRadius();

        if (distance <= maxDistance) {
            // 保存打卡記錄
            User user = userRepository.findByUsername(username);
            AttendanceRecord attendanceRecord = new AttendanceRecord();
            attendanceRecord.setUser(user);
            attendanceRecord.setClockInTime(LocalDateTime.now());
            attendanceRecordRepository.save(attendanceRecord);
            return true;
        } else {
            return false;
        }
    }

    // 計算兩點之間的距離（單位：米）
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; // 地球半徑，單位：米
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadius * c;
    }

    public List<AttendanceRecord> getAttendanceRecordsByUsername(String username) {
        User user = userRepository.findByUsername(username);
        return attendanceRecordRepository.findByUserOrderByClockInTimeDesc(user);
    }

    private Company getCompanyLocation() {
        return companyRepository.findFirstByOrderByIdAsc();
    }

    public double getCompanyLongitude() {
        Company company = getCompanyLocation();
        return company.getLongitude();
    }

    public double getCompanyLatitude() {
        Company company = getCompanyLocation();
        return company.getLatitude();
    }
}

