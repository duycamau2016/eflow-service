package com.eflow.mapper;

import com.eflow.entity.InvoiceMilestone;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Mapper
public interface InvoiceMilestoneMapper {

    Optional<InvoiceMilestone> findById(@Param("id") Long id);

    List<InvoiceMilestone> findByProjectNameIgnoreCaseOrderBySortOrderAsc(@Param("projectName") String projectName);

    /** Tổng giá trị đã invoiced hoặc paid */
    BigDecimal sumInvoicedAmountByProject(@Param("projectName") String projectName);

    /** Tổng giá trị đã paid */
    BigDecimal sumPaidAmountByProject(@Param("projectName") String projectName);

    int insert(InvoiceMilestone milestone);

    int update(InvoiceMilestone milestone);

    int deleteById(@Param("id") Long id);

    int deleteByProjectNameIgnoreCase(@Param("projectName") String projectName);
}
