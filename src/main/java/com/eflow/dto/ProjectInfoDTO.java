package com.eflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO cho ProjectInfo - thông tin tài chính tổng thể dự án.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectInfoDTO {

    private Long id;

    @NotBlank(message = "Tên dự án không được để trống")
    private String projectName;

    private String customer;
    private String contractNumber;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;

    /** Giá trị hợp đồng (VNĐ) */
    private BigDecimal contractValue;

    /** Chi phí kế hoạch (VNĐ) */
    private BigDecimal plannedCost;

    /** Chi phí thực tế (VNĐ) */
    private BigDecimal actualCost;

    // ── Calculated fields (server-side, read-only) ──

    /** Tổng đã xuất hóa đơn (tổng invoice_milestone.amount đã invoiced/paid) */
    private BigDecimal totalInvoiced;

    /** Tổng đã thanh toán (tổng invoice_milestone.amount có status=paid) */
    private BigDecimal totalPaid;

    /**
     * Profit margin (%) = (contractValue - actualCost) / contractValue * 100
     * null nếu contractValue = 0 hoặc null
     */
    private Double profitMargin;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
