package com.example.pms.repository;

import com.example.pms.entity.ReviewCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ReviewCycleRepository extends JpaRepository<ReviewCycle, Long> {

    List<ReviewCycle> findByStatus(String status);

}