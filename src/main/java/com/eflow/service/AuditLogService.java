package com.eflow.service;

import com.eflow.config.RequestContext;
import com.eflow.dto.AuditLogDTO;
import com.eflow.entity.AuditLog;
import com.eflow.mapper.AuditLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogMapper auditLogMapper;

    // ─────────────────────────────────────────────
    //  WRITE (dùng REQUIRES_NEW để không bị rollback
    //  cùng transaction nghiệp vụ)
    // ─────────────────────────────────────────────

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String entityType,
                    String entityId, String entityName, String detail) {
        try {
            AuditLog entry = AuditLog.builder()
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .entityName(entityName)
                    .actor(RequestContext.getActor())
                    .detail(detail)
                    .build();
            auditLogMapper.insert(entry);
        } catch (Exception ex) {
            // Không để lỗi audit làm fail nghiệp vụ chính
            log.warn("Không thể ghi audit log: {}", ex.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    //  READ
    // ─────────────────────────────────────────────

    public List<AuditLogDTO> findWithFilters(String entityType, String action,
                                              String actor, int page, int size) {
        int offset = page * size;
        return auditLogMapper.findWithFilters(entityType, action, actor, size, offset)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public int countWithFilters(String entityType, String action, String actor) {
        return auditLogMapper.countWithFilters(entityType, action, actor);
    }

    // ─────────────────────────────────────────────
    //  HELPER
    // ─────────────────────────────────────────────

    private AuditLogDTO toDTO(AuditLog e) {
        return AuditLogDTO.builder()
                .id(e.getId())
                .action(e.getAction())
                .entityType(e.getEntityType())
                .entityId(e.getEntityId())
                .entityName(e.getEntityName())
                .actor(e.getActor())
                .createdAt(e.getCreatedAt())
                .detail(e.getDetail())
                .build();
    }
}
