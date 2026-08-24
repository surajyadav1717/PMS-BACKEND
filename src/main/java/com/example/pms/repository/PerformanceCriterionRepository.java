package com.example.pms.repository;

import com.example.pms.entity.PerformanceCriterion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface PerformanceCriterionRepository extends JpaRepository<PerformanceCriterion, Long> { List<PerformanceCriterion> findByActiveTrue(); }