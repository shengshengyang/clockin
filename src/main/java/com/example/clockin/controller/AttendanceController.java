package com.example.clockin.controller;

import com.example.clockin.dto.ClockInRequest;
import com.example.clockin.model.AttendanceRecord;
import com.example.clockin.service.AttendanceService;
import com.example.clockin.util.DistanceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/attendance")
public class AttendanceController {

    @Autowired
    AttendanceService attendanceService;
    @GetMapping("/clock-in")
    public String clockInPage(Model model, Principal principal) {
        // 獲取公司的經緯度，傳遞給前端地圖顯示
        double companyLat = attendanceService.getCompanyLatitude();
        double companyLng = attendanceService.getCompanyLongitude();
        model.addAttribute("companyLat", companyLat);
        model.addAttribute("companyLng", companyLng);

        // 獲取當前用戶的打卡記錄
        String username = principal.getName();
        List<AttendanceRecord> records = attendanceService.getAttendanceRecordsByUsername(username);
        model.addAttribute("records", records);

        return "clock-in";
    }

    @PostMapping("/clock-in")
    @ResponseBody
    public String clockIn(@RequestBody Map<String, Double> location, Principal principal) {
        String username = principal.getName();
        double latitude = location.get("latitude");
        double longitude = location.get("longitude");

        boolean success = attendanceService.handleClockIn(username, latitude, longitude);

        if (success) {
            return "打卡成功";
        } else {
            return "打卡失敗，您不在允許的範圍內";
        }
    }
}