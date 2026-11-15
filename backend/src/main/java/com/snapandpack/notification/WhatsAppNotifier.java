package com.snapandpack.notification;

import com.snapandpack.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Placeholder for WhatsApp. NOT WORKING YET: it only logs that a message would have been sent.
 *
 * TO MAKE IT WORK:
 *   1. Register for the WhatsApp Business Platform (Meta Cloud API) or a provider that resells it,
 *      and get a phone number id and an access token.
 *   2. WhatsApp only allows messages to customers from pre-approved templates. Create and get approval
 *      for a template for each kind of message (order received, order shipped, payment received).
 *   3. Add app.whatsapp.phone-number-id and app.whatsapp.access-token to application.properties
 *      (read the token from an environment variable).
 *   4. Accounts do not store a phone number yet. Add a "phone" column to the users table and User class.
 *   5. In send() below, replace the log line with an HTTP POST to
 *      https://graph.facebook.com/<version>/<phone-number-id>/messages carrying the template name and values.
 *   6. Set app.whatsapp.enabled=true.
 */
@Component
@ConditionalOnProperty(name = "app.whatsapp.enabled", havingValue = "true")
public class WhatsAppNotifier implements Notifier {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppNotifier.class);

    /** Would send the message on WhatsApp. For now it only records that it was asked to. */
    @Override
    public void send(User user, String subject, String message) {
        log.warn("WhatsApp is not connected yet. Not sent to {}: {}", user.getEmail(), subject);
    }
}
