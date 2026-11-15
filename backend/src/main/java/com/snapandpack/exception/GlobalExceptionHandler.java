package com.snapandpack.exception;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Every error leaves the API as {"message": "..."} so the front end can show it.
 * Database details, stack traces and other internals are logged on the server and never sent to the browser.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Errors raised on purpose by the services, with their own status and message. */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, String>> handleApi(ApiException e) {
        return body(e.getStatus(), e.getMessage());
    }

    /** A form field failed its checks: reply with the first problem found. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Some details are missing or wrong");
        return body(HttpStatus.BAD_REQUEST, message);
    }

    /** The request body was not valid JSON or had a value of the wrong kind. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleUnreadable(HttpMessageNotReadableException e) {
        return body(HttpStatus.BAD_REQUEST, "The request could not be read");
    }

    /** An uploaded file was larger than the server accepts. */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleTooLarge(MaxUploadSizeExceededException e) {
        return body(HttpStatus.PAYLOAD_TOO_LARGE, "That file is too large");
    }

    /** The database refused the change because it breaks one of its rules. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDatabaseRule(DataIntegrityViolationException e) {
        log.warn("The database refused a change: {}", e.getMostSpecificCause().getMessage());
        return body(HttpStatus.CONFLICT, "That change is not allowed by the database rules");
    }

    /**
     * Anything not handled above. Sign-in and permission errors are passed on to Spring Security,
     * which answers 401 or 403. Ordinary web errors (wrong address, wrong method) keep their own status.
     * Everything else is a fault in the server: it is logged in full and the browser is told nothing specific.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpected(Exception e) throws Exception {
        if (e instanceof AccessDeniedException || e instanceof AuthenticationException) {
            throw e;
        }
        if (e instanceof ErrorResponse known) {
            HttpStatusCode status = known.getStatusCode();
            return ResponseEntity.status(status).body(Map.of("message", status.value() == 404 ? "Not found" : "That request is not supported"));
        }
        log.error("Unexpected error", e);
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong on the server. Please try again.");
    }

    /** Builds the JSON error reply. */
    private static ResponseEntity<Map<String, String>> body(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }
}
