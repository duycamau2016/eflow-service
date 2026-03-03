package com.eflow.mapper;

import com.eflow.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface DepartmentMapper {

    /** Lấy tất cả phòng ban, sắp xếp theo sort_order rồi name */
    List<Department> findAll();

    /** Tìm theo ID */
    Optional<Department> findById(@Param("id") Long id);

    /** Kiểm tra tên đã tồn tại (case-insensitive) */
    boolean existsByNameIgnoreCase(@Param("name") String name);

    /** Kiểm tra tên đã tồn tại ngoại trừ ID hiện tại */
    boolean existsByNameIgnoreCaseAndIdNot(@Param("name") String name, @Param("id") Long id);

    /** Lấy sort_order cao nhất hiện tại (để auto-increment khi thêm mới) */
    int maxSortOrder();

    /** Thêm phòng ban mới — tự điền generated id */
    int insert(Department department);

    /** Cập nhật phòng ban */
    int update(Department department);

    /** Xóa theo ID */
    int deleteById(@Param("id") Long id);
}
