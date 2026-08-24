package com.example.pms.entity;

import com.example.pms.domain.ReviewType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "review_cycles")
@Getter @Setter @NoArgsConstructor
public class ReviewCycle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Enumerated(EnumType.STRING)
    private ReviewType reviewType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status = "OPEN";
}
