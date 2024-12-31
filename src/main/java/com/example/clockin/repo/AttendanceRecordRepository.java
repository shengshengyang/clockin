package com.example.clockin.repo;

import com.example.clockin.model.AttendanceRecord;
import com.example.clockin.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Integer> {
    List<AttendanceRecord> findByUserOrderByClockInTimeDesc(User user);

    List<AttendanceRecord> findByUser(User user);

    //findAllByClockInTimeBetween
    List<AttendanceRecord> findAllByClockInTimeBetween(LocalDateTime start, LocalDateTime end);

    //findByUserAndClockInTimeBetweenOrderByClockInTimeDesc
    List<AttendanceRecord> findByUserAndClockInTimeBetweenOrderByClockInTimeDesc(User user, LocalDateTime start, LocalDateTime end);
}
