package com.example.pms.api;


import com.example.pms.domain.NotificationType;
import com.example.pms.dto.NotificationResponse;
import com.example.pms.entity.AppUser;
import com.example.pms.entity.Notification;
import com.example.pms.repository.AppUserRepository;
import com.example.pms.service.NotificationService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final AppUserRepository users;

    public NotificationController(
            NotificationService notificationService,
            AppUserRepository users
    ) {
        this.notificationService = notificationService;
        this.users = users;
    }

    @GetMapping
    public List<NotificationResponse> getNotifications() {

        AppUser user = currentUser();

        return notificationService
                .getNotifications(user.getId());
    }

    @GetMapping("/unread-count")
    public long getUnreadCount() {

        AppUser user = currentUser();

        return notificationService
                .getUnreadCount(user.getId());
    }

    @PatchMapping("/{id}/read")
    public void markAsRead(
            @PathVariable Long id
    ) {

        AppUser user = currentUser();

        notificationService.markAsRead(
                id,
                user.getId()
        );
    }

    private AppUser currentUser() {

        String username =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        return users.findByUsername(username)
                .orElseThrow();
    }


    //To Test Notification Is Working... Just sample method execute it

    @PostMapping("/test")
    public NotificationResponse testNotification() {

        AppUser currentUser = currentUser();

        Notification notification =
                notificationService.create(
                        currentUser,
                        "Test Notification",
                        "WebSocket is working successfully!",
                        NotificationType.REVIEW_SUBMITTED,
                        4L,
                        "TEST"
                );

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
