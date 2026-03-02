package com.eflow.controller;

import com.eflow.dto.ApiResponse;
import com.eflow.dto.EmployeeDTO;
import com.eflow.dto.OrgNodeDTO;
import com.eflow.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller cho Nhân viên.
 *
 * Base URL: /api/employees
 *
 * Endpoints:
 *   GET    /api/employees                       → Lấy tất cả nhân viên
 *   GET    /api/employees/{id}                  → Lấy nhân viên theo ID (kèm dự án)
 *   GET    /api/employees/search?keyword=       → Tìm kiếm nhân viên
 *   GET    /api/employees/department/{dept}     → Lọc theo phòng ban
 *   GET    /api/employees/{id}/subordinates     → Lấy nhân viên cấp dưới
 *   GET    /api/employees/org-tree              → Lấy sơ đồ tổ chức dạng cây
 *   POST   /api/employees                       → Tạo nhân viên mới
 *   PUT    /api/employees/{id}                  → Cập nhật nhân viên
 *   DELETE /api/employees/{id}                  → Xoá nhân viên
 */
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    // ─────────────────────────────────────────────
    //  GET ALL
    // ─────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<ApiResponse<List<EmployeeDTO>>> getAll() {
        List<EmployeeDTO> list = employeeService.findAll();
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách nhân viên thành công", list, list.size()));
    }

    // ─────────────────────────────────────────────
    //  ORG TREE (phải khai báo trước /{id} để tránh conflict)
    // ─────────────────────────────────────────────
    @GetMapping("/org-tree")
    public ResponseEntity<ApiResponse<List<OrgNodeDTO>>> getOrgTree() {
        List<OrgNodeDTO> tree = employeeService.buildOrgTree();
        return ResponseEntity.ok(ApiResponse.ok("Lấy sơ đồ tổ chức thành công", tree));
    }

    // ─────────────────────────────────────────────
    //  SEARCH
    // ─────────────────────────────────────────────
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<EmployeeDTO>>> search(
            @RequestParam String keyword) {
        List<EmployeeDTO> result = employeeService.search(keyword);
        return ResponseEntity.ok(ApiResponse.ok("Kết quả tìm kiếm", result, result.size()));
    }

    // ─────────────────────────────────────────────
    //  FILTER BY DEPARTMENT
    // ─────────────────────────────────────────────
    @GetMapping("/department/{department}")
    public ResponseEntity<ApiResponse<List<EmployeeDTO>>> getByDepartment(
            @PathVariable String department) {
        List<EmployeeDTO> list = employeeService.findByDepartment(department);
        return ResponseEntity.ok(ApiResponse.ok("Danh sách nhân viên phòng " + department, list, list.size()));
    }

    // ─────────────────────────────────────────────
    //  GET BY ID
    // ─────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeDTO>> getById(@PathVariable String id) {
        EmployeeDTO dto = employeeService.findById(id);
        return ResponseEntity.ok(ApiResponse.ok("Lấy thông tin nhân viên thành công", dto));
    }

    // ─────────────────────────────────────────────
    //  SUBORDINATES
    // ─────────────────────────────────────────────
    @GetMapping("/{id}/subordinates")
    public ResponseEntity<ApiResponse<List<EmployeeDTO>>> getSubordinates(@PathVariable String id) {
        List<EmployeeDTO> list = employeeService.findSubordinates(id);
        return ResponseEntity.ok(ApiResponse.ok("Danh sách nhân viên cấp dưới", list, list.size()));
    }

    // ─────────────────────────────────────────────
    //  BULK IMPORT (từ Angular Excel parse)
    // ─────────────────────────────────────────────
    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<Void>> bulkImport(@RequestBody List<EmployeeDTO> dtos) {
        employeeService.bulkImport(dtos);
        return ResponseEntity.ok(ApiResponse.ok("Import thành công " + dtos.size() + " nhân viên", null));
    }

    // ─────────────────────────────────────────────
    //  CREATE
    // ─────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeDTO>> create(@Valid @RequestBody EmployeeDTO dto) {
        EmployeeDTO created = employeeService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Tạo nhân viên thành công", created));
    }

    // ─────────────────────────────────────────────
    //  UPDATE
    // ─────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeDTO>> update(
            @PathVariable String id,
            @Valid @RequestBody EmployeeDTO dto) {
        EmployeeDTO updated = employeeService.update(id, dto);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật nhân viên thành công", updated));
    }

    // ─────────────────────────────────────────────
    //  DELETE
    // ─────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        employeeService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Xoá nhân viên thành công", null));
    }
}
