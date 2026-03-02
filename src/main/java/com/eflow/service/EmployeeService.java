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
        Employee emp = employeeRepository.findByIdWithProjects(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên", "id", id));
        return toDTO(emp, true);
    }

    /** Tìm kiếm nhân viên theo từ khoá (tên / chức vụ / phòng ban) */
    public List<EmployeeDTO> search(String keyword) {
        return employeeRepository.searchByKeyword(keyword).stream()
                .map(e -> toDTO(e, false))
                .collect(Collectors.toList());
    }

    /** Lấy nhân viên theo phòng ban */
    public List<EmployeeDTO> findByDepartment(String department) {
        return employeeRepository.findByDepartmentIgnoreCase(department).stream()
                .map(e -> toDTO(e, false))
                .collect(Collectors.toList());
    }

    /** Lấy danh sách nhân viên cấp dưới trực tiếp */
    public List<EmployeeDTO> findSubordinates(String managerId) {
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
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên", "id", id));

        // Gán lại managerId cho cấp dưới
        List<Employee> subordinates = employeeRepository.findByManagerId(id);
        subordinates.forEach(sub -> sub.setManagerId(emp.getManagerId()));
        employeeRepository.saveAll(subordinates);

        employeeRepository.delete(emp);
    }

    /** Xoá toàn bộ dữ liệu cũ và import lại từ danh sách DTO (dùng sau Excel parse) */
    @Transactional
    public void bulkImport(List<EmployeeDTO> dtos) {
        // 1. Xoá dữ liệu cũ (projects trước vì có FK)
        projectRepository.deleteAllInBatch();
        employeeRepository.deleteAllInBatch();

        // 2. Lưu nhân viên
        List<Employee> employees = dtos.stream()
                .map(dto -> {
                    Employee e = toEntity(dto);
                    e.setLevel(dto.getLevel());
                    e.setProjects(new ArrayList<>());
                    return e;
                })
                .collect(Collectors.toList());
        employeeRepository.saveAll(employees);

        // 3. Lưu projects
        List<Project> projects = new ArrayList<>();
        for (EmployeeDTO dto : dtos) {
            if (dto.getProjects() == null || dto.getProjects().isEmpty()) continue;
            Employee emp = employeeRepository.findById(dto.getId()).orElse(null);
            if (emp == null) continue;
            for (ProjectDTO pdto : dto.getProjects()) {
                if (pdto.getId() == null || pdto.getId().isBlank()) continue;
                Project p = Project.builder()
                        .id(pdto.getId())
                        .employee(emp)
                        .name(pdto.getName())
                        .role(pdto.getRole() != null ? pdto.getRole() : "")
                        .startDate(pdto.getStartDate())
                        .endDate(pdto.getEndDate())
                        .status(pdto.getStatus() != null ? pdto.getStatus() : Project.ProjectStatus.active)
                        .build();
                projects.add(p);
            }
        }
        projectRepository.saveAll(projects);
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
}
