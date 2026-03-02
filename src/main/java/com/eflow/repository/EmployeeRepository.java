package com.eflow.repository;

import com.eflow.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {

    /** Tìm nhân viên theo phòng ban */
    List<Employee> findByDepartmentIgnoreCase(String department);

    /** Tìm nhân viên theo quản lý trực tiếp (cấp dưới 1 bậc) */
    List<Employee> findByManagerId(String managerId);

    /** Tìm tất cả nhân viên gốc (không có quản lý) */
    List<Employee> findByManagerIdIsNull();

    /** Tìm kiếm theo tên, chức vụ hoặc phòng ban */
    @Query("SELECT e FROM Employee e WHERE " +
           "LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.position) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.department) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Employee> searchByKeyword(@Param("keyword") String keyword);

    /** Kiểm tra email đã tồn tại chưa (ngoại trừ ID hiện tại) */
    boolean existsByEmailAndIdNot(String email, String id);

    /** Kiểm tra email đã tồn tại chưa */
    boolean existsByEmail(String email);

    /** Đếm số nhân viên cấp dưới trực tiếp */
    long countByManagerId(String managerId);

    /** Tìm theo ID kèm eager-load projects */
    @Query("SELECT DISTINCT e FROM Employee e LEFT JOIN FETCH e.projects WHERE e.id = :id")
    Optional<Employee> findByIdWithProjects(@Param("id") String id);

    /** Tìm tất cả nhân viên kèm eager-load projects */
    @Query("SELECT DISTINCT e FROM Employee e LEFT JOIN FETCH e.projects")
    List<Employee> findAllWithProjects();
}
