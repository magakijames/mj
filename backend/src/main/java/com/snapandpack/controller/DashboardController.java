package com.snapandpack.controller;

import com.snapandpack.dto.StatsResponse;
import com.snapandpack.service.DashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCT_MANAGER', 'ORDER_MANAGER', 'SUPPORT')")
    public StatsResponse stats() {
        return dashboardService.stats();
    }
}
