package com.eflow.dto;

import com.eflow.entity.ProjectPhase.PhaseStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

/**
 * DTO cho ProjectPhase - giai đoạn / tiến độ dự án.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectPhaseDTO {

    private Long id;

    @NotBlank(message = "Tên dự án không được để trống")
    private String projectName;

    @NotBlank(message = "Tên giai đoạn không được để trống")
    private String name;

    private LocalDate plannedStart;
    private LocalDate plannedEnd;
    private LocalDate actualStart;
    private LocalDate actualEnd;

    @Min(value = 0, message = "Progress tối thiểu là 0")
    @Max(value = 100, message = "Progress tối đa là 100")
    private int progress;

    @NotNull(message = "Trạng thái không được để trống")
    private PhaseStatus status;

    private String note;
    private int sortOrder;
}
