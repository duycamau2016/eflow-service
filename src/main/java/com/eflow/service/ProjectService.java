package com.eflow.service;

import com.eflow.dto.ProjectDTO;
import com.eflow.entity.Employee;
import com.eflow.entity.Project;
import com.eflow.entity.Project.ProjectStatus;
import com.eflow.exception.ResourceNotFoundException;
import com.eflow.repository.EmployeeRepository;
import com.eflow.repository.ProjectRepository;
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

    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;

    // ─────────────────────────────────────────────
    //  READ
    // ─────────────────────────────────────────────

    /** Lấy tất cả dự án */
    public List<ProjectDTO> findAll() {
        return projectRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /** Lấy dự án theo ID */
    public ProjectDTO findById(String id) {
        return toDTO(getOrThrow(id));
    }

    /** Lấy tất cả dự án của một nhân viên */
    public List<ProjectDTO> findByEmployee(String employeeId) {
        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên", "id", employeeId));
        return projectRepository.findByEmployeeId(employeeId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /** Lọc dự án theo trạng thái */
    public List<ProjectDTO> findByStatus(ProjectStatus status) {
        return projectRepository.findByStatus(status).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /** Lọc dự án của nhân viên theo trạng thái */
    public List<ProjectDTO> findByEmployeeAndStatus(String employeeId, ProjectStatus status) {
        return projectRepository.findByEmployeeIdAndStatus(employeeId, status).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────
    //  WRITE
    // ─────────────────────────────────────────────

    /** Tạo dự án mới */
    @Transactional
    public ProjectDTO create(ProjectDTO dto) {
        Employee emp = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên", "id", dto.getEmployeeId()));

        Project project = toEntity(dto, emp);
        project.setId(dto.getId() != null && !dto.getId().isBlank()
                ? dto.getId()
                : UUID.randomUUID().toString());

        return toDTO(projectRepository.save(project));
    }

    /** Cập nhật dự án */
    @Transactional
    public ProjectDTO update(String id, ProjectDTO dto) {
        Project existing = getOrThrow(id);

        // Cho phép thay đổi nhân viên tham gia
        if (!existing.getEmployee().getId().equals(dto.getEmployeeId())) {
            Employee newEmp = employeeRepository.findById(dto.getEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Nhân viên", "id", dto.getEmployeeId()));
            existing.setEmployee(newEmp);
        }

        existing.setName(dto.getName());
        existing.setRole(dto.getRole());
        existing.setStartDate(dto.getStartDate());
        existing.setEndDate(dto.getEndDate());
        existing.setStatus(dto.getStatus());

        return toDTO(projectRepository.save(existing));
    }

    /** Xoá dự án */
    @Transactional
    public void delete(String id) {
        projectRepository.delete(getOrThrow(id));
    }

    /** Xoá tất cả dự án của nhân viên */
    @Transactional
    public void deleteByEmployee(String employeeId) {
        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên", "id", employeeId));
        projectRepository.deleteByEmployeeId(employeeId);
    }

    /** Lấy tất cả assignment theo tên dự án */
    public List<ProjectDTO> findByProjectName(String name) {
        return projectRepository.findByNameIgnoreCase(name).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /** Xoá toàn bộ dự án theo tên (xoá dự án + tất cả thành viên) */
    @Transactional
    public void deleteByProjectName(String name) {
        List<Project> projects = projectRepository.findByNameIgnoreCase(name);
        if (projects.isEmpty()) {
            throw new ResourceNotFoundException("Dự án", "name", name);
        }
        projectRepository.deleteAll(projects);
    }

    /** Đổi tên dự án (cập nhật tên cho tất cả assignment) */
    @Transactional
    public void renameProject(String oldName, String newName) {
        List<Project> projects = projectRepository.findByNameIgnoreCase(oldName);
        if (projects.isEmpty()) {
            throw new ResourceNotFoundException("Dự án", "name", oldName);
        }
        projects.forEach(p -> p.setName(newName.trim()));
        projectRepository.saveAll(projects);
    }

    // ─────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────

    private Project getOrThrow(String id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dự án", "id", id));
    }

    public ProjectDTO toDTO(Project p) {
        return ProjectDTO.builder()
                .id(p.getId())
                .employeeId(p.getEmployee().getId())
                .name(p.getName())
                .role(p.getRole())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .status(p.getStatus())
                .build();
    }

    private Project toEntity(ProjectDTO dto, Employee emp) {
        return Project.builder()
                .id(dto.getId())
                .employee(emp)
                .name(dto.getName())
                .role(dto.getRole())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .status(dto.getStatus())
                .build();
    }
}
