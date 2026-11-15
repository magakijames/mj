package com.snapandpack.notification;

import com.snapandpack.model.User;

/**
 * One way of reaching a person outside the site: the server log, email, SMS or WhatsApp.
 * Every class that implements this interface and is switched on receives each message.
 * To add a new channel, write a class like EmailNotifier; nothing else needs to change.
 */
public interface Notifier {

    /** Sends a short message with a subject line to one user. */
    void send(User user, String subject, String message);

    /** Tells a user how to choose a new password. The link works once and for a limited time. */
    default void sendPasswordReset(User user, String link) {
        send(user, "Reset your password",
                "Open this link to choose a new password. It works once and for 30 minutes:\n" + link
                        + "\n\nIf you did not ask for this, you can ignore this message.");
    }
}
