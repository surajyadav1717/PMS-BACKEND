package com.example.pms.dto;

import com.example.pms.domain.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String title,
        String message,
        NotificationType type,
        Long referenceId,
        String referenceType,
        boolean isRead,
        LocalDateTime createdAt
) {
}
