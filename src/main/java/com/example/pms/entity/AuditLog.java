package com.example.pms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter @Setter @NoArgsConstructor
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) private AppUser user;
    private String entityType;
    private Long entityId;
    private String action;
    @Column(columnDefinition = "TEXT") private String oldValue;
    @Column(columnDefinition = "TEXT") private String newValue;
    private LocalDateTime createdAt = LocalDateTime.now();
}
