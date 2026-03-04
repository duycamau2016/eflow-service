package com.eflow.entity;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Bản ghi lịch sử thay đổi dữ liệu.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    private Long          id;

    /** CREATE | UPDATE | DELETE | IMPORT */
    private String        action;

    /** EMPLOYEE | PROJECT | DEPARTMENT */
    private String        entityType;

    private String        entityId;
    private String        entityName;

    /** Username thực hiện thao tác */
    private String        actor;

    private LocalDateTime createdAt;

    /** Thông tin bổ sung tùy chọn */
    private String        detail;
}
