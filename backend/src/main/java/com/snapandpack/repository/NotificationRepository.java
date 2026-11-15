package com.snapandpack.repository;

import com.snapandpack.model.Notification;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Database access for in-site notifications. */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** One user's 50 most recent messages, newest first. */
    List<Notification> findTop50ByUserIdOrderByIdDesc(Long userId);

    /** How many of one user's messages have not been seen. */
    long countByUserIdAndReadAtIsNull(Long userId);

    /** Finds a message only if it belongs to this user, so nobody can touch another person's messages. */
    Optional<Notification> findByIdAndUserId(Long id, Long userId);

    /** Marks all of one user's unseen messages as seen. */
    @Modifying
    @Query("update Notification n set n.readAt = :now where n.userId = :userId and n.readAt is null")
    int markAllRead(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}
