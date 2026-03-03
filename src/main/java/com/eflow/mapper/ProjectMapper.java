package com.eflow.mapper;

import com.eflow.entity.Project;
import com.eflow.entity.Project.ProjectStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ProjectMapper {

    /** Lấy tất cả dự án */
    List<Project> findAll();

    /** Lấy dự án theo ID */
    Optional<Project> findById(@Param("id") String id);

    /** Lấy tất cả dự án của một nhân viên */
    List<Project> findByEmployeeId(@Param("employeeId") String employeeId);

    /** Lọc dự án theo trạng thái */
    List<Project> findByStatus(@Param("status") ProjectStatus status);

    /** Lọc dự án của nhân viên theo trạng thái */
    List<Project> findByEmployeeIdAndStatus(@Param("employeeId") String employeeId,
                                            @Param("status") ProjectStatus status);

    /** Tìm tất cả assignment theo tên dự án */
    List<Project> findByNameIgnoreCase(@Param("name") String name);

    /** Lấy nhiều project theo danh sách ID */
    List<Project> findAllByIds(@Param("ids") List<String> ids);

    /** Insert dự án mới */
    int insert(Project project);

    /** Update dự án */
    int update(Project project);

    /** Xóa dự án theo ID */
    int deleteById(@Param("id") String id);

    /** Xóa tất cả dự án của nhân viên */
    int deleteByEmployeeId(@Param("employeeId") String employeeId);

    /** Xóa tất cả assignment theo tên dự án */
    int deleteByNameIgnoreCase(@Param("name") String name);

    /** Đổi tên dự án (cập nhật tên cho tất cả assignment) */
    int updateNameByNameIgnoreCase(@Param("oldName") String oldName, @Param("newName") String newName);

    /** Cập nhật trạng thái cho tất cả assignment cùng tên */
    int updateStatusByNameIgnoreCase(@Param("name") String name, @Param("status") ProjectStatus status);
}
