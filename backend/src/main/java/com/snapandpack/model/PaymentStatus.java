package com.snapandpack.model;

/** Where a payment request stands. */
public enum PaymentStatus {
    /** The request was sent to the customer's phone and no answer has come back yet. */
    PENDING,
    /** The payment provider confirmed the money was received. */
    SUCCESS,
    /** The customer cancelled, the request timed out, or the provider refused it. */
    FAILED
}
