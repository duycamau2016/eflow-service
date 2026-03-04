package com.eflow.service;

import com.eflow.dto.EmployeeDTO;
import com.eflow.dto.OrgNodeDTO;
import com.eflow.dto.ProjectDTO;
import com.eflow.entity.Employee;
import com.eflow.entity.Project;
import com.eflow.exception.DuplicateResourceException;
import com.eflow.exception.ResourceNotFoundException;
import com.eflow.mapper.EmployeeMapper;
import com.eflow.mapper.ProjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeMapper     employeeMapper;
    private final ProjectMapper      projectMapper;
    private final AuditLogService    auditLogService;

    // ─────────────────────────────────────────────
    //  READ
    // ─────────────────────────────────────────────

    /** Lay tat ca nhan vien kem danh sach du an */
    public List<EmployeeDTO> findAll() {
        List<Employee> employees = employeeMapper.findAll();
        employees.forEach(e -> e.setProjects(projectMapper.findByEmployeeId(e.getId())));
        return employees.stream()
                .map(e -> toDTO(e, true))
                .collect(Collectors.toList());
    }

    /** Lay nhan vien theo ID kem danh sach du an */
    public EmployeeDTO findById(String id) {
        requireNotBlank(id, "id");
        Employee emp = employeeMapper.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nhan vien", "id", id));
        emp.setProjects(projectMapper.findByEmployeeId(id));
        return toDTO(emp, true);
    }

    /** Tim kiem nhan vien theo tu khoa */
    public List<EmployeeDTO> search(String keyword) {
        requireNotBlank(keyword, "keyword");
        return employeeMapper.searchByKeyword(keyword).stream()
                .map(e -> toDTO(e, false))
                .collect(Collectors.toList());
    }

    /** Lay nhan vien theo phong ban */
    public List<EmployeeDTO> findByDepartment(String department) {
        requireNotBlank(department, "department");
        return employeeMapper.findByDepartmentIgnoreCase(department).stream()
                .map(e -> toDTO(e, false))
                .collect(Collectors.toList());
    }

    /** Lay danh sach nhan vien cap duoi truc tiep */
    public List<EmployeeDTO> findSubordinates(String managerId) {
        requireNotBlank(managerId, "managerId");
        employeeMapper.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("Nhan vien", "id", managerId));
        return employeeMapper.findByManagerId(managerId).stream()
                .map(e -> toDTO(e, false))
                .collect(Collectors.toList());
    }

    /** Xay dung cay so do to chuc */
    public List<OrgNodeDTO> buildOrgTree() {
        List<Employee> allEmployees = employeeMapper.findAll();

        Map<String, List<Employee>> childrenMap = allEmployees.stream()
                .filter(e -> e.getManagerId() != null)
                .collect(Collectors.groupingBy(Employee::getManagerId));

        Map<String, Long> subordinateCount = allEmployees.stream()
                .filter(e -> e.getManagerId() != null)
                .collect(Collectors.groupingBy(Employee::getManagerId, Collectors.counting()));

        List<Employee> roots = employeeMapper.findByManagerIdIsNull();

        return roots.stream()
                .map(root -> buildNode(root, childrenMap, subordinateCount))
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────
    //  WRITE
    // ─────────────────────────────────────────────

    /** Tao nhan vien moi */
    @Transactional
    public EmployeeDTO create(EmployeeDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Du lieu nhan vien khong duoc null");
        if (dto.getEmail() != null && employeeMapper.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email '" + dto.getEmail() + "' da ton tai");
        }
        if (dto.getManagerId() != null && !dto.getManagerId().isBlank()) {
            employeeMapper.findById(dto.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Quan ly", "id", dto.getManagerId()));
        }

        Employee entity = toEntity(dto);
        entity.setId(dto.getId() != null && !dto.getId().isBlank()
                ? dto.getId()
                : UUID.randomUUID().toString());
        entity.setLevel(computeLevel(dto.getManagerId()));

        employeeMapper.insert(entity);
        auditLogService.log("CREATE", "EMPLOYEE", entity.getId(), entity.getName(), null);
        return toDTO(entity, false);
    }

    /** Cap nhat thong tin nhan vien */
    @Transactional
    public EmployeeDTO update(String id, EmployeeDTO dto) {
        requireNotBlank(id, "id");
        if (dto == null) throw new IllegalArgumentException("Du lieu nhan vien khong duoc null");
        Employee existing = employeeMapper.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nhan vien", "id", id));

        if (dto.getEmail() != null && employeeMapper.existsByEmailAndIdNot(dto.getEmail(), id)) {
            throw new DuplicateResourceException("Email '" + dto.getEmail() + "' da ton tai");
        }
        if (dto.getManagerId() != null && !dto.getManagerId().isBlank()) {
            if (dto.getManagerId().equals(id)) {
                throw new IllegalArgumentException("Nhan vien khong the la quan ly cua chinh minh");
            }
            employeeMapper.findById(dto.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Quan ly", "id", dto.getManagerId()));
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
        employeeMapper.update(existing);
        auditLogService.log("UPDATE", "EMPLOYEE", existing.getId(), existing.getName(), null);
        existing.setProjects(projectMapper.findByEmployeeId(id));
        return toDTO(existing, true);
    }

    /** Xoa nhan vien */
    @Transactional
    public void delete(String id) {
        requireNotBlank(id, "id");
        Employee emp = employeeMapper.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nhan vien", "id", id));

        List<Employee> subordinates = employeeMapper.findByManagerId(id);
        subordinates.forEach(sub -> employeeMapper.updateManagerId(sub.getId(), emp.getManagerId()));

        employeeMapper.deleteById(id);
        auditLogService.log("DELETE", "EMPLOYEE", id, emp.getName(), null);
    }

    /** Upsert danh sach nhan vien tu file Excel */
    @Transactional
    public void bulkImport(List<EmployeeDTO> dtos) {
        if (dtos == null) throw new IllegalArgumentException("Danh sach nhan vien khong duoc null");

        List<String> incomingIds = dtos.stream()
                .map(EmployeeDTO::getId)
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toList());

        Map<String, Employee> existingMap = incomingIds.isEmpty()
                ? Collections.emptyMap()
                : employeeMapper.findAllByIds(incomingIds)
                        .stream().collect(Collectors.toMap(Employee::getId, e -> e));

        for (EmployeeDTO dto : dtos) {
            if (dto.getId() == null || dto.getId().isBlank()) continue;

            Employee emp = existingMap.get(dto.getId());
            if (emp != null) {
                emp.setName(dto.getName());
                emp.setPosition(dto.getPosition());
                emp.setDepartment(dto.getDepartment());
                emp.setEmail(dto.getEmail());
                emp.setPhone(dto.getPhone());
                emp.setManagerId(dto.getManagerId());
                emp.setAvatar(dto.getAvatar());
                emp.setJoinDate(dto.getJoinDate());
                emp.setLevel(dto.getLevel() > 0 ? dto.getLevel() : computeLevel(dto.getManagerId()));
                employeeMapper.update(emp);
            } else {
                emp = toEntity(dto);
                emp.setLevel(dto.getLevel() > 0 ? dto.getLevel() : computeLevel(dto.getManagerId()));
                employeeMapper.insert(emp);
            }
        }

        for (EmployeeDTO dto : dtos) {
            if (dto.getProjects() == null || dto.getProjects().isEmpty()) continue;
            if (employeeMapper.findById(dto.getId()).isEmpty()) continue;

            List<String> incomingProjectIds = dto.getProjects().stream()
                    .map(ProjectDTO::getId)
                    .filter(pid -> pid != null && !pid.isBlank())
                    .collect(Collectors.toList());

            Map<String, Project> existingProjects = incomingProjectIds.isEmpty()
                    ? Collections.emptyMap()
                    : projectMapper.findAllByIds(incomingProjectIds)
                            .stream().collect(Collectors.toMap(Project::getId, p -> p));

            for (ProjectDTO pdto : dto.getProjects()) {
                if (pdto.getId() == null || pdto.getId().isBlank()) continue;

                Project p = existingProjects.get(pdto.getId());
                if (p != null) {
                    p.setName(pdto.getName());
                    p.setRole(pdto.getRole() != null ? pdto.getRole() : p.getRole());
                    p.setStartDate(pdto.getStartDate());
                    p.setEndDate(pdto.getEndDate());
                    p.setStatus(pdto.getStatus() != null ? pdto.getStatus() : p.getStatus());
                    projectMapper.update(p);
                } else {
                    p = Project.builder()
                            .id(pdto.getId())
                            .employeeId(dto.getId())
                            .name(pdto.getName())
                            .role(pdto.getRole() != null ? pdto.getRole() : "")
                            .startDate(pdto.getStartDate())
                            .endDate(pdto.getEndDate())
                            .status(pdto.getStatus() != null ? pdto.getStatus() : Project.ProjectStatus.active)
                            .build();
                    projectMapper.insert(p);
                }
            }
        }
        auditLogService.log("IMPORT", "EMPLOYEE", null,
                "Import " + dtos.size() + " nhân viên", null);
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
        return employeeMapper.findById(managerId)
                .map(m -> m.getLevel() + 1)
                .orElse(0);
    }

    /** Entity to DTO */
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
                .subordinatesCount((int) employeeMapper.countByManagerId(emp.getId()))
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
                .employeeId(p.getEmployeeId())
                .name(p.getName())
                .role(p.getRole())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .status(p.getStatus())
                .build();
    }

    /** DTO to Entity */
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

    private static void requireNotBlank(String value, String param) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Tham so '" + param + "' khong duoc de trong");
    }
}