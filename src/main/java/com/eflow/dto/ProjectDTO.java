package com.eflow.dto;

import com.eflow.entity.Project.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

/**
 * DTO cho Project - ánh xạ tới interface Project trong Angular.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDTO {

    private String id;

    /** ID nhân viên tham gia dự án */
    @NotBlank(message = "employeeId không được để trống")
    private String employeeId;

    @NotBlank(message = "Tên dự án không được để trống")
    private String name;

    @NotBlank(message = "Vai trò không được để trống")
    private String role;

    private LocalDate startDate;

    /** null = dự án đang tiếp tục */
    private LocalDate endDate;

    @NotNull(message = "Trạng thái không được để trống")
    private ProjectStatus status;
}
