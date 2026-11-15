package com.snapandpack.notification;

import com.snapandpack.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Placeholder for SMS. NOT WORKING YET: it only logs that an SMS would have been sent.
 *
 * TO MAKE IT WORK:
 *   1. Choose an SMS provider (in Kenya: Africa's Talking, Safaricom, Twilio and others) and open an account.
 *   2. Add the provider's username, API key and sender name to application.properties as
 *      app.sms.username, app.sms.api-key and app.sms.sender (read the key from an environment variable).
 *   3. Accounts do not store a phone number yet. Add a "phone" column to the users table and to the
 *      User class, and ask for it when registering.
 *   4. In send() below, replace the log line with an HTTP call to the provider, for example with
 *      Spring's RestClient, posting the phone number and the message.
 *   5. Set app.sms.enabled=true.
 */
@Component
@ConditionalOnProperty(name = "app.sms.enabled", havingValue = "true")
public class SmsNotifier implements Notifier {

    private static final Logger log = LoggerFactory.getLogger(SmsNotifier.class);

    /** Would send the message as an SMS. For now it only records that it was asked to. */
    @Override
    public void send(User user, String subject, String message) {
        log.warn("SMS is not connected yet. Not sent to {}: {}", user.getEmail(), subject);
    }
}
