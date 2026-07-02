package com.example.gifbottary.domain.auth.controller;

import com.example.gifbottary.common.response.ApiResponse;
import com.example.gifbottary.domain.auth.dto.request.LoginRequest;
import com.example.gifbottary.domain.auth.dto.request.SignupRequest;
import com.example.gifbottary.domain.auth.dto.response.LoginResponse;
import com.example.gifbottary.domain.auth.dto.response.LogoutResponse;
import com.example.gifbottary.domain.auth.dto.response.MyInfoResponse;
import com.example.gifbottary.domain.auth.dto.response.SignupResponse;
import com.example.gifbottary.domain.auth.service.AuthService;
import com.example.gifbottary.domain.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SignupResponse> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        return ApiResponse.ok(authService.signup(request));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ApiResponse.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ApiResponse<LogoutResponse> logout() {
        return ApiResponse.ok(authService.logout());
    }

    @GetMapping("/me")
    public ApiResponse<MyInfoResponse> getMyInfo(@AuthenticationPrincipal User user) {
        return ApiResponse.ok(authService.getMyInfo(user));
    }
}
