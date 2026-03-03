package com.eflow.mapper;

import com.eflow.entity.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface EmployeeMapper {

    /** Lấy tất cả nhân viên */
    List<Employee> findAll();

    /** Lấy nhân viên theo ID */
    Optional<Employee> findById(@Param("id") String id);

    /** Tìm nhân viên theo phòng ban */
    List<Employee> findByDepartmentIgnoreCase(@Param("department") String department);

    /** Lấy tất cả nhân viên không có quản lý (gốc) */
    List<Employee> findByManagerIdIsNull();

    /** Lấy cấp dưới trực tiếp */
    List<Employee> findByManagerId(@Param("managerId") String managerId);

    /** Tìm kiếm theo từ khoá (tên / chức vụ / phòng ban) */
    List<Employee> searchByKeyword(@Param("keyword") String keyword);

    /** Kiểm tra email tồn tại */
    boolean existsByEmail(@Param("email") String email);

    /** Kiểm tra email tồn tại ngoại trừ ID hiện tại */
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") String id);

    /** Đếm số cấp dưới trực tiếp */
    long countByManagerId(@Param("managerId") String managerId);

    /** Lấy nhiều nhân viên theo danh sách ID */
    List<Employee> findAllByIds(@Param("ids") List<String> ids);

    /** Insert nhân viên mới */
    int insert(Employee employee);

    /** Update nhân viên */
    int update(Employee employee);

    /** Xóa nhân viên theo ID */
    int deleteById(@Param("id") String id);

    /** Cập nhật managerId cho nhân viên */
    int updateManagerId(@Param("id") String id, @Param("managerId") String managerId);
}
