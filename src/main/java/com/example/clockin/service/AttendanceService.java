package com.example.clockin.service;

import com.example.clockin.model.AttendanceRecord;
import com.example.clockin.model.CompanyLocation;
import com.example.clockin.model.User;
import com.example.clockin.repo.AttendanceRecordRepository;
import com.example.clockin.repo.CompanyLocationRepository;
import com.example.clockin.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AttendanceService {
    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CompanyLocationRepository companyLocationRepository;

    private final double MAX_DISTANCE = 200;


    public boolean handleClockIn(String username, double latitude, double longitude) {
        double companyLat = getCompanyLatitude();
        double companyLng = getCompanyLongitude();
        double distance = calculateDistance(latitude, longitude, companyLat, companyLng);

        if (distance <= MAX_DISTANCE) {
            // 保存打卡記錄
            User user = userRepository.findByUsername(username);
            AttendanceRecord record = new AttendanceRecord();
            record.setUser(user);
            record.setLatitude(latitude);
            record.setLongitude(longitude);
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

    private CompanyLocation getCompanyLocation() {
        return companyLocationRepository.findFirstByOrderByIdAsc();
    }
    public double getCompanyLongitude() {
        CompanyLocation companyLocation = getCompanyLocation();
        return companyLocation.getLongitude();
    }
    public double getCompanyLatitude() {
        CompanyLocation companyLocation = getCompanyLocation();
        return companyLocation.getLatitude();
    }
}

