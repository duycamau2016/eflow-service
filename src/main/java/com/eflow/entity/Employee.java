package com.eflow.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Employee entity - maps to Sheet 1 "Nhân sự" of the Excel import.
 *
 * Excel columns:
 *   A - ID          → id
 *   B - Họ và tên   → name
 *   C - Chức vụ     → position
 *   D - Phòng ban   → department
 *   E - Email       → email
 *   F - SĐT         → phone
 *   G - ID quản lý  → managerId (null = root)
 *   H - Ngày vào làm→ joinDate
 */
@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @Column(name = "id", nullable = false, unique = true, length = 50)
    private String id;

    @NotBlank(message = "Họ và tên không được để trống")
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank(message = "Chức vụ không được để trống")
    @Column(name = "position", nullable = false)
    private String position;

    @NotBlank(message = "Phòng ban không được để trống")
    @Column(name = "department", nullable = false)
    private String department;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    /** ID của quản lý trực tiếp; null nếu là node gốc */
    @Column(name = "manager_id", length = 50)
    private String managerId;

    @Column(name = "avatar")
    private String avatar;

    /** Cấp bậc trong sơ đồ tổ chức (được tính tự động) */
    @Column(name = "level")
    private int level;

    @Column(name = "join_date")
    private LocalDate joinDate;

    /** Các dự án mà nhân viên tham gia */
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Project> projects = new ArrayList<>();
}
