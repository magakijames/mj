package com.snapandpack.dto;

import com.snapandpack.model.Notification;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

/** Response bodies for in-site notifications. */
public final class NotificationDtos {

    private NotificationDtos() {
    }

    /** One message as the front end sees it. */
    public record NotificationResponse(Long id, String title, String body, String link, boolean read, Instant createdAt) {

        /** Copies a message from the database into a response. */
        public static NotificationResponse from(Notification n) {
            return new NotificationResponse(n.getId(), n.getTitle(), n.getBody(), n.getLink(), n.getReadAt() != null,
                    n.getCreatedAt().toInstant(ZoneOffset.UTC));
        }
    }

    /** The user's recent messages and how many are unseen. */
    public record NotificationList(long unread, List<NotificationResponse> items) {
    }

    /** Just the unseen count, for the number in the header. */
    public record UnreadCount(long unread) {
    }
}
