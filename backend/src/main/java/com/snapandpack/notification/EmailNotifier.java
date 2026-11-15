package com.snapandpack.notification;

import com.snapandpack.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Sends messages by email. Off until configured.
 *
 * TO SWITCH ON, in application.properties (or as environment variables):
 *   1. app.mail.enabled=true
 *   2. app.mail.from=        the address messages come from, for example orders@yourshop.co.ke
 *   3. spring.mail.host=     your mail provider's SMTP server, for example smtp.gmail.com
 *      spring.mail.port=     usually 587
 *      spring.mail.username= the mailbox login
 *      spring.mail.password= the mailbox password or app password (set it as MAIL_PASSWORD, never in the file)
 *      spring.mail.properties.mail.smtp.auth=true
 *      spring.mail.properties.mail.smtp.starttls.enable=true
 * The lines are already in application.properties, commented out.
 *
 * This class has not been run against a real mail server yet. Test it with your provider before going live.
 */
@Component
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true")
public class EmailNotifier implements Notifier {

    private static final Logger log = LoggerFactory.getLogger(EmailNotifier.class);

    private final JavaMailSender mailSender;
    private final String from;

    /** Receives Spring's mail sender (built from the spring.mail.* settings) and the "from" address. */
    public EmailNotifier(JavaMailSender mailSender, @Value("${app.mail.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    /** Sends a plain-text email. A failure is logged and does not stop the action that caused the message. */
    @Override
    public void send(User user, String subject, String message) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(from);
            mail.setTo(user.getEmail());
            mail.setSubject(subject);
            mail.setText(message);
            mailSender.send(mail);
        } catch (RuntimeException e) {
            log.error("Could not send email to {}: {}", user.getEmail(), e.getMessage());
        }
    }
}
