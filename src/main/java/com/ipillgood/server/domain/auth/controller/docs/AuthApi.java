package com.ipillgood.server.domain.auth.controller.docs;

import com.ipillgood.server.domain.auth.dto.AuthRequest;
import com.ipillgood.server.domain.auth.dto.AuthResponse;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Auth API", description = "인증(회원가입/로그인) 관련 API")
public interface AuthApi {

    @Operation(summary = "로컬 회원가입",
            description = "닉네임/아이디/이메일/비밀번호를 입력받아 회원가입을 진행합니다.")
    ApiResponse<AuthResponse.SignUp> signUp(AuthRequest.SignUp request);
}
