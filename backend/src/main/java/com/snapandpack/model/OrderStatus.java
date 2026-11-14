package com.snapandpack.model;

/** The stages an order moves through. CANCELLED is final. */
public enum OrderStatus {
    PENDING,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
