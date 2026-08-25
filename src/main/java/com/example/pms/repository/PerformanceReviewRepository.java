package com.example.pms.repository;

import com.example.pms.entity.PerformanceReview;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {
 List<PerformanceReview> findByReviewerId(Long reviewerId);
 List<PerformanceReview> findByEmployeeId(Long employeeId);
 Optional<PerformanceReview> findByEmployeeIdAndReviewCycleId(Long employeeId, Long cycleId);
 List<PerformanceReview> findByEmployee_Head_Id(Long headId);


 @Query("""
    SELECT r
    FROM PerformanceReview r
    JOIN FETCH r.employee e
    LEFT JOIN FETCH e.head
    WHERE r.id = :id
""")
 Optional<PerformanceReview> findByIdWithEmployeeAndHead(@Param("id") Long id);
}