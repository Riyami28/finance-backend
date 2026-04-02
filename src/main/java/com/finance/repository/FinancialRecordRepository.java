package com.finance.repository;

import com.finance.model.FinancialRecord;
import com.finance.model.FinancialRecord.RecordType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long> {

    // ---- Filtered listing (soft-delete aware) ----
    @Query("SELECT r FROM FinancialRecord r WHERE r.deleted = false " +
           "AND (:type IS NULL OR r.type = :type) " +
           "AND (:category IS NULL OR r.category = :category) " +
           "AND (:startDate IS NULL OR r.date >= :startDate) " +
           "AND (:endDate IS NULL OR r.date <= :endDate)")
    Page<FinancialRecord> findFiltered(
            @Param("type") RecordType type,
            @Param("category") String category,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    // ---- Dashboard aggregations ----
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinancialRecord r " +
           "WHERE r.deleted = false AND r.type = :type")
    BigDecimal sumByType(@Param("type") RecordType type);

    @Query("SELECT r.category, SUM(r.amount) FROM FinancialRecord r " +
           "WHERE r.deleted = false AND r.type = :type " +
           "GROUP BY r.category ORDER BY SUM(r.amount) DESC")
    List<Object[]> sumByCategoryAndType(@Param("type") RecordType type);

    @Query("SELECT FUNCTION('TO_CHAR', r.date, 'YYYY-MM'), r.type, SUM(r.amount) " +
           "FROM FinancialRecord r WHERE r.deleted = false " +
           "AND r.date >= :since " +
           "GROUP BY FUNCTION('TO_CHAR', r.date, 'YYYY-MM'), r.type " +
           "ORDER BY FUNCTION('TO_CHAR', r.date, 'YYYY-MM')")
    List<Object[]> monthlyTrends(@Param("since") LocalDate since);

    // Recent activity
    @Query("SELECT r FROM FinancialRecord r WHERE r.deleted = false ORDER BY r.createdAt DESC")
    List<FinancialRecord> findRecentActivity(Pageable pageable);

    // Count active
    long countByDeletedFalse();
}
