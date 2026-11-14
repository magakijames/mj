package com.snapandpack.service;

import com.snapandpack.dto.AuditEntryResponse;
import com.snapandpack.model.AuditLog;
import com.snapandpack.model.User;
import com.snapandpack.repository.AuditLogRepository;
import com.snapandpack.repository.UserRepository;
import com.snapandpack.security.CurrentUser;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Keeps the activity record: a line for every change staff make and every security event. */
@Service
public class AuditService {

    private static final int MAX_DETAILS = 500;

    private final AuditLogRepository auditLog;
    private final UserRepository users;

    /** Receives database access for the activity record and for accounts. */
    public AuditService(AuditLogRepository auditLog, UserRepository users) {
        this.auditLog = auditLog;
        this.users = users;
    }

    /** Adds a line for something done by whoever is signed in on the current request. */
    @Transactional
    public void record(String action, String details) {
        Long actorId = CurrentUser.idOrNull();
        User actor = actorId == null ? null : users.findById(actorId).orElse(null);
        record(actor, action, details);
    }

    /** Adds a line for something that concerns a known account, or nobody (null), such as a locked sign-in. */
    @Transactional
    public void record(User actor, String action, String details) {
        AuditLog entry = new AuditLog();
        entry.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        entry.setActorId(actor == null ? null : actor.getId());
        entry.setActorEmail(actor == null ? null : actor.getEmail());
        entry.setAction(action);
        entry.setDetails(details != null && details.length() > MAX_DETAILS ? details.substring(0, MAX_DETAILS) : details);
        auditLog.save(entry);
    }

    /** The 200 most recent lines, newest first, for the admin's Activity page. */
    @Transactional(readOnly = true)
    public List<AuditEntryResponse> recent() {
        return auditLog.findTop200ByOrderByIdDesc().stream().map(AuditEntryResponse::from).toList();
    }
}
