package com.snapandpack.notification;

import com.snapandpack.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Writes every outgoing message to the server log. Useful while testing, because password reset links
 * can be read there before email is connected. Switch it off for the live site with
 * app.notifications.log=false, so customer messages are not kept in log files.
 */
@Component
@ConditionalOnProperty(name = "app.notifications.log", havingValue = "true", matchIfMissing = true)
public class LogNotifier implements Notifier {

    private static final Logger log = LoggerFactory.getLogger(LogNotifier.class);

    /** Writes the message to the server log. */
    @Override
    public void send(User user, String subject, String message) {
        log.info("Message for {} | {} | {}", user.getEmail(), subject, message.replace("\n", " "));
    }

    /** Writes the reset link to the server log on a line of its own so it is easy to find. */
    @Override
    public void sendPasswordReset(User user, String link) {
        log.info("Password reset link for {}: {}", user.getEmail(), link);
    }
}
