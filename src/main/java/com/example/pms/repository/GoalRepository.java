package com.example.pms.repository;

import com.example.pms.entity.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface GoalRepository extends JpaRepository<Goal, Long> { List<Goal> findByEmployeeId(Long employeeId); }