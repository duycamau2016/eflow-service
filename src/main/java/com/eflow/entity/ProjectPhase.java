package com.eflow.entity;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ProjectPhase entity - giai đoạn / tiến độ thực hiện dự án.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectPhase {

    private Long id;

    /** Tên dự án — liên kết với project_info.project_name */
    private String projectName;

    /** Tên giai đoạn (vd: "Phân tích yêu cầu", "Dev", "Testing"...) */
    private String name;

    /** Ngày bắt đầu kế hoạch */
    private LocalDate plannedStart;

    /** Ngày kết thúc kế hoạch */
    private LocalDate plannedEnd;

    /** Ngày bắt đầu thực tế */
    private LocalDate actualStart;

    /** Ngày kết thúc thực tế */
    private LocalDate actualEnd;

    /** % hoàn thành (0–100) */
    @Builder.Default
    private int progress = 0;

    @Builder.Default
    private PhaseStatus status = PhaseStatus.on_track;

    /** Ghi chú */
    private String note;

    /** Thứ tự hiển thị */
    @Builder.Default
    private int sortOrder = 0;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum PhaseStatus {
        on_track, at_risk, delayed, completed
    }
}
