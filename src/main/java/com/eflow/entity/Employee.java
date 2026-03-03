package com.eflow.entity;

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
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    private String id;
    private String name;
    private String position;
    private String department;
    private String email;
    private String phone;

    /** ID của quản lý trực tiếp; null nếu là node gốc */
    private String managerId;

    private String avatar;

    /** Cấp bậc trong sơ đồ tổ chức (được tính tự động) */
    private int level;

    private LocalDate joinDate;

    /** Các dự án mà nhân viên tham gia (load thủ công qua mapper) */
    @Builder.Default
    private List<Project> projects = new ArrayList<>();
}
