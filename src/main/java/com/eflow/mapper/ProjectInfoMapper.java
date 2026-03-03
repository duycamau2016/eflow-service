package com.eflow.mapper;

import com.eflow.entity.ProjectInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ProjectInfoMapper {

    List<ProjectInfo> findAll();

    Optional<ProjectInfo> findById(@Param("id") Long id);

    Optional<ProjectInfo> findByProjectNameIgnoreCase(@Param("projectName") String projectName);

    boolean existsByProjectNameIgnoreCase(@Param("projectName") String projectName);

    boolean existsByProjectNameIgnoreCaseAndIdNot(@Param("projectName") String projectName,
                                                  @Param("id") Long id);

    int insert(ProjectInfo projectInfo);

    int update(ProjectInfo projectInfo);

    int deleteById(@Param("id") Long id);
}
