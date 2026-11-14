package com.snapandpack.repository;

import com.snapandpack.model.PasswordResetToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Database access for password reset links. */
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /** Finds the reset record whose stored hash matches. */
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    /** Removes every reset link belonging to one account, so only the newest link can work. */
    @Modifying
    @Query("delete from PasswordResetToken t where t.user.id = :userId")
    int deleteByUserId(@Param("userId") Long userId);
}
