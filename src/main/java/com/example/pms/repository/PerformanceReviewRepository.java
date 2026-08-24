package com.example.pms.repository;

import com.example.pms.entity.PerformanceReview;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {
 List<PerformanceReview> findByReviewerId(Long reviewerId);
 List<PerformanceReview> findByEmployeeId(Long employeeId);
 Optional<PerformanceReview> findByEmployeeIdAndReviewCycleId(Long employeeId, Long cycleId);
 List<PerformanceReview> findByEmployee_Head_Id(Long headId);

}