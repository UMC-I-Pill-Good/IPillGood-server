package com.ipillgood.server.domain.auth.controller;

import com.ipillgood.server.domain.auth.code.AuthSuccessCode;
import com.ipillgood.server.domain.auth.controller.docs.AuthApi;
import com.ipillgood.server.domain.auth.dto.AuthRequest;
import com.ipillgood.server.domain.auth.dto.AuthResponse;
import com.ipillgood.server.domain.auth.service.AuthService;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthApi {

    private final AuthService authService;

    // 회원가입
    @Override
    @PostMapping("/signup")
    public ApiResponse<AuthResponse.SignUp> signUp(@Valid @RequestBody AuthRequest.SignUp request) {
        AuthResponse.SignUp response = authService.signUp(request);
        return ApiResponse.onSuccess(AuthSuccessCode.SIGNUP_SUCCESS, response);
    }
}
