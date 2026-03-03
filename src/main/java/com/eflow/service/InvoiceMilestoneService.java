package com.eflow.service;

import com.eflow.dto.InvoiceMilestoneDTO;
import com.eflow.entity.InvoiceMilestone;
import com.eflow.entity.InvoiceMilestone.MilestoneStatus;
import com.eflow.exception.ResourceNotFoundException;
import com.eflow.mapper.InvoiceMilestoneMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvoiceMilestoneService {

    private final InvoiceMilestoneMapper milestoneMapper;

    // ─────────────────────────────────────────────
    //  READ
    // ─────────────────────────────────────────────

    public List<InvoiceMilestoneDTO> findByProject(String projectName) {
        requireNotBlank(projectName, "projectName");
        return milestoneMapper
                .findByProjectNameIgnoreCaseOrderBySortOrderAsc(projectName)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public InvoiceMilestoneDTO findById(Long id) {
        return toDTO(getOrThrow(id));
    }

    // ─────────────────────────────────────────────
    //  WRITE
    // ─────────────────────────────────────────────

    @Transactional
    public InvoiceMilestoneDTO create(InvoiceMilestoneDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Dữ liệu không được null");
        requireNotBlank(dto.getProjectName(), "projectName");
        requireNotBlank(dto.getName(), "name");
        InvoiceMilestone entity = toEntity(dto);
        milestoneMapper.insert(entity);
        return toDTO(entity);
    }

    @Transactional
    public InvoiceMilestoneDTO update(Long id, InvoiceMilestoneDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Dữ liệu không được null");
        InvoiceMilestone existing = getOrThrow(id);

        existing.setName(dto.getName());
        existing.setAmount(dto.getAmount());
        existing.setPlannedDate(dto.getPlannedDate());
        existing.setActualDate(dto.getActualDate());
        existing.setStatus(dto.getStatus());
        existing.setNote(dto.getNote());
        existing.setSortOrder(dto.getSortOrder());

        milestoneMapper.update(existing);
        return toDTO(existing);
    }

    @Transactional
    public void delete(Long id) {
        getOrThrow(id);
        milestoneMapper.deleteById(id);
    }

    // ─────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────

    private InvoiceMilestone getOrThrow(Long id) {
        return milestoneMapper.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mốc hóa đơn", "id", id));
    }

    private InvoiceMilestoneDTO toDTO(InvoiceMilestone entity) {
        boolean overdue = entity.getStatus() == MilestoneStatus.pending
                && entity.getPlannedDate() != null
                && entity.getPlannedDate().isBefore(LocalDate.now());

        return InvoiceMilestoneDTO.builder()
                .id(entity.getId())
                .projectName(entity.getProjectName())
                .name(entity.getName())
                .amount(entity.getAmount())
                .plannedDate(entity.getPlannedDate())
                .actualDate(entity.getActualDate())
                .status(entity.getStatus())
                .note(entity.getNote())
                .sortOrder(entity.getSortOrder())
                .overdue(overdue)
                .build();
    }

    private InvoiceMilestone toEntity(InvoiceMilestoneDTO dto) {
        return InvoiceMilestone.builder()
                .projectName(dto.getProjectName())
                .name(dto.getName())
                .amount(dto.getAmount())
                .plannedDate(dto.getPlannedDate())
                .actualDate(dto.getActualDate())
                .status(dto.getStatus() != null ? dto.getStatus() : MilestoneStatus.pending)
                .note(dto.getNote())
                .sortOrder(dto.getSortOrder())
                .build();
    }

    private static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Tham số '" + fieldName + "' không được để trống");
        }
    }
}
