package com.snapandpack.controller;

import com.snapandpack.dto.StatsResponse;
import com.snapandpack.service.DashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Address for the admin dashboard totals (/api/dashboard). */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    /** Receives the service this controller passes requests to. */
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /** GET /api/dashboard/stats: counts and revenue. Any member of staff. */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCT_MANAGER', 'ORDER_MANAGER', 'SUPPORT')")
    public StatsResponse stats() {
        return dashboardService.stats();
    }
}
