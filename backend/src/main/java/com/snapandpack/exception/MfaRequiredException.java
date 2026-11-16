package com.snapandpack.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown at sign-in when the password is right but the account also needs a code from the
 * authenticator app and none was sent. The front end then shows the code box.
 */
public class MfaRequiredException extends ApiException {

    /** Creates the "enter your code" answer. */
    public MfaRequiredException() {
        super(HttpStatus.UNAUTHORIZED, "Enter the 6-digit code from your authenticator app.");
    }
}
