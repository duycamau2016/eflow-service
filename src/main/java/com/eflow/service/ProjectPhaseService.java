package com.eflow.service;

import com.eflow.dto.ProjectPhaseDTO;
import com.eflow.entity.ProjectPhase;
import com.eflow.entity.ProjectPhase.PhaseStatus;
import com.eflow.exception.ResourceNotFoundException;
import com.eflow.mapper.ProjectPhaseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectPhaseService {

    private final ProjectPhaseMapper phaseMapper;

    // ─────────────────────────────────────────────
    //  READ
    // ─────────────────────────────────────────────

    public List<ProjectPhaseDTO> findByProject(String projectName) {
        requireNotBlank(projectName, "projectName");
        return phaseMapper
                .findByProjectNameIgnoreCaseOrderBySortOrderAsc(projectName)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ProjectPhaseDTO findById(Long id) {
        return toDTO(getOrThrow(id));
    }

    // ─────────────────────────────────────────────
    //  WRITE
    // ─────────────────────────────────────────────

    @Transactional
    public ProjectPhaseDTO create(ProjectPhaseDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Dữ liệu không được null");
        requireNotBlank(dto.getProjectName(), "projectName");
        requireNotBlank(dto.getName(), "name");
        ProjectPhase entity = toEntity(dto);
        phaseMapper.insert(entity);
        return toDTO(entity);
    }

    @Transactional
    public ProjectPhaseDTO update(Long id, ProjectPhaseDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Dữ liệu không được null");
        ProjectPhase existing = getOrThrow(id);

        existing.setName(dto.getName());
        existing.setPlannedStart(dto.getPlannedStart());
        existing.setPlannedEnd(dto.getPlannedEnd());
        existing.setActualStart(dto.getActualStart());
        existing.setActualEnd(dto.getActualEnd());
        existing.setProgress(dto.getProgress());
        existing.setStatus(dto.getStatus());
        existing.setNote(dto.getNote());
        existing.setSortOrder(dto.getSortOrder());
        phaseMapper.update(existing);
        return toDTO(existing);
    }

    @Transactional
    public void delete(Long id) {
        getOrThrow(id);
        phaseMapper.deleteById(id);
    }

    // ─────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────

    private ProjectPhase getOrThrow(Long id) {
        return phaseMapper.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Giai đoạn dự án", "id", id));
    }

    private ProjectPhaseDTO toDTO(ProjectPhase entity) {
        return ProjectPhaseDTO.builder()
                .id(entity.getId())
                .projectName(entity.getProjectName())
                .name(entity.getName())
                .plannedStart(entity.getPlannedStart())
                .plannedEnd(entity.getPlannedEnd())
                .actualStart(entity.getActualStart())
                .actualEnd(entity.getActualEnd())
                .progress(entity.getProgress())
                .status(entity.getStatus())
                .note(entity.getNote())
                .sortOrder(entity.getSortOrder())
                .build();
    }

    private ProjectPhase toEntity(ProjectPhaseDTO dto) {
        return ProjectPhase.builder()
                .projectName(dto.getProjectName())
                .name(dto.getName())
                .plannedStart(dto.getPlannedStart())
                .plannedEnd(dto.getPlannedEnd())
                .actualStart(dto.getActualStart())
                .actualEnd(dto.getActualEnd())
                .progress(dto.getProgress())
                .status(dto.getStatus() != null ? dto.getStatus() : PhaseStatus.on_track)
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
