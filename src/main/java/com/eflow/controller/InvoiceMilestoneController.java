package com.eflow.controller;

import com.eflow.dto.ApiResponse;
import com.eflow.dto.InvoiceMilestoneDTO;
import com.eflow.service.InvoiceMilestoneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Mốc Hóa đơn", description = "CRUD mốc xuất hóa đơn")
@RestController
@RequestMapping("/api/invoice-milestones")
@RequiredArgsConstructor
public class InvoiceMilestoneController {

    private final InvoiceMilestoneService milestoneService;

    // ─────────────────────────────────────────────
    //  GET BY PROJECT
    // ─────────────────────────────────────────────
    @GetMapping("/{projectName}")
    public ResponseEntity<ApiResponse<List<InvoiceMilestoneDTO>>> getByProject(
            @PathVariable String projectName) {
        List<InvoiceMilestoneDTO> list = milestoneService.findByProject(projectName);
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách mốc hóa đơn thành công", list, list.size()));
    }

    // ─────────────────────────────────────────────
    //  GET BY ID
    // ─────────────────────────────────────────────
    @GetMapping("/item/{id}")
    public ResponseEntity<ApiResponse<InvoiceMilestoneDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Lấy mốc hóa đơn thành công", milestoneService.findById(id)));
    }

    // ─────────────────────────────────────────────
    //  CREATE
    // ─────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<ApiResponse<InvoiceMilestoneDTO>> create(
            @Valid @RequestBody InvoiceMilestoneDTO dto) {
        InvoiceMilestoneDTO created = milestoneService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Tạo mốc hóa đơn thành công", created));
    }

    // ─────────────────────────────────────────────
    //  UPDATE
    // ─────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceMilestoneDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody InvoiceMilestoneDTO dto) {
        InvoiceMilestoneDTO updated = milestoneService.update(id, dto);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật mốc hóa đơn thành công", updated));
    }

    // ─────────────────────────────────────────────
    //  DELETE
    // ─────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        milestoneService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa mốc hóa đơn thành công", null));
    }
}
