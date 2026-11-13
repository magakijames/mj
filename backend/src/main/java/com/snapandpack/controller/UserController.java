package com.snapandpack.controller;

import com.snapandpack.dto.UserDtos.CreateUserRequest;
import com.snapandpack.dto.UserDtos.UpdateActiveRequest;
import com.snapandpack.dto.UserDtos.UpdateRoleRequest;
import com.snapandpack.dto.UserDtos.UserResponse;
import com.snapandpack.security.CurrentUser;
import com.snapandpack.service.UserService;
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

/** Accounts. Only the admin can create staff, change roles or switch accounts on and off. */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public List<UserResponse> list(Authentication authentication) {
        return userService.list(CurrentUser.id(authentication));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return userService.create(request);
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse changeRole(Authentication authentication, @PathVariable Long id,
                                   @Valid @RequestBody UpdateRoleRequest request) {
        return userService.changeRole(CurrentUser.id(authentication), id, request.role());
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse setActive(Authentication authentication, @PathVariable Long id,
                                  @Valid @RequestBody UpdateActiveRequest request) {
        return userService.setActive(CurrentUser.id(authentication), id, request.active());
    }
}
