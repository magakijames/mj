package com.snapandpack.notification;

import com.snapandpack.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Stand-in used until email is connected: writes the message to the server log, where the person
 * running the server can read it and pass the link on. Replace with an email sender before going live.
 */
@Component
public class LogNotifier implements Notifier {

    private static final Logger log = LoggerFactory.getLogger(LogNotifier.class);

    /** Writes the reset link for this user to the server log. */
    @Override
    public void sendPasswordReset(User user, String link) {
        log.info("Password reset link for {}: {}", user.getEmail(), link);
    }
}
