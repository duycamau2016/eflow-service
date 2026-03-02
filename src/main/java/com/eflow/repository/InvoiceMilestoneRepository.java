package com.eflow.repository;

import com.eflow.entity.InvoiceMilestone;
import com.eflow.entity.InvoiceMilestone.MilestoneStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface InvoiceMilestoneRepository extends JpaRepository<InvoiceMilestone, Long> {

    /** Lấy tất cả mốc của một dự án, sắp xếp theo sortOrder */
    List<InvoiceMilestone> findByProjectNameIgnoreCaseOrderBySortOrderAsc(String projectName);

    /** Xóa tất cả mốc của một dự án */
    void deleteByProjectNameIgnoreCase(String projectName);

    /** Tổng giá trị hóa đơn đã invoiced hoặc paid của một dự án */
    @Query("SELECT COALESCE(SUM(m.amount), 0) FROM InvoiceMilestone m " +
           "WHERE LOWER(m.projectName) = LOWER(:projectName) " +
           "AND m.status IN ('invoiced', 'paid')")
    BigDecimal sumInvoicedAmountByProject(String projectName);

    /** Tổng giá trị đã thanh toán (paid) của một dự án */
    @Query("SELECT COALESCE(SUM(m.amount), 0) FROM InvoiceMilestone m " +
           "WHERE LOWER(m.projectName) = LOWER(:projectName) " +
           "AND m.status = 'paid'")
    BigDecimal sumPaidAmountByProject(String projectName);
}
