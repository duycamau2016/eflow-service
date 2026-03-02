package com.eflow.dto;

import com.eflow.entity.InvoiceMilestone.MilestoneStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO cho InvoiceMilestone - mốc xuất hóa đơn.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceMilestoneDTO {

    private Long id;

    @NotBlank(message = "Tên dự án không được để trống")
    private String projectName;

    @NotBlank(message = "Tên mốc không được để trống")
    private String name;

    private BigDecimal amount;
    private LocalDate plannedDate;
    private LocalDate actualDate;

    @NotNull(message = "Trạng thái không được để trống")
    private MilestoneStatus status;

    private String note;
    private int sortOrder;

    /** true nếu quá plannedDate mà vẫn pending */
    private boolean overdue;
}
