package com.snapandpack.controller;

import com.snapandpack.dto.NotificationDtos.NotificationList;
import com.snapandpack.dto.NotificationDtos.UnreadCount;
import com.snapandpack.security.CurrentUser;
import com.snapandpack.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Addresses for the signed-in user's own notifications (/api/notifications). Any signed-in user. */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    /** Receives the service this controller passes requests to. */
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /** GET /api/notifications: the user's recent messages and the unseen count. */
    @GetMapping
    public NotificationList list(Authentication authentication) {
        return notificationService.list(CurrentUser.id(authentication));
    }

    /** GET /api/notifications/unread-count: just the unseen count, for the number in the header. */
    @GetMapping("/unread-count")
    public UnreadCount unreadCount(Authentication authentication) {
        return new UnreadCount(notificationService.unreadCount(CurrentUser.id(authentication)));
    }

    /** POST /api/notifications/{id}/read: marks one of the user's messages as seen. */
    @PostMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(Authentication authentication, @PathVariable Long id) {
        notificationService.markRead(CurrentUser.id(authentication), id);
    }

    /** POST /api/notifications/read-all: marks all of the user's messages as seen. */
    @PostMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllRead(Authentication authentication) {
        notificationService.markAllRead(CurrentUser.id(authentication));
    }
}
