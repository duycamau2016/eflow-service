package com.eflow.service;

import com.eflow.dto.EmployeeDTO;
import com.eflow.dto.OrgNodeDTO;
import com.eflow.dto.ProjectDTO;
import com.eflow.entity.Employee;
import com.eflow.entity.Project;
import com.eflow.exception.DuplicateResourceException;
import com.eflow.exception.ResourceNotFoundException;
import com.eflow.repository.EmployeeRepository;
import com.eflow.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;

    // ─────────────────────────────────────────────
    //  READ
    // ─────────────────────────────────────────────

    /** Lấy tất cả nhân viên kèm danh sách dự án */
    public List<EmployeeDTO> findAll() {
        return employeeRepository.findAllWithProjects().stream()
                .map(e -> toDTO(e, true))
                .collect(Collectors.toList());
    }

    /** Lấy nhân viên theo ID kèm danh sách dự án */
    public EmployeeDTO findById(String id) {
        requireNotBlank(id, "id");
        Employee emp = employeeRepository.findByIdWithProjects(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên", "id", id));
        return toDTO(emp, true);
    }

    /** Tìm kiếm nhân viên theo từ khoá (tên / chức vụ / phòng ban) */
    public List<EmployeeDTO> search(String keyword) {
        requireNotBlank(keyword, "keyword");
        return employeeRepository.searchByKeyword(keyword).stream()
                .map(e -> toDTO(e, false))
                .collect(Collectors.toList());
    }

    /** Lấy nhân viên theo phòng ban */
    public List<EmployeeDTO> findByDepartment(String department) {
        requireNotBlank(department, "department");
        return employeeRepository.findByDepartmentIgnoreCase(department).stream()
                .map(e -> toDTO(e, false))
                .collect(Collectors.toList());
    }

    /** Lấy danh sách nhân viên cấp dưới trực tiếp */
    public List<EmployeeDTO> findSubordinates(String managerId) {
        requireNotBlank(managerId, "managerId");
        // validate manager exists
        employeeRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên", "id", managerId));
        return employeeRepository.findByManagerId(managerId).stream()
                .map(e -> toDTO(e, false))
                .collect(Collectors.toList());
    }

    /**
     * Xây dựng cây sơ đồ tổ chức từ danh sách nhân viên.
     * Trả về danh sách các node gốc (không có quản lý).
     */
    public List<OrgNodeDTO> buildOrgTree() {
        List<Employee> allEmployees = employeeRepository.findAll();

        // Map employeeId -> danh sách cấp dưới
        Map<String, List<Employee>> childrenMap = allEmployees.stream()
                .filter(e -> e.getManagerId() != null)
                .collect(Collectors.groupingBy(Employee::getManagerId));

        // Đếm số cấp dưới cho từng nhân viên
        Map<String, Long> subordinateCount = allEmployees.stream()
                .filter(e -> e.getManagerId() != null)
                .collect(Collectors.groupingBy(Employee::getManagerId, Collectors.counting()));

        // Lấy các node gốc
        List<Employee> roots = employeeRepository.findByManagerIdIsNull();

        return roots.stream()
                .map(root -> buildNode(root, childrenMap, subordinateCount))
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────
    //  WRITE
    // ─────────────────────────────────────────────

    /** Tạo nhân viên mới */
    @Transactional
    public EmployeeDTO create(EmployeeDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Dữ liệu nhân viên không được null");
        if (dto.getEmail() != null && employeeRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email '" + dto.getEmail() + "' đã tồn tại");
        }
        // Validate manager tồn tại nếu có
        if (dto.getManagerId() != null && !dto.getManagerId().isBlank()) {
            employeeRepository.findById(dto.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Quản lý", "id", dto.getManagerId()));
        }

        Employee entity = toEntity(dto);
        entity.setId(dto.getId() != null && !dto.getId().isBlank()
                ? dto.getId()
                : UUID.randomUUID().toString());
        entity.setLevel(computeLevel(dto.getManagerId()));
        entity.setProjects(new ArrayList<>());

        return toDTO(employeeRepository.save(entity), false);
    }

    /** Cập nhật thông tin nhân viên */
    @Transactional
    public EmployeeDTO update(String id, EmployeeDTO dto) {
        requireNotBlank(id, "id");
        if (dto == null) throw new IllegalArgumentException("Dữ liệu nhân viên không được null");
        Employee existing = employeeRepository.findByIdWithProjects(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên", "id", id));

        if (dto.getEmail() != null && employeeRepository.existsByEmailAndIdNot(dto.getEmail(), id)) {
            throw new DuplicateResourceException("Email '" + dto.getEmail() + "' đã tồn tại");
        }
        if (dto.getManagerId() != null && !dto.getManagerId().isBlank()) {
            if (dto.getManagerId().equals(id)) {
                throw new IllegalArgumentException("Nhân viên không thể là quản lý của chính mình");
            }
            employeeRepository.findById(dto.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Quản lý", "id", dto.getManagerId()));
        }

        existing.setName(dto.getName());
        existing.setPosition(dto.getPosition());
        existing.setDepartment(dto.getDepartment());
        existing.setEmail(dto.getEmail());
        existing.setPhone(dto.getPhone());
        existing.setManagerId(dto.getManagerId());
        existing.setAvatar(dto.getAvatar());
        existing.setJoinDate(dto.getJoinDate());
        existing.setLevel(computeLevel(dto.getManagerId()));

        return toDTO(employeeRepository.save(existing), true);
    }

    /** Xoá nhân viên */
    @Transactional
    public void delete(String id) {
        requireNotBlank(id, "id");
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên", "id", id));

        // Gán lại managerId cho cấp dưới
        List<Employee> subordinates = employeeRepository.findByManagerId(id);
        subordinates.forEach(sub -> sub.setManagerId(emp.getManagerId()));
        employeeRepository.saveAll(subordinates);

        employeeRepository.delete(emp);
    }

    /**
     * Upsert danh sách nhân viên từ file Excel:
     * - Nếu ID đã tồn tại → update thông tin (giữ nguyên dữ liệu thủ công không có trong file)
     * - Nếu ID chưa tồn tại → insert mới
     * - Nhân viên không có trong file → KHÔNG xoá
     * - Projects của từng nhân viên trong file → upsert tương tự
     */
    @Transactional
    public void bulkImport(List<EmployeeDTO> dtos) {
        if (dtos == null) throw new IllegalArgumentException("Danh sách nhân viên không được null");

        // ── 1. Upsert Employees ──────────────────────────────────────────
        Set<String> incomingIds = dtos.stream()
                .map(EmployeeDTO::getId)
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toSet());

        // Load tất cả employee đang có trong DB (1 lần duy nhất)
        Map<String, Employee> existingMap = employeeRepository.findAllById(incomingIds)
                .stream().collect(Collectors.toMap(Employee::getId, e -> e));

        List<Employee> toSave = new ArrayList<>();
        for (EmployeeDTO dto : dtos) {
            if (dto.getId() == null || dto.getId().isBlank()) continue;

            Employee emp = existingMap.get(dto.getId());
            if (emp != null) {
                // UPDATE: chỉ ghi đè các field có trong file
                emp.setName(dto.getName());
                emp.setPosition(dto.getPosition());
                emp.setDepartment(dto.getDepartment());
                emp.setEmail(dto.getEmail());
                emp.setPhone(dto.getPhone());
                emp.setManagerId(dto.getManagerId());
                emp.setAvatar(dto.getAvatar());
                emp.setJoinDate(dto.getJoinDate());
                emp.setLevel(dto.getLevel() != null ? dto.getLevel() : computeLevel(dto.getManagerId()));
            } else {
                // INSERT: tạo mới
                emp = toEntity(dto);
                emp.setLevel(dto.getLevel() != null ? dto.getLevel() : computeLevel(dto.getManagerId()));
                emp.setProjects(new ArrayList<>());
            }
            toSave.add(emp);
        }
        employeeRepository.saveAll(toSave);

        // ── 2. Upsert Projects ───────────────────────────────────────────
        for (EmployeeDTO dto : dtos) {
            if (dto.getProjects() == null || dto.getProjects().isEmpty()) continue;
            Employee emp = employeeRepository.findById(dto.getId()).orElse(null);
            if (emp == null) continue;

            // Lấy các project ID từ file
            Set<String> incomingProjectIds = dto.getProjects().stream()
                    .map(ProjectDTO::getId)
                    .filter(id -> id != null && !id.isBlank())
                    .collect(Collectors.toSet());

            Map<String, Project> existingProjects = projectRepository.findAllById(incomingProjectIds)
                    .stream().collect(Collectors.toMap(Project::getId, p -> p));

            List<Project> projectsToSave = new ArrayList<>();
            for (ProjectDTO pdto : dto.getProjects()) {
                if (pdto.getId() == null || pdto.getId().isBlank()) continue;

                Project p = existingProjects.get(pdto.getId());
                if (p != null) {
                    // UPDATE project
                    p.setName(pdto.getName());
                    p.setRole(pdto.getRole() != null ? pdto.getRole() : p.getRole());
                    p.setStartDate(pdto.getStartDate());
                    p.setEndDate(pdto.getEndDate());
                    p.setStatus(pdto.getStatus() != null ? pdto.getStatus() : p.getStatus());
                } else {
                    // INSERT project
                    p = Project.builder()
                            .id(pdto.getId())
                            .employee(emp)
                            .name(pdto.getName())
                            .role(pdto.getRole() != null ? pdto.getRole() : "")
                            .startDate(pdto.getStartDate())
                            .endDate(pdto.getEndDate())
                            .status(pdto.getStatus() != null ? pdto.getStatus() : Project.ProjectStatus.active)
                            .build();
                }
                projectsToSave.add(p);
            }
            projectRepository.saveAll(projectsToSave);
        }
    }

    // ─────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────

    private OrgNodeDTO buildNode(Employee emp,
                                  Map<String, List<Employee>> childrenMap,
                                  Map<String, Long> subordinateCount) {
        EmployeeDTO dto = toDTO(emp, false);
        dto.setSubordinatesCount(subordinateCount.getOrDefault(emp.getId(), 0L).intValue());

        List<OrgNodeDTO> childNodes = childrenMap.getOrDefault(emp.getId(), List.of())
                .stream()
                .map(child -> buildNode(child, childrenMap, subordinateCount))
                .collect(Collectors.toList());

        return OrgNodeDTO.builder()
                .employee(dto)
                .children(childNodes)
                .build();
    }

    private int computeLevel(String managerId) {
        if (managerId == null || managerId.isBlank()) return 0;
        return employeeRepository.findById(managerId)
                .map(m -> m.getLevel() + 1)
                .orElse(0);
    }

    /** Entity → DTO */
    public EmployeeDTO toDTO(Employee emp, boolean includeProjects) {
        EmployeeDTO dto = EmployeeDTO.builder()
                .id(emp.getId())
                .name(emp.getName())
                .position(emp.getPosition())
                .department(emp.getDepartment())
                .email(emp.getEmail())
                .phone(emp.getPhone())
                .managerId(emp.getManagerId())
                .avatar(emp.getAvatar())
                .level(emp.getLevel())
                .joinDate(emp.getJoinDate())
                .subordinatesCount((int) employeeRepository.countByManagerId(emp.getId()))
                .build();

        if (includeProjects && emp.getProjects() != null) {
            dto.setProjects(emp.getProjects().stream()
                    .map(this::projectToDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private ProjectDTO projectToDTO(Project p) {
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

    /** DTO → Entity (không bao gồm projects) */
    private Employee toEntity(EmployeeDTO dto) {
        return Employee.builder()
                .id(dto.getId())
                .name(dto.getName())
                .position(dto.getPosition())
                .department(dto.getDepartment())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .managerId(dto.getManagerId())
                .avatar(dto.getAvatar())
                .level(dto.getLevel())
                .joinDate(dto.getJoinDate())
                .build();
    }

    /** Kiểm tra tham số String không được null hoặc blank */
    private static void requireNotBlank(String value, String param) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Tham số '" + param + "' không được để trống");
    }
}
