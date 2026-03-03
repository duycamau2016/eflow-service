package com.eflow.entity;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * InvoiceMilestone entity - mốc xuất hóa đơn của dự án.
 * Admin tạo tên mốc linh hoạt (không cố định SIT/UAT/PAT).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceMilestone {

    private Long id;

    /** Tên dự án — liên kết với project_info.project_name */
    private String projectName;

    /** Tên mốc hóa đơn (vd: "Kickoff", "UAT", "Go-live"...) */
    private String name;

    /** Giá trị hóa đơn mốc này (VNĐ) */
    private BigDecimal amount;

    /** Ngày xuất hóa đơn kế hoạch */
    private LocalDate plannedDate;

    /** Ngày xuất hóa đơn thực tế */
    private LocalDate actualDate;

    private MilestoneStatus status;

    /** Ghi chú */
    private String note;

    /** Thứ tự hiển thị */
    @Builder.Default
    private int sortOrder = 0;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum MilestoneStatus {
        pending, invoiced, paid
    }
}
