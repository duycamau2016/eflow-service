package com.eflow.controller;

import com.eflow.dto.ApiResponse;
import com.eflow.dto.ProjectInfoDTO;
import com.eflow.service.ProjectInfoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller cho Thông tin tài chính dự án.
 *
 * Base URL: /api/project-info
 *
 * Endpoints:
 *   GET    /api/project-info                        → Tất cả dự án có thông tin tài chính
 *   GET    /api/project-info/{projectName}          → Theo tên dự án
 *   POST   /api/project-info                        → Tạo mới
 *   PUT    /api/project-info/{projectName}          → Cập nhật
 *   DELETE /api/project-info/{projectName}          → Xóa
 */
@RestController
@RequestMapping("/api/project-info")
@RequiredArgsConstructor
public class ProjectInfoController {

    private final ProjectInfoService projectInfoService;

    // ─────────────────────────────────────────────
    //  GET ALL
    // ─────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectInfoDTO>>> getAll() {
        List<ProjectInfoDTO> list = projectInfoService.findAll();
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách thông tin tài chính dự án thành công", list, list.size()));
    }

    // ─────────────────────────────────────────────
    //  GET BY PROJECT NAME
    // ─────────────────────────────────────────────
    @GetMapping("/{projectName}")
    public ResponseEntity<ApiResponse<ProjectInfoDTO>> getByProjectName(
            @PathVariable String projectName) {
        ProjectInfoDTO dto = projectInfoService.findByProjectName(projectName);
        return ResponseEntity.ok(ApiResponse.ok("Lấy thông tin tài chính dự án thành công", dto));
    }

    // ─────────────────────────────────────────────
    //  CREATE
    // ─────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<ApiResponse<ProjectInfoDTO>> create(
            @Valid @RequestBody ProjectInfoDTO dto) {
        ProjectInfoDTO created = projectInfoService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Tạo thông tin tài chính dự án thành công", created));
    }

    // ─────────────────────────────────────────────
    //  UPDATE
    // ─────────────────────────────────────────────
    @PutMapping("/{projectName}")
    public ResponseEntity<ApiResponse<ProjectInfoDTO>> update(
            @PathVariable String projectName,
            @Valid @RequestBody ProjectInfoDTO dto) {
        ProjectInfoDTO updated = projectInfoService.update(projectName, dto);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thông tin tài chính dự án thành công", updated));
    }

    // ─────────────────────────────────────────────
    //  DELETE
    // ─────────────────────────────────────────────
    @DeleteMapping("/{projectName}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String projectName) {
        projectInfoService.delete(projectName);
        return ResponseEntity.ok(ApiResponse.ok("Xóa thông tin tài chính dự án thành công", null));
    }
}
