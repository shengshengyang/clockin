package com.example.clockin.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "shifts")
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "shift_name", nullable = false)
    private String shiftName;

    // 與 ShiftPeriod 的一對多關係
    @OneToMany(mappedBy = "shift", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ShiftPeriod> periods;

    // 與 User 的一對多關係
    @OneToMany(mappedBy = "shift")
    private List<User> users;

    // 無參構造函數
    public Shift() {}

    // Getters 和 Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getShiftName() {
        return shiftName;
    }

    public void setShiftName(String shiftName) {
        this.shiftName = shiftName;
    }

    public List<ShiftPeriod> getPeriods() {
        return periods;
    }

    public void setPeriods(List<ShiftPeriod> periods) {
        this.periods = periods;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
