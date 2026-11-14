package com.snapandpack.repository;

import com.snapandpack.model.AuditLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Database access for the activity record. */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /** The 200 most recent lines, newest first. */
    List<AuditLog> findTop200ByOrderByIdDesc();
}
