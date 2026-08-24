package com.example.pms.service;


import com.example.pms.domain.NotificationType;
import com.example.pms.dto.NotificationResponse;
import com.example.pms.entity.AppUser;
import com.example.pms.entity.Notification;
import com.example.pms.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(
            NotificationRepository notificationRepository,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public Notification create(
            AppUser user,
            String title,
            String message,
            NotificationType type,
            Long referenceId,
            String referenceType
    ) {

        Notification notification = new Notification();

        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setReferenceId(referenceId);
        notification.setReferenceType(referenceType);
        notification.setRead(false);

        Notification saved =
                notificationRepository.save(notification);

        // Real-time notification
        messagingTemplate.convertAndSendToUser(
                user.getUsername(),
                "/queue/notifications",
                toResponse(saved)
        );

        return saved;
    }

    public List<NotificationResponse> getNotifications(
            Long userId
    ) {

        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public long getUnreadCount(Long userId) {

        return notificationRepository
                .countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(
            Long notificationId,
            Long userId
    ) {

        Notification notification =
                notificationRepository.findById(notificationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Notification not found"
                                ));

        // Security check
        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException(
                    "You cannot modify this notification"
            );
        }

        notification.setRead(true);
    }

    private NotificationResponse toResponse(
            Notification notification
    ) {

        return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.getReferenceId(),
                notification.getReferenceType(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
