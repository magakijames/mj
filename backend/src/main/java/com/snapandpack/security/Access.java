package com.snapandpack.security;

/** The role rules used on controller methods, written once so every controller agrees. */
public final class Access {

    private Access() {
    }

    /** Only the super admin: accounts, roles, site settings and the activity record. */
    public static final String SUPER_ADMIN = "hasRole('SUPER_ADMIN')";
    /** Anyone who works for the shop. */
    public static final String STAFF = "hasAnyRole('SUPER_ADMIN', 'ADMIN', 'INVENTORY_MANAGER', 'ORDER_MANAGER', 'SUPPORT')";
    /** May add, edit and delete products and upload product photos. */
    public static final String PRODUCT_EDITORS = "hasAnyRole('SUPER_ADMIN', 'ADMIN', 'INVENTORY_MANAGER')";
    /** May see every order. */
    public static final String ORDER_VIEWERS = "hasAnyRole('SUPER_ADMIN', 'ADMIN', 'ORDER_MANAGER', 'SUPPORT')";
    /** May change an order's status. */
    public static final String ORDER_EDITORS = "hasAnyRole('SUPER_ADMIN', 'ADMIN', 'ORDER_MANAGER')";
    /** May see the account list (support staff get customers only). */
    public static final String USER_VIEWERS = "hasAnyRole('SUPER_ADMIN', 'SUPPORT')";
}
