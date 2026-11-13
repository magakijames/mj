package com.snapandpack.model;

/**
 * What each account is allowed to do.
 * The admin controls the whole system; the other staff roles each cover one area.
 */
public enum Role {
    /** Everything, including creating staff and changing roles. */
    ADMIN,
    /** Adds, edits and removes products and keeps stock up to date. */
    PRODUCT_MANAGER,
    /** Sees every order and moves it through pending, shipped, delivered or cancelled. */
    ORDER_MANAGER,
    /** Read-only view of orders and customers, for answering customer questions. */
    SUPPORT,
    /** A shopper. */
    CUSTOMER;

    public boolean isStaff() {
        return this != CUSTOMER;
    }
}
