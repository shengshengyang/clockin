package com.example.clockin.service;

import com.example.clockin.model.AttendanceRecord;
import com.example.clockin.model.CompanyLocation;
import com.example.clockin.model.User;
import com.example.clockin.repo.AttendanceRecordRepository;
import com.example.clockin.repo.CompanyLocationRepository;
import com.example.clockin.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttendanceService {
    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CompanyLocationRepository companyLocationRepository;

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

