package com.snapandpack.repository;

import com.snapandpack.model.Role;
import com.snapandpack.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Database access for accounts. Spring writes the SQL from each method name. */
public interface UserRepository extends JpaRepository<User, Long> {

    /** Finds the account with this email, whatever the letter case. */
    Optional<User> findByEmailIgnoreCase(String email);

    /** True when an account with this email already exists. */
    boolean existsByEmailIgnoreCase(String email);

    /** True when at least one account has this role. */
    boolean existsByRole(Role role);

    /** How many accounts have this role. */
    long countByRole(Role role);

    /** Every account, newest first. */
    List<User> findAllByOrderByCreatedAtDesc();

    /** Every account with this role, newest first. */
    List<User> findByRoleOrderByCreatedAtDesc(Role role);
}
