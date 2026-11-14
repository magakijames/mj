package com.snapandpack.service;

import com.snapandpack.dto.UserDtos.CreateUserRequest;
import com.snapandpack.dto.UserDtos.UserResponse;
import com.snapandpack.exception.ApiException;
import com.snapandpack.model.Role;
import com.snapandpack.model.User;
import com.snapandpack.repository.UserRepository;
import com.snapandpack.security.PasswordPolicy;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Creating accounts and the admin's control over them. */
@Service
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AuditService audit;

    /** Receives the services and database access this class needs. */
    public UserService(UserRepository users, PasswordEncoder passwordEncoder, AuditService audit) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.audit = audit;
    }

    /** One account's details. */
    @Transactional(readOnly = true)
    public UserResponse get(Long id) {
        return UserResponse.from(find(id));
    }

    /** The account list for the Users page: the admin sees every account, support staff see customers only. */
    @Transactional(readOnly = true)
    public List<UserResponse> list(Long viewerId) {
        User viewer = find(viewerId);
        List<User> result = viewer.getRole() == Role.ADMIN
                ? users.findAllByOrderByCreatedAtDesc()
                : users.findByRoleOrderByCreatedAtDesc(Role.CUSTOMER);
        return result.stream().map(UserResponse::from).toList();
    }

    /** Lets the admin create an account with any role, and records it. */
    @Transactional
    public UserResponse create(CreateUserRequest request) {
        User user = createAccount(request.name(), request.email(), request.password(), request.role());
        audit.record("USER_CREATED", user.getEmail() + " created as " + user.getRole());
        return UserResponse.from(user);
    }

    /**
     * Saves a new account. Checks the password rules, stores the email in lower case,
     * refuses an email that is already taken and stores only a hash of the password.
     */
    @Transactional
    public User createAccount(String name, String email, String password, Role role) {
        PasswordPolicy.check(password);
        String cleanEmail = email.trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(cleanEmail)) {
            throw ApiException.conflict("That email is already registered");
        }
        User user = new User();
        user.setName(name.trim());
        user.setEmail(cleanEmail);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        return users.save(user);
    }

    /** Lets the admin change what an account may do. The admin cannot change their own role. */
    @Transactional
    public UserResponse changeRole(Long actingAdminId, Long userId, Role role) {
        if (actingAdminId.equals(userId)) {
            throw ApiException.badRequest("You cannot change your own role");
        }
        User user = find(userId);
        Role before = user.getRole();
        user.setRole(role);
        audit.record("USER_ROLE_CHANGED", user.getEmail() + ": " + before + " to " + role);
        return UserResponse.from(user);
    }

    /**
     * Lets the admin switch an account off or back on. Accounts are switched off, not deleted,
     * so past orders keep their customer. The admin cannot switch off their own account.
     */
    @Transactional
    public UserResponse setActive(Long actingAdminId, Long userId, boolean active) {
        if (actingAdminId.equals(userId)) {
            throw ApiException.badRequest("You cannot deactivate your own account");
        }
        User user = find(userId);
        user.setActive(active);
        audit.record(active ? "USER_ACTIVATED" : "USER_DEACTIVATED", user.getEmail());
        return UserResponse.from(user);
    }

    /** Loads an account or fails with "not found". */
    private User find(Long id) {
        return users.findById(id).orElseThrow(() -> ApiException.notFound("Account not found"));
    }
}
