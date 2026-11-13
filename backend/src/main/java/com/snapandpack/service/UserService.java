package com.snapandpack.service;

import com.snapandpack.dto.UserDtos.CreateUserRequest;
import com.snapandpack.dto.UserDtos.UserResponse;
import com.snapandpack.exception.ApiException;
import com.snapandpack.model.Role;
import com.snapandpack.model.User;
import com.snapandpack.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UserResponse get(Long id) {
        return UserResponse.from(find(id));
    }

    /** The admin sees every account; support staff see customers only. */
    @Transactional(readOnly = true)
    public List<UserResponse> list(Long viewerId) {
        User viewer = find(viewerId);
        List<User> result = viewer.getRole() == Role.ADMIN
                ? users.findAllByOrderByCreatedAtDesc()
                : users.findByRoleOrderByCreatedAtDesc(Role.CUSTOMER);
        return result.stream().map(UserResponse::from).toList();
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        return UserResponse.from(createAccount(request.name(), request.email(), request.password(), request.role()));
    }

    @Transactional
    public User createAccount(String name, String email, String password, Role role) {
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

    @Transactional
    public UserResponse changeRole(Long actingAdminId, Long userId, Role role) {
        if (actingAdminId.equals(userId)) {
            throw ApiException.badRequest("You cannot change your own role");
        }
        User user = find(userId);
        user.setRole(role);
        return UserResponse.from(user);
    }

    /** Accounts are switched off, not deleted, so past orders keep their customer. */
    @Transactional
    public UserResponse setActive(Long actingAdminId, Long userId, boolean active) {
        if (actingAdminId.equals(userId)) {
            throw ApiException.badRequest("You cannot deactivate your own account");
        }
        User user = find(userId);
        user.setActive(active);
        return UserResponse.from(user);
    }

    private User find(Long id) {
        return users.findById(id).orElseThrow(() -> ApiException.notFound("Account not found"));
    }
}
