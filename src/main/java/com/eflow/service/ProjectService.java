package com.eflow.service;

import com.eflow.dto.ProjectDTO;
import com.eflow.entity.Project;
import com.eflow.entity.Project.ProjectStatus;
import com.eflow.exception.ResourceNotFoundException;
import com.eflow.mapper.EmployeeMapper;
import com.eflow.mapper.ProjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectMapper   projectMapper;
    private final EmployeeMapper  employeeMapper;
    private final AuditLogService auditLogService;

    // ─────────────────────────────────────────────
    //  READ
    // ─────────────────────────────────────────────

    /** Lấy tất cả dự án */
    public List<ProjectDTO> findAll() {
        return projectMapper.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /** Lấy dự án theo ID */
    public ProjectDTO findById(String id) {
        requireNotBlank(id, "id");
        return toDTO(getOrThrow(id));
    }

    /** Lấy tất cả dự án của một nhân viên */
    public List<ProjectDTO> findByEmployee(String employeeId) {
        requireNotBlank(employeeId, "employeeId");
        employeeMapper.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên", "id", employeeId));
        return projectMapper.findByEmployeeId(employeeId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /** Lọc dự án theo trạng thái */
    public List<ProjectDTO> findByStatus(ProjectStatus status) {
        if (status == null) throw new IllegalArgumentException("Tham số 'status' không được null");
        return projectMapper.findByStatus(status).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /** Lọc dự án của nhân viên theo trạng thái */
    public List<ProjectDTO> findByEmployeeAndStatus(String employeeId, ProjectStatus status) {
        requireNotBlank(employeeId, "employeeId");
        if (status == null) throw new IllegalArgumentException("Tham số 'status' không được null");
        return projectMapper.findByEmployeeIdAndStatus(employeeId, status).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────
    //  WRITE
    // ─────────────────────────────────────────────

    /** Tạo dự án mới */
    @Transactional
    public ProjectDTO create(ProjectDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Dữ liệu dự án không được null");
        employeeMapper.findById(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên", "id", dto.getEmployeeId()));

        Project project = toEntity(dto);
        project.setId(dto.getId() != null && !dto.getId().isBlank()
                ? dto.getId()
                : UUID.randomUUID().toString());
        projectMapper.insert(project);
        auditLogService.log("CREATE", "PROJECT", project.getId(), project.getName(),
                "employeeId=" + project.getEmployeeId());
        return toDTO(project);
    }

    /** Cập nhật dự án */
    @Transactional
    public ProjectDTO update(String id, ProjectDTO dto) {
        requireNotBlank(id, "id");
        if (dto == null) throw new IllegalArgumentException("Dữ liệu dự án không được null");
        Project existing = getOrThrow(id);

        if (!existing.getEmployeeId().equals(dto.getEmployeeId())) {
            employeeMapper.findById(dto.getEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Nhân viên", "id", dto.getEmployeeId()));
            existing.setEmployeeId(dto.getEmployeeId());
        }

        existing.setName(dto.getName());
        existing.setRole(dto.getRole());
        existing.setStartDate(dto.getStartDate());
        existing.setEndDate(dto.getEndDate());
        existing.setStatus(dto.getStatus());
        projectMapper.update(existing);
        auditLogService.log("UPDATE", "PROJECT", existing.getId(), existing.getName(), null);
        return toDTO(existing);
    }

    /** Xoá dự án */
    @Transactional
    public void delete(String id) {
        requireNotBlank(id, "id");
        Project p = getOrThrow(id);
        projectMapper.deleteById(id);
        auditLogService.log("DELETE", "PROJECT", id, p.getName(), null);
    }

    /** Xoá tất cả dự án của nhân viên */
    @Transactional
    public void deleteByEmployee(String employeeId) {
        requireNotBlank(employeeId, "employeeId");
        employeeMapper.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên", "id", employeeId));
        projectMapper.deleteByEmployeeId(employeeId);
    }

    /** Lấy tất cả assignment theo tên dự án */
    public List<ProjectDTO> findByProjectName(String name) {
        requireNotBlank(name, "name");
        return projectMapper.findByNameIgnoreCase(name).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /** Xoá toàn bộ dự án theo tên */
    @Transactional
    public void deleteByProjectName(String name) {
        requireNotBlank(name, "name");
        List<Project> projects = projectMapper.findByNameIgnoreCase(name);
        if (projects.isEmpty()) {
            throw new ResourceNotFoundException("Dự án", "name", name);
        }
        projectMapper.deleteByNameIgnoreCase(name);
    }

    /** Đổi tên dự án */
    @Transactional
    public void renameProject(String oldName, String newName) {
        requireNotBlank(oldName, "oldName");
        requireNotBlank(newName, "newName");
        List<Project> projects = projectMapper.findByNameIgnoreCase(oldName);
        if (projects.isEmpty()) {
            throw new ResourceNotFoundException("Dự án", "name", oldName);
        }
        projectMapper.updateNameByNameIgnoreCase(oldName, newName.trim());
        auditLogService.log("UPDATE", "PROJECT", null, newName.trim(),
                "Đổi tên từ '" + oldName + "'");
    }

    /** Cập nhật trạng thái dự án độc lập */
    @Transactional
    public void updateProjectStatus(String name, ProjectStatus status) {
        requireNotBlank(name, "name");
        if (status == null) throw new IllegalArgumentException("Tham số 'status' không được null");
        List<Project> projects = projectMapper.findByNameIgnoreCase(name);
        if (projects.isEmpty()) {
            throw new ResourceNotFoundException("Dự án", "name", name);
        }
        projectMapper.updateStatusByNameIgnoreCase(name, status);
    }

    // ─────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────

    private Project getOrThrow(String id) {
        return projectMapper.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dự án", "id", id));
    }

    public ProjectDTO toDTO(Project p) {
        return ProjectDTO.builder()
                .id(p.getId())
                .employeeId(p.getEmployeeId())
                .name(p.getName())
                .role(p.getRole())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .status(p.getStatus())
                .build();
    }

    private Project toEntity(ProjectDTO dto) {
        return Project.builder()
                .id(dto.getId())
                .employeeId(dto.getEmployeeId())
                .name(dto.getName())
                .role(dto.getRole())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .status(dto.getStatus())
                .build();
    }

    /** Kiểm tra tham số String không được null hoặc blank */
    private static void requireNotBlank(String value, String param) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Tham số '" + param + "' không được để trống");
    }
}
