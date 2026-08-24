package com.example.pms.entity;

import com.example.pms.domain.GoalStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "goals")
@Getter @Setter @NoArgsConstructor
public class Goal {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    private String title;
    @Column(columnDefinition = "TEXT") private String description;
    private LocalDate startDate;
    private LocalDate targetDate;
    private Double weight;
    private Integer progressPercentage = 0;
    @Enumerated(EnumType.STRING) private GoalStatus status = GoalStatus.IN_PROGRESS;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "created_by")
    private Employee createdBy;
    private LocalDateTime createdAt = LocalDateTime.now();
}
