package com.example.clockin.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_records")
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY) // 延遲加載
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "clock_in_time", nullable = false)
    private LocalDateTime clockInTime;

    // 新增一個字段來存儲狀態（不存入數據庫，由後端計算）
    @Transient
    private String status;

    // 無參構造函數
    public AttendanceRecord() {
    }

    // Getters 和 Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getClockInTime() {
        return clockInTime;
    }

    public void setClockInTime(LocalDateTime clockInTime) {
        this.clockInTime = clockInTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
