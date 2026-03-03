package com.eflow.service;

import com.eflow.dto.DepartmentDTO;
import com.eflow.entity.Department;
import com.eflow.exception.ResourceNotFoundException;
import com.eflow.mapper.DepartmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentMapper departmentMapper;

    // ─────────────────────────────────────────────
    //  READ
    // ─────────────────────────────────────────────

    public List<DepartmentDTO> findAll() {
        return departmentMapper.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────
    //  WRITE
    // ─────────────────────────────────────────────

    @Transactional
    public DepartmentDTO create(DepartmentDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Dữ liệu không được null");
        String name = dto.getName().trim();
        if (name.isEmpty()) throw new IllegalArgumentException("Tên phòng ban không được để trống");

        if (departmentMapper.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Phòng ban '" + name + "' đã tồn tại");
        }

        int nextOrder = departmentMapper.maxSortOrder() + 1;
        Department entity = Department.builder()
                .name(name)
                .sortOrder(nextOrder)
                .build();
        departmentMapper.insert(entity);
        return toDTO(entity);
    }

    @Transactional
    public DepartmentDTO rename(Long id, String newName) {
        if (newName == null) throw new IllegalArgumentException("Tên phòng ban không được null");
        newName = newName.trim();
        if (newName.isEmpty()) throw new IllegalArgumentException("Tên phòng ban không được để trống");

        Department existing = departmentMapper.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Phòng ban", "id", id));

        if (!existing.getName().equalsIgnoreCase(newName) &&
                departmentMapper.existsByNameIgnoreCaseAndIdNot(newName, id)) {
            throw new IllegalArgumentException("Tên phòng ban '" + newName + "' đã tồn tại");
        }

        existing.setName(newName);
        departmentMapper.update(existing);
        return toDTO(existing);
    }

    @Transactional
    public void delete(Long id) {
        departmentMapper.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Phòng ban", "id", id));
        departmentMapper.deleteById(id);
    }

    /**
     * Seed: nhận danh sách tên phòng ban từ import nhân viên,
     * chỉ thêm những cái chưa tồn tại trong DB.
     */
    @Transactional
    public List<DepartmentDTO> seed(List<String> names) {
        if (names != null) {
            for (String raw : names) {
                if (raw == null || raw.isBlank()) continue;
                String name = raw.trim();
                if (!departmentMapper.existsByNameIgnoreCase(name)) {
                    int nextOrder = departmentMapper.maxSortOrder() + 1;
                    departmentMapper.insert(Department.builder()
                            .name(name)
                            .sortOrder(nextOrder)
                            .build());
                }
            }
        }
        return findAll();
    }

    // ─────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────

    private DepartmentDTO toDTO(Department d) {
        return DepartmentDTO.builder()
                .id(d.getId())
                .name(d.getName())
                .sortOrder(d.getSortOrder())
                .build();
    }
}
