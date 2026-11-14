package com.snapandpack.controller;

import com.snapandpack.dto.AuthDtos.ResetLinkResponse;
import com.snapandpack.dto.UserDtos.CreateUserRequest;
import com.snapandpack.dto.UserDtos.UpdateActiveRequest;
import com.snapandpack.dto.UserDtos.UpdateRoleRequest;
import com.snapandpack.dto.UserDtos.UserResponse;
import com.snapandpack.security.CurrentUser;
import com.snapandpack.service.PasswordResetService;
import com.snapandpack.service.UserService;
import com.snapandpack.web.SiteUrl;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Addresses for accounts (/api/users). Only the admin can create staff, change roles or switch accounts on and off. */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final PasswordResetService passwordResetService;
    private final SiteUrl siteUrl;

    /** Receives the services this controller passes requests to. */
    public UserController(UserService userService, PasswordResetService passwordResetService, SiteUrl siteUrl) {
        this.userService = userService;
        this.passwordResetService = passwordResetService;
        this.siteUrl = siteUrl;
    }

    /** GET /api/users: the account list. The admin gets everyone; support gets customers only. */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public List<UserResponse> list(Authentication authentication) {
        return userService.list(CurrentUser.id(authentication));
    }

    /** POST /api/users: creates an account with any role. Admin only. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return userService.create(request);
    }

    /** PATCH /api/users/{id}/role: changes what an account may do. Admin only. */
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse changeRole(Authentication authentication, @PathVariable Long id,
                                   @Valid @RequestBody UpdateRoleRequest request) {
        return userService.changeRole(CurrentUser.id(authentication), id, request.role());
    }

    /** PATCH /api/users/{id}/active: switches an account off or back on. Admin only. */
    @PatchMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse setActive(Authentication authentication, @PathVariable Long id,
                                  @Valid @RequestBody UpdateActiveRequest request) {
        return userService.setActive(CurrentUser.id(authentication), id, request.active());
    }

    /** POST /api/users/{id}/password-reset: makes a one-time reset link for the admin to pass on. Admin only. */
    @PostMapping("/{id}/password-reset")
    @PreAuthorize("hasRole('ADMIN')")
    public ResetLinkResponse createResetLink(@PathVariable Long id) {
        return passwordResetService.createLinkForUser(id, siteUrl.current());
    }
}
