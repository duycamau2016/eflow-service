package com.eflow.mapper;

import com.eflow.entity.ProjectPhase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ProjectPhaseMapper {

    Optional<ProjectPhase> findById(@Param("id") Long id);

    List<ProjectPhase> findByProjectNameIgnoreCaseOrderBySortOrderAsc(@Param("projectName") String projectName);

    int insert(ProjectPhase phase);

    int update(ProjectPhase phase);

    int deleteById(@Param("id") Long id);

    int deleteByProjectNameIgnoreCase(@Param("projectName") String projectName);
}
