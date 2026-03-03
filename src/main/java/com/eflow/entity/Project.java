package com.eflow.entity;

import lombok.*;

import java.time.LocalDate;

/**
 * Project entity - maps to Sheet 2 "Dự án" of the Excel import.
 *
 * Excel columns:
 *   A - ID nhân viên  → employeeId (FK)
 *   B - Tên dự án     → name
 *   C - Vai trò       → role
 *   D - Ngày bắt đầu  → startDate
 *   E - Ngày kết thúc → endDate (null = đang tiếp tục)
 *   F - Trạng thái    → status (active / completed / pending)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    private String id;
    private String employeeId;
    private String name;
    private String role;
    private LocalDate startDate;

    /** Null = dự án đang tiếp tục */
    private LocalDate endDate;

    private ProjectStatus status;

    public enum ProjectStatus {
        active, completed, pending
    }
}
