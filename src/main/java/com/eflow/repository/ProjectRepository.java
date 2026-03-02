package com.eflow.repository;

import com.eflow.entity.Project;
import com.eflow.entity.Project.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {

    /** Lấy tất cả dự án của một nhân viên */
    List<Project> findByEmployeeId(String employeeId);

    /** Lọc dự án theo trạng thái */
    List<Project> findByStatus(ProjectStatus status);

    /** Lọc dự án của nhân viên theo trạng thái */
    List<Project> findByEmployeeIdAndStatus(String employeeId, ProjectStatus status);

    /** Xoá tất cả dự án của nhân viên */
    void deleteByEmployeeId(String employeeId);

    /** Tìm tất cả assignment theo tên dự án */
    List<Project> findByNameIgnoreCase(String name);

    /** Xoá tất cả assignment theo tên dự án */
    void deleteByNameIgnoreCase(String name);
}
