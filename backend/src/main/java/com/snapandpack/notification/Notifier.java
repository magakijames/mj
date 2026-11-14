package com.snapandpack.notification;

import com.snapandpack.model.User;

/**
 * Messages the shop sends to people. Today there is one implementation, LogNotifier, which only writes
 * to the server log. Email, SMS and WhatsApp will be added later as further implementations of this
 * interface, without changing the code that calls it.
 */
public interface Notifier {

    /** Tells a user how to choose a new password. The link works once and for a limited time. */
    void sendPasswordReset(User user, String link);
}
