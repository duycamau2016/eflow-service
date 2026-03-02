package com.eflow.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ProjectInfo entity - lưu thông tin tài chính tổng thể của một dự án.
 * Liên kết với Project qua projectName (không FK cứng để linh hoạt).
 */
@Entity
@Table(name = "project_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tên dự án — unique, dùng làm khoá liên kết với bảng projects */
    @Column(name = "project_name", nullable = false, unique = true, length = 255)
    private String projectName;

    /** Tên khách hàng (nhập tự do) */
    @Column(name = "customer", length = 255)
    private String customer;

    /** Số hợp đồng */
    @Column(name = "contract_number", length = 100)
    private String contractNumber;

    /** Mô tả / ghi chú hợp đồng */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** Ngày bắt đầu hợp đồng */
    @Column(name = "start_date")
    private LocalDate startDate;

    /** Ngày kết thúc hợp đồng */
    @Column(name = "end_date")
    private LocalDate endDate;

    /** Giá trị hợp đồng (VNĐ) */
    @Column(name = "contract_value", precision = 18, scale = 0)
    private BigDecimal contractValue;

    /** Chi phí kế hoạch (VNĐ) */
    @Column(name = "planned_cost", precision = 18, scale = 0)
    private BigDecimal plannedCost;

    /** Chi phí thực tế (VNĐ) — admin nhập tay */
    @Column(name = "actual_cost", precision = 18, scale = 0)
    private BigDecimal actualCost;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
