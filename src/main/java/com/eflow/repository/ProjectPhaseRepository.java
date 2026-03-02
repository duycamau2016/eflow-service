package com.eflow.repository;

import com.eflow.entity.ProjectPhase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectPhaseRepository extends JpaRepository<ProjectPhase, Long> {

    /** Lấy tất cả phase của một dự án, sắp xếp theo sortOrder */
    List<ProjectPhase> findByProjectNameIgnoreCaseOrderBySortOrderAsc(String projectName);

    /** Xóa tất cả phase của một dự án */
    void deleteByProjectNameIgnoreCase(String projectName);
}
