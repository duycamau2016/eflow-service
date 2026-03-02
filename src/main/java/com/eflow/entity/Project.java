package com.eflow.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;

/**
 * Project entity - maps to Sheet 2 "Dự án" of the Excel import.
 *
 * Excel columns:
 *   A - ID nhân viên  → employee (FK)
 *   B - Tên dự án     → name
 *   C - Vai trò       → role
 *   D - Ngày bắt đầu  → startDate
 *   E - Ngày kết thúc → endDate (null = đang tiếp tục)
 *   F - Trạng thái    → status (active / completed / pending)
 */
@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @Column(name = "id", nullable = false, unique = true, length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotBlank(message = "Tên dự án không được để trống")
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank(message = "Vai trò không được để trống")
    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "start_date")
    private LocalDate startDate;

    /** Null = dự án đang tiếp tục */
    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProjectStatus status;

    public enum ProjectStatus {
        active, completed, pending
    }
}
