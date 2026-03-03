package com.eflow.entity;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ProjectInfo entity - lưu thông tin tài chính tổng thể của một dự án.
 * Liên kết với Project qua projectName (không FK cứng để linh hoạt).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectInfo {

    private Long id;

    /** Tên dự án — unique, dùng làm khoá liên kết với bảng projects */
    private String projectName;

    /** Tên khách hàng (nhập tự do) */
    private String customer;

    /** Số hợp đồng */
    private String contractNumber;

    /** Mô tả / ghi chú hợp đồng */
    private String description;

    /** Ngày bắt đầu hợp đồng */
    private LocalDate startDate;

    /** Ngày kết thúc hợp đồng */
    private LocalDate endDate;

    /** Giá trị hợp đồng (VNĐ) */
    private BigDecimal contractValue;

    /** Chi phí kế hoạch (VNĐ) */
    private BigDecimal plannedCost;

    /** Chi phí thực tế (VNĐ) — admin nhập tay */
    private BigDecimal actualCost;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
