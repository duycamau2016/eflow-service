package com.eflow.controller;

import com.eflow.dto.ApiResponse;
import com.eflow.dto.DepartmentDTO;
import com.eflow.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Phòng ban", description = "Danh mục phòng ban toàn công ty")
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    // ─────────────────────────────────────────────
    //  GET ALL
    // ─────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentDTO>>> getAll() {
        List<DepartmentDTO> list = departmentService.findAll();
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách phòng ban thành công", list, list.size()));
    }

    // ─────────────────────────────────────────────
    //  CREATE
    // ─────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<ApiResponse<DepartmentDTO>> create(
            @Valid @RequestBody DepartmentDTO dto) {
        DepartmentDTO created = departmentService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Tạo phòng ban thành công", created));
    }

    // ─────────────────────────────────────────────
    //  RENAME
    // ─────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentDTO>> rename(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String newName = body.getOrDefault("name", "");
        DepartmentDTO updated = departmentService.rename(id, newName);
        return ResponseEntity.ok(ApiResponse.ok("Đổi tên phòng ban thành công", updated));
    }

    // ─────────────────────────────────────────────
    //  DELETE
    // ─────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa phòng ban thành công", null));
    }

    // ─────────────────────────────────────────────
    //  SEED (từ import nhân viên — chỉ thêm thiếu)
    // ─────────────────────────────────────────────
    @PostMapping("/seed")
    public ResponseEntity<ApiResponse<List<DepartmentDTO>>> seed(
            @RequestBody List<String> names) {
        List<DepartmentDTO> list = departmentService.seed(names);
        return ResponseEntity.ok(ApiResponse.ok("Seed phòng ban thành công", list, list.size()));
    }
}
