package com.eflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * InvoiceMilestone entity - mốc xuất hóa đơn của dự án.
 * Admin tạo tên mốc linh hoạt (không cố định SIT/UAT/PAT).
 */
@Entity
@Table(name = "invoice_milestone")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceMilestone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tên dự án — liên kết với project_info.project_name */
    @Column(name = "project_name", nullable = false, length = 255)
    private String projectName;

    /** Tên mốc hóa đơn (vd: "Kickoff", "UAT", "Go-live"...) */
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /** Giá trị hóa đơn mốc này (VNĐ) */
    @Column(name = "amount", precision = 18, scale = 0)
    private BigDecimal amount;

    /** Ngày xuất hóa đơn kế hoạch */
    @Column(name = "planned_date")
    private LocalDate plannedDate;

    /** Ngày xuất hóa đơn thực tế */
    @Column(name = "actual_date")
    private LocalDate actualDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MilestoneStatus status;

    /** Ghi chú */
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    /** Thứ tự hiển thị */
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private int sortOrder = 0;

    public enum MilestoneStatus {
        pending, invoiced, paid
    }
}
