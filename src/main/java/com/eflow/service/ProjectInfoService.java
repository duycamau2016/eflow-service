package com.eflow.service;

import com.eflow.dto.ProjectInfoDTO;
import com.eflow.entity.ProjectInfo;
import com.eflow.exception.ResourceNotFoundException;
import com.eflow.mapper.InvoiceMilestoneMapper;
import com.eflow.mapper.ProjectInfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectInfoService {

    private final ProjectInfoMapper projectInfoMapper;
    private final InvoiceMilestoneMapper milestoneMapper;

    // ─────────────────────────────────────────────
    //  READ
    // ─────────────────────────────────────────────

    public List<ProjectInfoDTO> findAll() {
        return projectInfoMapper.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ProjectInfoDTO findByProjectName(String projectName) {
        requireNotBlank(projectName, "projectName");
        return toDTO(getOrThrow(projectName));
    }

    public ProjectInfoDTO findById(Long id) {
        ProjectInfo info = projectInfoMapper.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thông tin tài chính dự án", "id", id));
        return toDTO(info);
    }

    // ─────────────────────────────────────────────
    //  WRITE
    // ─────────────────────────────────────────────

    @Transactional
    public ProjectInfoDTO create(ProjectInfoDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Dữ liệu không được null");
        requireNotBlank(dto.getProjectName(), "projectName");
        if (projectInfoMapper.existsByProjectNameIgnoreCase(dto.getProjectName())) {
            throw new IllegalArgumentException(
                    "Dự án '" + dto.getProjectName() + "' đã có thông tin tài chính");
        }
        ProjectInfo entity = toEntity(dto);
        projectInfoMapper.insert(entity);
        return toDTO(entity);
    }

    @Transactional
    public ProjectInfoDTO update(String projectName, ProjectInfoDTO dto) {
        requireNotBlank(projectName, "projectName");
        if (dto == null) throw new IllegalArgumentException("Dữ liệu không được null");
        ProjectInfo existing = getOrThrow(projectName);

        if (!existing.getProjectName().equalsIgnoreCase(dto.getProjectName()) &&
                projectInfoMapper.existsByProjectNameIgnoreCaseAndIdNot(dto.getProjectName(), existing.getId())) {
            throw new IllegalArgumentException("Tên dự án '" + dto.getProjectName() + "' đã tồn tại");
        }

        existing.setProjectName(dto.getProjectName());
        existing.setCustomer(dto.getCustomer());
        existing.setContractNumber(dto.getContractNumber());
        existing.setDescription(dto.getDescription());
        existing.setStartDate(dto.getStartDate());
        existing.setEndDate(dto.getEndDate());
        existing.setContractValue(dto.getContractValue());
        existing.setPlannedCost(dto.getPlannedCost());
        existing.setActualCost(dto.getActualCost());

        projectInfoMapper.update(existing);
        return toDTO(existing);
    }

    @Transactional
    public void delete(String projectName) {
        requireNotBlank(projectName, "projectName");
        ProjectInfo existing = getOrThrow(projectName);
        projectInfoMapper.deleteById(existing.getId());
    }

    // ─────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────

    private ProjectInfo getOrThrow(String projectName) {
        return projectInfoMapper.findByProjectNameIgnoreCase(projectName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Thông tin tài chính dự án", "projectName", projectName));
    }

    private ProjectInfoDTO toDTO(ProjectInfo entity) {
        BigDecimal totalInvoiced = milestoneMapper.sumInvoicedAmountByProject(entity.getProjectName());
        BigDecimal totalPaid = milestoneMapper.sumPaidAmountByProject(entity.getProjectName());

        if (totalInvoiced == null) totalInvoiced = BigDecimal.ZERO;
        if (totalPaid == null) totalPaid = BigDecimal.ZERO;

        // Tính profit margin
        Double profitMargin = null;
        if (entity.getContractValue() != null &&
                entity.getContractValue().compareTo(BigDecimal.ZERO) > 0 &&
                entity.getActualCost() != null) {
            profitMargin = entity.getContractValue()
                    .subtract(entity.getActualCost())
                    .divide(entity.getContractValue(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

        return ProjectInfoDTO.builder()
                .id(entity.getId())
                .projectName(entity.getProjectName())
                .customer(entity.getCustomer())
                .contractNumber(entity.getContractNumber())
                .description(entity.getDescription())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .contractValue(entity.getContractValue())
                .plannedCost(entity.getPlannedCost())
                .actualCost(entity.getActualCost())
                .totalInvoiced(totalInvoiced)
                .totalPaid(totalPaid)
                .profitMargin(profitMargin)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private ProjectInfo toEntity(ProjectInfoDTO dto) {
        return ProjectInfo.builder()
                .projectName(dto.getProjectName())
                .customer(dto.getCustomer())
                .contractNumber(dto.getContractNumber())
                .description(dto.getDescription())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .contractValue(dto.getContractValue())
                .plannedCost(dto.getPlannedCost())
                .actualCost(dto.getActualCost())
                .build();
    }

    private static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Tham số '" + fieldName + "' không được để trống");
        }
    }
}
