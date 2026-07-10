package com.ipillgood.server.global.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import com.ipillgood.server.global.apiPayload.code.BaseErrorCode;
import com.ipillgood.server.global.security.jwt.code.JwtErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 인증되지 않은 요청이 API에 접근했을 때 401 응답을 생성하는 로직
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        // JwtAuthFilter가 담아둔 토큰 검증 실패 원인
        BaseErrorCode errorCode = (BaseErrorCode) request.getAttribute(JwtAuthFilter.ERROR_CODE_ATTRIBUTE);

        // 에러코드가 null 값이라면 토큰이 아예 전달되지 않은 요청
        if (errorCode == null) {
            errorCode = JwtErrorCode.TOKEN_EMPTY;
        }

        // 응답 설정
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        // JSON 응답(401 상태코드)
        objectMapper.writeValue(response.getWriter(), ApiResponse.onFailure(errorCode, null));
    }
}
