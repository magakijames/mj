package com.snapandpack.controller;

import com.snapandpack.dto.AuthDtos.AuthResponse;
import com.snapandpack.dto.AuthDtos.LoginRequest;
import com.snapandpack.dto.AuthDtos.RegisterRequest;
import com.snapandpack.dto.UserDtos.UserResponse;
import com.snapandpack.security.CurrentUser;
import com.snapandpack.service.AuthService;
import com.snapandpack.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /** The signed-in user's own details, with their current role. */
    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        return userService.get(CurrentUser.id(authentication));
    }
}
