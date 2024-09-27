package com.example.clockin.repo;

import com.example.clockin.model.AttendanceRecord;
import com.example.clockin.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Integer> {
    List<AttendanceRecord> findByUserOrderByClockInTimeDesc(User user);
}