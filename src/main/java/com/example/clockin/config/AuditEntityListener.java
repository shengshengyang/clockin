package com.example.clockin.config;

import com.example.clockin.AuditLog;
import com.example.clockin.repo.AuditLogRepository;
import com.example.clockin.service.Auditable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PreUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AuditEntityListener {

    private static AuditLogRepository staticAuditLogRepository;

    @Autowired
    public void setAuditLogRepository(AuditLogRepository auditLogRepository) {
        // 靜態注入，以便在實體事件中使用
        AuditEntityListener.staticAuditLogRepository = auditLogRepository;
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostPersist
    public void onInsert(Object entity) {
        saveAuditLog(entity, "INSERT", null, entity.toString());
    }


    /**
     * 用來在 @PreUpdate 階段暫存舊值 (示範性做法)
     */
    @PreUpdate
    public void preUpdate(Object entity) {
        if (entity instanceof Auditable auditableEntity) {
            // 將實體的目前狀態序列化，存在實體本身的暫存欄位
            auditableEntity.setOldValueSnapshot(serialize(entity));
        }
    }

    /**
     * 更新後將舊值 / 新值紀錄進AuditLog
     */
    @PostUpdate
    public void onUpdate(Object entity) {
        if (entity instanceof Auditable auditableEntity) {
            String oldValue = auditableEntity.getOldValueSnapshot();
            String newValue = serialize(entity);

            saveAuditLog(entity, "UPDATE", oldValue, newValue);
        }
    }

    /**
     * 刪除後記錄
     */
    @PostRemove
    public void onDelete(Object entity) {
        // 刪除只會有舊值
        saveAuditLog(entity, "DELETE", serialize(entity), null);
    }

    // =============================================
    // =========== 審計紀錄的核心方法  ===============
    // =============================================

    private static void saveAuditLog(Object entity, String action,
                                     String oldValue, String newValue) {
        if (staticAuditLogRepository == null) {
            // 若未成功注入 Repository，可使用其他方式處理
            return;
        }

        AuditLog log = new AuditLog();
        log.setEntityName(entity.getClass().getSimpleName());
        log.setEntityId(getEntityId(entity));
        log.setAction(action);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setChangeTimestamp(Instant.now());
        staticAuditLogRepository.save(log);
    }

    private String serialize(Object entity) {
        try {
            return objectMapper.writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private static Long getEntityId(Object entity) {
        try {
            // 假設您在每個實體都有 getId() 方法
            return (Long) entity.getClass().getMethod("getId").invoke(entity);
        } catch (Exception e) {
            return null;
        }
    }
}

