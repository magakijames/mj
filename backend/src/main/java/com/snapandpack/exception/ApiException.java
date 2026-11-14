package com.snapandpack.exception;

import org.springframework.http.HttpStatus;

/** Thrown by services when a request cannot be carried out; turned into a JSON error by GlobalExceptionHandler. */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    /** Creates an error with the HTTP status to send and the message to show the user. */
    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    /** The HTTP status this error is sent with. */
    public HttpStatus getStatus() {
        return status;
    }

    /** The thing asked for does not exist (404). */
    public static ApiException notFound(String message) {
        return new ApiException(HttpStatus.NOT_FOUND, message);
    }

    /** The request itself is wrong (400). */
    public static ApiException badRequest(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, message);
    }

    /** The request clashes with what is already saved, such as a taken email or no stock left (409). */
    public static ApiException conflict(String message) {
        return new ApiException(HttpStatus.CONFLICT, message);
    }
}
