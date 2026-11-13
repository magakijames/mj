package com.snapandpack.dto;

import java.math.BigDecimal;

/** Totals shown on the admin dashboard. Revenue leaves out cancelled orders. */
public record StatsResponse(long products, long orders, long customers, long staff, BigDecimal revenue) {
}
