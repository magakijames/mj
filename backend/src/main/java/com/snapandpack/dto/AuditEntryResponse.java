package com.snapandpack.dto;

import com.snapandpack.model.AuditLog;
import java.time.Instant;
import java.time.ZoneOffset;

/** One line of the activity record as the admin page shows it. */
public record AuditEntryResponse(Long id, Instant at, String actorEmail, String action, String details) {

    /** Copies an activity line from the database into a response. */
    public static AuditEntryResponse from(AuditLog entry) {
        return new AuditEntryResponse(entry.getId(), entry.getCreatedAt().toInstant(ZoneOffset.UTC),
                entry.getActorEmail(), entry.getAction(), entry.getDetails());
    }
}
