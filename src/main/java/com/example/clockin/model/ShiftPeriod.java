package com.example.clockin.model;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "shift_periods")
public class ShiftPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "allowed_late_minutes")
    private int allowedLateMinutes;

    @Column(name = "allowed_early_leave_minutes")
    private int allowedEarlyLeaveMinutes;

    // 無參構造函數
    public ShiftPeriod() {}

    // Getters 和 Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Shift getShift() {
        return shift;
    }

    public void setShift(Shift shift) {
        this.shift = shift;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public int getAllowedLateMinutes() {
        return allowedLateMinutes;
    }

    public void setAllowedLateMinutes(int allowedLateMinutes) {
        this.allowedLateMinutes = allowedLateMinutes;
    }

    public int getAllowedEarlyLeaveMinutes() {
        return allowedEarlyLeaveMinutes;
    }

    public void setAllowedEarlyLeaveMinutes(int allowedEarlyLeaveMinutes) {
        this.allowedEarlyLeaveMinutes = allowedEarlyLeaveMinutes;
    }
}
