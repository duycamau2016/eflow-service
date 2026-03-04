package com.eflow.controller;

import com.eflow.dto.ApiResponse;
import com.eflow.dto.AuditLogDTO;
import com.eflow.service.AuditLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Audit Log", description = "Lịch sử thay đổi dữ liệu hệ thống")
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    /**
     * GET /api/audit-logs
     *
     * @param entityType  EMPLOYEE | PROJECT | DEPARTMENT (optional)
     * @param action      CREATE | UPDATE | DELETE | IMPORT (optional)
     * @param actor       tìm theo username (optional, substring)
     * @param page        trang (0-based)
     * @param size        số dòng mỗi trang (default 50)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AuditLogDTO>>> getAll(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String actor,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {

        List<AuditLogDTO> list  = auditLogService.findWithFilters(entityType, action, actor, page, size);
        int total               = auditLogService.countWithFilters(entityType, action, actor);
        return ResponseEntity.ok(ApiResponse.ok("Lịch sử thay đổi", list, total));
    }
}
