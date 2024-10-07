package com.example.clockin.repo;

import com.example.clockin.model.Shift;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShiftRepository extends JpaRepository<Shift, Integer> {

    Shift findByShiftName(String name);
}
