package com.eflow.controller;

import com.eflow.dto.ApiResponse;
import com.eflow.dto.ProjectPhaseDTO;
import com.eflow.service.ProjectPhaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller cho Giai đoạn / Tiến độ dự án.
 *
 * Base URL: /api/project-phases
 *
 * Endpoints:
 *   GET    /api/project-phases/{projectName}        → Tất cả phase của dự án
 *   GET    /api/project-phases/item/{id}            → Theo ID
 *   POST   /api/project-phases                      → Tạo phase mới
 *   PUT    /api/project-phases/{id}                 → Cập nhật
 *   DELETE /api/project-phases/{id}                 → Xóa
 */
@RestController
@RequestMapping("/api/project-phases")
@RequiredArgsConstructor
public class ProjectPhaseController {

    private final ProjectPhaseService phaseService;

    // ─────────────────────────────────────────────
    //  GET BY PROJECT
    // ─────────────────────────────────────────────
    @GetMapping("/{projectName}")
    public ResponseEntity<ApiResponse<List<ProjectPhaseDTO>>> getByProject(
            @PathVariable String projectName) {
        List<ProjectPhaseDTO> list = phaseService.findByProject(projectName);
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách giai đoạn dự án thành công", list, list.size()));
    }

    // ─────────────────────────────────────────────
    //  GET BY ID
    // ─────────────────────────────────────────────
    @GetMapping("/item/{id}")
    public ResponseEntity<ApiResponse<ProjectPhaseDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Lấy giai đoạn dự án thành công", phaseService.findById(id)));
    }

    // ─────────────────────────────────────────────
    //  CREATE
    // ─────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<ApiResponse<ProjectPhaseDTO>> create(
            @Valid @RequestBody ProjectPhaseDTO dto) {
        ProjectPhaseDTO created = phaseService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Tạo giai đoạn dự án thành công", created));
    }

    // ─────────────────────────────────────────────
    //  UPDATE
    // ─────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectPhaseDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody ProjectPhaseDTO dto) {
        ProjectPhaseDTO updated = phaseService.update(id, dto);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật giai đoạn dự án thành công", updated));
    }

    // ─────────────────────────────────────────────
    //  DELETE
    // ─────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        phaseService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa giai đoạn dự án thành công", null));
    }
}
