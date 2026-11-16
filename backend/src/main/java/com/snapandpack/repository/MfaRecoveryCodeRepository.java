package com.snapandpack.repository;

import com.snapandpack.model.MfaRecoveryCode;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Database access for recovery codes. */
public interface MfaRecoveryCodeRepository extends JpaRepository<MfaRecoveryCode, Long> {

    /** Finds one of a user's recovery codes that has not been used yet, by its hash. */
    Optional<MfaRecoveryCode> findFirstByUserIdAndCodeHashAndUsedAtIsNull(Long userId, String codeHash);

    /** How many unused recovery codes a user has left. */
    long countByUserIdAndUsedAtIsNull(Long userId);

    /** Removes all of a user's recovery codes, when two-step sign-in is switched off or set up again. */
    @Modifying
    @Query("delete from MfaRecoveryCode c where c.userId = :userId")
    int deleteByUserId(@Param("userId") Long userId);
}
