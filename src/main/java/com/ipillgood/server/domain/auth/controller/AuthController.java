package com.ipillgood.server.domain.auth.controller;

import com.ipillgood.server.domain.auth.code.AuthSuccessCode;
import com.ipillgood.server.domain.auth.controller.docs.AuthApi;
import com.ipillgood.server.domain.auth.dto.AuthRequest;
import com.ipillgood.server.domain.auth.dto.AuthResponse;
import com.ipillgood.server.domain.auth.service.AuthService;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthApi {

    private final AuthService authService;

    // 로컬 회원가입
    @Override
    @PostMapping("/signup")
    public ApiResponse<AuthResponse.SignUp> signUp(@Valid @RequestBody AuthRequest.SignUp request) {
        AuthResponse.SignUp response = authService.signUp(request);
        return ApiResponse.onSuccess(AuthSuccessCode.SIGNUP_SUCCESS, response);
    }

    // 로컬 로그인
    @Override
    @PostMapping("/login")
    public ApiResponse<AuthResponse.Login> login(@Valid @RequestBody AuthRequest.Login request) {
        AuthResponse.Login response = authService.login(request);
        return ApiResponse.onSuccess(AuthSuccessCode.LOGIN_SUCCESS, response);
    }

    // 아이디 중복확인
    @Override
    @GetMapping("/check-username")
    public ApiResponse<Void> checkUsername(@RequestParam String username) {
        authService.checkUsernameDuplicate(username);
        return ApiResponse.onSuccess(AuthSuccessCode.USERNAME_AVAILABLE, null);
    }

    // 이메일 중복확인
    @Override
    @GetMapping("/check-email")
    public ApiResponse<Void> checkEmail(@RequestParam String email) {
        authService.checkEmailDuplicate(email);
        return ApiResponse.onSuccess(AuthSuccessCode.EMAIL_AVAILABLE, null);
    }
}
