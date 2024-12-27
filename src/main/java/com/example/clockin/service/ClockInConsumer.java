package com.example.clockin.service;

import com.example.clockin.dto.ClockInEvent;
import com.example.clockin.dto.ClockInResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

@Service
public class ClockInConsumer {

    private final AttendanceService attendanceService;

    @Autowired
    public ClockInConsumer(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @KafkaListener(
            topics = "clock-in-request-topic",
            groupId = "attendance-group",
            containerFactory = "kafkaListenerContainerFactory")
    @SendTo
    public ClockInResult consume(ClockInEvent event) {
        String username = event.getUsername();
        double latitude = event.getLatitude();
        double longitude = event.getLongitude();

        boolean success = attendanceService.handleClockIn(username, latitude, longitude);

        if (success) {
            return new ClockInResult(true, "打卡成功");
        } else {
            return new ClockInResult(false, "打卡失敗，您不在允許的範圍內");
        }
    }
}
