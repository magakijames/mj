package com.snapandpack.model;

import java.util.List;

/**
 * What each account is allowed to do.
 * The super admin controls the whole system; the other staff roles each cover one area.
 */
public enum Role {
    /** Everything, including site settings, the activity record, staff accounts and roles. */
    SUPER_ADMIN,
    /** Runs the shop day to day: products, stock and orders. Cannot manage accounts or settings. */
    ADMIN,
    /** Adds, edits and removes products and keeps stock up to date. */
    INVENTORY_MANAGER,
    /** Sees every order and moves it through pending, shipped, delivered or cancelled. */
    ORDER_MANAGER,
    /** Read-only view of orders and customers, for answering customer questions. */
    SUPPORT,
    /** A shopper. */
    CUSTOMER;

    /** Roles that should hear about new orders and payments. */
    public static final List<Role> ORDER_STAFF = List.of(SUPER_ADMIN, ADMIN, ORDER_MANAGER);
    /** Roles that should hear about stock running low. */
    public static final List<Role> STOCK_STAFF = List.of(SUPER_ADMIN, ADMIN, INVENTORY_MANAGER);

    /** True for every role that works for the shop, false for customers. */
    public boolean isStaff() {
        return this != CUSTOMER;
    }
}
