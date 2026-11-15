package com.snapandpack.controller;

import com.snapandpack.dto.AuditEntryResponse;
import com.snapandpack.security.Access;
import com.snapandpack.service.AuditService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Address for the activity record (/api/audit). */
@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService auditService;

    /** Receives the service this controller passes requests to. */
    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    /** GET /api/audit: the 200 most recent activity lines. Super admin only. */
    @GetMapping
    @PreAuthorize(Access.SUPER_ADMIN)
    public List<AuditEntryResponse> recent() {
        return auditService.recent();
    }
}
