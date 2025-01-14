package com.example.clockin.repo;

import com.example.clockin.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository  extends JpaRepository<AuditLog, Integer> {
    AuditLog findFirstByOrderByIdDesc();
}
