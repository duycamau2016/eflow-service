package com.eflow.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogDTO {

    private Long          id;
    private String        action;
    private String        entityType;
    private String        entityId;
    private String        entityName;
    private String        actor;
    private LocalDateTime createdAt;
    private String        detail;
}
