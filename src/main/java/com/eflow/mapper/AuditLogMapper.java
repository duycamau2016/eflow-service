package com.eflow.mapper;

import com.eflow.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AuditLogMapper {

    void insert(AuditLog log);

    List<AuditLog> findWithFilters(
            @Param("entityType") String entityType,
            @Param("action")     String action,
            @Param("actor")      String actor,
            @Param("limit")      int    limit,
            @Param("offset")     int    offset
    );

    int countWithFilters(
            @Param("entityType") String entityType,
            @Param("action")     String action,
            @Param("actor")      String actor
    );
}
