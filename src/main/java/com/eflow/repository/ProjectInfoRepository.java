package com.eflow.repository;

import com.eflow.entity.ProjectInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectInfoRepository extends JpaRepository<ProjectInfo, Long> {

    /** Tìm theo tên dự án */
    Optional<ProjectInfo> findByProjectNameIgnoreCase(String projectName);

    /** Kiểm tra tồn tại theo tên dự án */
    boolean existsByProjectNameIgnoreCase(String projectName);

    /** Kiểm tra tồn tại theo tên dự án, ngoại trừ id hiện tại (dùng khi update) */
    boolean existsByProjectNameIgnoreCaseAndIdNot(String projectName, Long id);
}
