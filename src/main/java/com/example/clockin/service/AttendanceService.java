package com.example.clockin.service;

import com.example.clockin.dto.ClockInEvent;
import com.example.clockin.model.AttendanceRecord;
import com.example.clockin.model.Company;
import com.example.clockin.model.User;
import com.example.clockin.repo.AttendanceRecordRepository;
import com.example.clockin.repo.CompanyRepository;
import com.example.clockin.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AttendanceService {
    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CompanyRepository companyRepository;

    /**
     * 由 Facade 呼叫，用來處理打卡流程。
     * @param event 建立好的打卡事件 (包含 username, lat, lng)
     * @return 回傳顯示給前端的訊息
     */
    public String processClockIn(ClockInEvent event) throws Exception {
        // 1. 檢查用戶是否存在
        User user = userRepository.findByUsername(event.getUsername());
        if (user == null) {
            return "用戶不存在：" + event.getUsername();
        }

        // 2. 建立一筆新的考勤記錄
        AttendanceRecord record = new AttendanceRecord();
        record.setUser(user);
        record.setClockInTime(LocalDateTime.now()); // 以當前時間做打卡時間

        // 3. 寫入資料庫
        attendanceRecordRepository.save(record);

        // 4. 可能做更多事情 (例如發送通知、寫日誌、整合 Kafka...)
        //    若要做 Kafka Request-Reply，可在這裡加上非同步處理邏輯

        // 5. 回傳成功訊息
        return "打卡成功！";
    }


    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public boolean handleClockIn(String username, double latitude, double longitude) {
        double companyLat = getCompanyLatitude();
        double companyLng = getCompanyLongitude();
        double distance = calculateDistance(latitude, longitude, companyLat, companyLng);

        double MAX_DISTANCE = 200;
        if (distance <= MAX_DISTANCE) {
            // 保存打卡記錄
            User user = userRepository.findByUsername(username);
            AttendanceRecord record = new AttendanceRecord();
            record.setUser(user);
            record.setClockInTime(LocalDateTime.now());
            attendanceRecordRepository.save(record);
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

