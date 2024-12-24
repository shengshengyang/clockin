package com.example.clockin.service;

import com.example.clockin.dto.ClockInEvent;
import com.example.clockin.dto.ClockInResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClockInConsumer {

    @Autowired
    private AttendanceService attendanceService;

    @KafkaListener(
            topics = "clock-in-request-topic",
            groupId = "attendance-group",
            containerFactory = "kafkaListenerContainerFactory")
    @SendTo  // 默认发送到消息头中的 replyTo 主题
    public ClockInResult consume(ClockInEvent event) {
        String username = event.getUsername();
        double latitude = event.getLatitude();
        double longitude = event.getLongitude();

        boolean success = attendanceService.handleClockIn(username, latitude, longitude);

        if (success) {
            return new ClockInResult(true, "打卡成功");
        } else {
            return new ClockInResult(false, "打卡失败，您不在允许的范围内");
        }
    }
}