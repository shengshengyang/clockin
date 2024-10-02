package com.example.clockin.controller;

import com.example.clockin.dto.ClockInRequest;
import com.example.clockin.model.AttendanceRecord;
import com.example.clockin.model.User;
import com.example.clockin.repo.AttendanceRecordRepository;
import com.example.clockin.repo.UserRepository;
import com.example.clockin.service.AttendanceService;
import com.example.clockin.util.DistanceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/attendance")
public class AttendanceController {

    @Autowired
    AttendanceService attendanceService;

    @Autowired
    AttendanceRecordRepository attendanceRecordRepository;

    @Autowired
    UserRepository userRepository;


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

    @GetMapping("/records")
    @ResponseBody
    public Map<String, Object> getAttendanceRecords() {
        List<AttendanceRecord> records = attendanceRecordRepository.findAll();

        // 將嵌套的 user 對象展開，格式化數據
        List<Map<String, Object>> formattedRecords = records.stream().map(record -> {
            Map<String, Object> formattedRecord = new HashMap<>();
            formattedRecord.put("id", record.getId());
            formattedRecord.put("username", record.getUser().getUsername());
            formattedRecord.put("clockInTime", record.getClockInTime().toString());
            formattedRecord.put("latitude", record.getLatitude());
            formattedRecord.put("longitude", record.getLongitude());
            return formattedRecord;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("page", 1);  // 假設只返回一頁
        response.put("total", 1); // 總頁數
        response.put("records", formattedRecords.size());  // 總記錄數
        response.put("data", formattedRecords);  // 返回的數據

        return response;
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
            AttendanceRecord record = new AttendanceRecord();
            record.setUser(user);
            record.setClockInTime(LocalDateTime.parse(clockInTime));
            record.setLatitude(latitude);
            record.setLongitude(longitude);
            attendanceRecordRepository.save(record);
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
        Optional<AttendanceRecord> recordOpt = attendanceRecordRepository.findById(id);

        if (recordOpt.isPresent()) {
            AttendanceRecord record = recordOpt.get();
            record.setClockInTime(LocalDateTime.parse(clockInTime)); // 更新打卡時間
            attendanceRecordRepository.save(record);
            return "更新成功";
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