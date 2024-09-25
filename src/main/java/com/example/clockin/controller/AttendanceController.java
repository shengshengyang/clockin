package com.example.clockin.controller;

import com.example.clockin.dto.ClockInRequest;
import com.example.clockin.util.DistanceUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@RequestBody ClockInRequest request) {
        double userLat = request.getLatitude();
        double userLng = request.getLongitude();

        if (DistanceUtil.isWithin200Meters(userLat, userLng)) {
            // 保存打卡記錄
            return ResponseEntity.ok("打卡成功");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("您不在允許的打卡範圍內");
        }
    }

    // 其他方法...
}