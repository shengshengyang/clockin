package com.example.clockin.repo;

import com.example.clockin.model.ShiftPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShiftPeriodRepository extends JpaRepository<ShiftPeriod, Integer> {
}
