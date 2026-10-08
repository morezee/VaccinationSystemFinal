package com.immunecare.service;

import com.immunecare.entity.AuditLog;
import com.immunecare.entity.User;
import com.immunecare.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void logAction(User user, String action, String tableAffected, Long recordId, String oldValues, String newValues) {
        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setAction(action);
        log.setTableAffected(tableAffected);
        log.setRecordId(recordId);
        log.setOldValues(oldValues);
        log.setNewValues(newValues);
        auditLogRepository.save(log);
    }
}
