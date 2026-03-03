package com.eflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO cho phòng ban.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentDTO {

    private Long id;

    @NotBlank(message = "Tên phòng ban không được để trống")
    @Size(max = 255, message = "Tên phòng ban tối đa 255 ký tự")
    private String name;

    private int sortOrder;
}
