package com.snapandpack.repository;

import com.snapandpack.model.EmailVerificationToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Database access for email confirmation links. */
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    /** Finds the record whose stored hash matches the code in a link. */
    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

    /** The newest link made for an account, used to stop links being requested too often. */
    Optional<EmailVerificationToken> findFirstByUserIdOrderByIdDesc(Long userId);

    /** Removes every confirmation link belonging to one account. */
    @Modifying
    @Query("delete from EmailVerificationToken t where t.user.id = :userId")
    int deleteByUserId(@Param("userId") Long userId);
}
