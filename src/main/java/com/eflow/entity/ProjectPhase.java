package com.eflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * ProjectPhase entity - giai đoạn / tiến độ thực hiện dự án.
 */
@Entity
@Table(name = "project_phase")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectPhase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tên dự án — liên kết với project_info.project_name */
    @Column(name = "project_name", nullable = false, length = 255)
    private String projectName;

    /** Tên giai đoạn (vd: "Phân tích yêu cầu", "Dev", "Testing"...) */
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /** Ngày bắt đầu kế hoạch */
    @Column(name = "planned_start")
    private LocalDate plannedStart;

    /** Ngày kết thúc kế hoạch */
    @Column(name = "planned_end")
    private LocalDate plannedEnd;

    /** Ngày bắt đầu thực tế */
    @Column(name = "actual_start")
    private LocalDate actualStart;

    /** Ngày kết thúc thực tế */
    @Column(name = "actual_end")
    private LocalDate actualEnd;

    /** % hoàn thành (0–100) */
    @Column(name = "progress", nullable = false)
    @Builder.Default
    private int progress = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PhaseStatus status = PhaseStatus.on_track;

    /** Ghi chú */
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    /** Thứ tự hiển thị */
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private int sortOrder = 0;

    public enum PhaseStatus {
        on_track, at_risk, delayed, completed
    }
}
