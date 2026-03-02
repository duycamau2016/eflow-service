package com.eflow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO cho Employee - ánh xạ tới interface Employee trong Angular.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDTO {

    private String id;

    @NotBlank(message = "Họ và tên không được để trống")
    private String name;

    @NotBlank(message = "Chức vụ không được để trống")
    private String position;

    @NotBlank(message = "Phòng ban không được để trống")
    private String department;

    @Email(message = "Email không hợp lệ")
    private String email;

    private String phone;

    /** null nếu là node gốc */
    private String managerId;

    private String avatar;

    /** Cấp bậc trong sơ đồ tổ chức (tính tự động) */
    private int level;

    private LocalDate joinDate;

    /** Danh sách dự án (chỉ trả về trong get-by-id / org-tree) */
    private List<ProjectDTO> projects;

    /** Số nhân viên cấp dưới (computed) */
    private Integer subordinatesCount;
}
