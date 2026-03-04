package com.eflow.controller;

import com.eflow.config.RequestContext;
import com.eflow.dto.ApiResponse;
import com.eflow.dto.EmployeeDTO;
import com.eflow.dto.OrgNodeDTO;
import com.eflow.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Nhân viên", description = "CRUD nhân viên + sơ đồ tổ chức")
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
    //  ⛔ Manager không được phép import
    // ─────────────────────────────────────────────
    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<Void>> bulkImport(@RequestBody List<EmployeeDTO> dtos) {
        if (RequestContext.isManager()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Tài khoản Manager không có quyền import dữ liệu"));
        }
        employeeService.bulkImport(dtos);
        return ResponseEntity.ok(ApiResponse.ok("Import thành công " + dtos.size() + " nhân viên", null));
    }

    // ─────────────────────────────────────────────
    //  CREATE
    //  ⛔ Manager không được phép tạo nhân viên mới
    // ─────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeDTO>> create(@Valid @RequestBody EmployeeDTO dto) {
        if (RequestContext.isManager()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Tài khoản Manager không có quyền tạo nhân viên mới"));
        }
        EmployeeDTO created = employeeService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Tạo nhân viên thành công", created));
    }

    // ─────────────────────────────────────────────
    //  UPDATE
    //  ✅ Manager chỉ được sửa nhân viên thuộc phòng ban của mình
    // ─────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeDTO>> update(
            @PathVariable String id,
            @Valid @RequestBody EmployeeDTO dto) {

        if (RequestContext.isManager()) {
            EmployeeDTO existing = employeeService.findById(id);
            ResponseEntity<ApiResponse<EmployeeDTO>> denied = checkManagerDeptAccess(existing.getDepartment());
            if (denied != null) return denied;
        }

        EmployeeDTO updated = employeeService.update(id, dto);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật nhân viên thành công", updated));
    }

    // ─────────────────────────────────────────────
    //  DELETE
    //  ⛔ Manager không được xoá nhân viên
    // ─────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        if (RequestContext.isManager()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Tài khoản Manager không có quyền xoá nhân viên"));
        }
        employeeService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Xoá nhân viên thành công", null));
    }

    // ─────────────────────────────────────────────
    //  HELPERS — access control
    // ─────────────────────────────────────────────

    /**
     * Kiểm tra phòng ban của nhân viên mục tiêu có khớp với phòng ban quản lý của Manager không.
     * Trả về 403 ResponseEntity nếu bị từ chối, null nếu được phép.
     * <p>
     * Matching: "Kỹ thuật / Engineering".split(" / ")[0] == empDept (case-insensitive).
     */
    private ResponseEntity<ApiResponse<EmployeeDTO>> checkManagerDeptAccess(String empDepartment) {
        String managerDept = RequestContext.getManagerDepartment();
        if (!deptMatches(managerDept, empDepartment)) {
            String deptLabel = (managerDept != null)
                    ? "\"" + managerDept.split(" / ")[0].trim() + "\""
                    : "của bạn";
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(
                            "Bạn chỉ được chỉnh sửa nhân viên thuộc phòng ban " + deptLabel));
        }
        return null;
    }

    /**
     * So khớp tên phòng ban employee với phần tiếng Việt của managerDept.
     * VD: deptMatches("Kỹ thuật / Engineering", "Kỹ thuật") → true
     */
    private static boolean deptMatches(String managerDept, String empDept) {
        if (managerDept == null || empDept == null) return false;
        String primary = managerDept.split(" / ")[0].trim();
        return primary.equalsIgnoreCase(empDept.trim());
    }}