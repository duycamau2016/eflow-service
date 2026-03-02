package com.eflow.controller;

import com.eflow.dto.ApiResponse;
import com.eflow.dto.ProjectDTO;
import com.eflow.entity.Project.ProjectStatus;
import com.eflow.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller cho Dự án.
 *
 * Base URL: /api/projects
 *
 * Endpoints:
 *   GET    /api/projects                              → Lấy tất cả dự án
 *   GET    /api/projects/{id}                         → Lấy dự án theo ID
 *   GET    /api/projects/employee/{employeeId}        → Dự án của nhân viên
 *   GET    /api/projects/status/{status}              → Lọc theo trạng thái
 *   GET    /api/projects/employee/{employeeId}/status/{status} → Lọc kết hợp
 *   POST   /api/projects                              → Tạo dự án mới
 *   PUT    /api/projects/{id}                         → Cập nhật dự án
 *   DELETE /api/projects/{id}                         → Xoá dự án
 *   DELETE /api/projects/employee/{employeeId}        → Xoá tất cả dự án của nhân viên
 */
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    // ─────────────────────────────────────────────
    //  GET ALL
    // ─────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectDTO>>> getAll() {
        List<ProjectDTO> list = projectService.findAll();
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách dự án thành công", list, list.size()));
    }

    // ─────────────────────────────────────────────
    //  FILTER BY STATUS
    // ─────────────────────────────────────────────
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<ProjectDTO>>> getByStatus(
            @PathVariable ProjectStatus status) {
        List<ProjectDTO> list = projectService.findByStatus(status);
        return ResponseEntity.ok(ApiResponse.ok("Danh sách dự án theo trạng thái", list, list.size()));
    }

    // ─────────────────────────────────────────────
    //  FILTER BY EMPLOYEE
    // ─────────────────────────────────────────────
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<ProjectDTO>>> getByEmployee(
            @PathVariable String employeeId) {
        List<ProjectDTO> list = projectService.findByEmployee(employeeId);
        return ResponseEntity.ok(ApiResponse.ok("Dự án của nhân viên", list, list.size()));
    }

    // ─────────────────────────────────────────────
    //  FILTER BY EMPLOYEE + STATUS
    // ─────────────────────────────────────────────
    @GetMapping("/employee/{employeeId}/status/{status}")
    public ResponseEntity<ApiResponse<List<ProjectDTO>>> getByEmployeeAndStatus(
            @PathVariable String employeeId,
            @PathVariable ProjectStatus status) {
        List<ProjectDTO> list = projectService.findByEmployeeAndStatus(employeeId, status);
        return ResponseEntity.ok(ApiResponse.ok("Dự án của nhân viên theo trạng thái", list, list.size()));
    }

    // ─────────────────────────────────────────────
    //  GET BY ID
    // ─────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectDTO>> getById(@PathVariable String id) {
        ProjectDTO dto = projectService.findById(id);
        return ResponseEntity.ok(ApiResponse.ok("Lấy thông tin dự án thành công", dto));
    }

    // ─────────────────────────────────────────────
    //  CREATE
    // ─────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<ApiResponse<ProjectDTO>> create(@Valid @RequestBody ProjectDTO dto) {
        ProjectDTO created = projectService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Tạo dự án thành công", created));
    }

    // ─────────────────────────────────────────────
    //  UPDATE
    // ─────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectDTO>> update(
            @PathVariable String id,
            @Valid @RequestBody ProjectDTO dto) {
        ProjectDTO updated = projectService.update(id, dto);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật dự án thành công", updated));
    }

    // ─────────────────────────────────────────────────────────────────────
    //  BY PROJECT NAME
    // ─────────────────────────────────────────────────────────────────────

    /** GET /api/projects/by-name/{name} - lấy tất cả assignment của một dự án */
    @GetMapping("/by-name/{name}")
    public ResponseEntity<ApiResponse<List<ProjectDTO>>> getByProjectName(
            @PathVariable String name) {
        List<ProjectDTO> list = projectService.findByProjectName(name);
        return ResponseEntity.ok(ApiResponse.ok("Danh sách thành viên dự án", list, list.size()));
    }

    /** DELETE /api/projects/by-name/{name} - xoá toàn bộ dự án */
    @DeleteMapping("/by-name/{name}")
    public ResponseEntity<ApiResponse<Void>> deleteByProjectName(
            @PathVariable String name) {
        projectService.deleteByProjectName(name);
        return ResponseEntity.ok(ApiResponse.ok("Xoá dự án '" + name + "' thành công", null));
    }

    /** PUT /api/projects/by-name/{name}/rename?newName= - đổi tên dự án */
    @PutMapping("/by-name/{name}/rename")
    public ResponseEntity<ApiResponse<Void>> renameProject(
            @PathVariable String name,
            @RequestParam String newName) {
        projectService.renameProject(name, newName);
        return ResponseEntity.ok(ApiResponse.ok("Đổi tên dự án thành công", null));
    }

    /** PATCH /api/projects/by-name/{name}/status?status= - cập nhật trạng thái dự án độc lập */
    @PatchMapping("/by-name/{name}/status")
    public ResponseEntity<ApiResponse<Void>> updateProjectStatus(
            @PathVariable String name,
            @RequestParam ProjectStatus status) {
        projectService.updateProjectStatus(name, status);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái dự án thành công", null));
    }

    // ─────────────────────────────────────────────
    //  DELETE
    // ─────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        projectService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Xoá dự án thành công", null));
    }

    @DeleteMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<Void>> deleteByEmployee(@PathVariable String employeeId) {
        projectService.deleteByEmployee(employeeId);
        return ResponseEntity.ok(ApiResponse.ok("Xoá tất cả dự án của nhân viên thành công", null));
    }
}
