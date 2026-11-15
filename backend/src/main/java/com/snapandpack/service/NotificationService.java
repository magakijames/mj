package com.snapandpack.service;

import com.snapandpack.dto.NotificationDtos.NotificationList;
import com.snapandpack.dto.NotificationDtos.NotificationResponse;
import com.snapandpack.exception.ApiException;
import com.snapandpack.model.Notification;
import com.snapandpack.model.Role;
import com.snapandpack.model.User;
import com.snapandpack.notification.Notifier;
import com.snapandpack.repository.NotificationRepository;
import com.snapandpack.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Notifications. Each one is saved so it shows inside the site (the number in the header and the
 * Notifications page). Messages to customers are also handed to every outside channel that is
 * switched on: the server log, email, and later SMS and WhatsApp.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notifications;
    private final UserRepository users;
    private final List<Notifier> channels;

    /** Receives database access and every outside channel that is switched on. */
    public NotificationService(NotificationRepository notifications, UserRepository users, List<Notifier> channels) {
        this.notifications = notifications;
        this.users = users;
        this.channels = channels;
    }

    /** Tells one user something: saves it for the site and sends it through the outside channels. */
    @Transactional
    public void notify(User user, String title, String body, String link) {
        save(user.getId(), title, body, link);
        for (Notifier channel : channels) {
            try {
                channel.send(user, title, body);
            } catch (RuntimeException e) {
                // A broken channel must never undo the order or payment that caused the message.
                log.error("Could not send \"{}\" through {}: {}", title, channel.getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    /** Tells every active member of staff with one of these roles, inside the site only. */
    @Transactional
    public void notifyStaff(List<Role> roles, String title, String body, String link) {
        for (User member : users.findByRoleInAndActiveTrue(roles)) {
            save(member.getId(), title, body, link);
        }
    }

    /** The signed-in user's 50 most recent messages and how many are unseen. */
    @Transactional(readOnly = true)
    public NotificationList list(Long userId) {
        List<NotificationResponse> items = notifications.findTop50ByUserIdOrderByIdDesc(userId).stream()
                .map(NotificationResponse::from).toList();
        return new NotificationList(notifications.countByUserIdAndReadAtIsNull(userId), items);
    }

    /** How many of the user's messages are unseen, for the number in the header. */
    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return notifications.countByUserIdAndReadAtIsNull(userId);
    }

    /** Marks one message as seen. Fails with "not found" if it belongs to someone else. */
    @Transactional
    public void markRead(Long userId, Long notificationId) {
        Notification notification = notifications.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> ApiException.notFound("Notification not found"));
        if (notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now(ZoneOffset.UTC));
        }
    }

    /** Marks all of the user's messages as seen. */
    @Transactional
    public void markAllRead(Long userId) {
        notifications.markAllRead(userId, LocalDateTime.now(ZoneOffset.UTC));
    }

    /** Stores one message for one user, shortened to fit the database columns. */
    private void save(Long userId, String title, String body, String link) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(cut(title, 150));
        notification.setBody(cut(body, 500));
        notification.setLink(link);
        notification.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        notifications.save(notification);
    }

    /** Shortens text to at most max characters. */
    private static String cut(String text, int max) {
        return text.length() > max ? text.substring(0, max) : text;
    }
}
