package com.ipillgood.server.domain.auth.converter;

import com.ipillgood.server.domain.auth.dto.AuthRequest;
import com.ipillgood.server.domain.auth.dto.AuthResponse;
import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.member.entity.Role;

public class AuthConverter {

    // 회원가입 - 멤버 엔티티 생성
    public static Member toMember(AuthRequest.SignUp request, String encodedPassword) {
        return Member.builder()
                .nickname(request.nickname())
                .username(request.username())
                .email(request.email())
                .password(encodedPassword)
                .role(Role.USER)
                .build();
    }

    // 회원가입 - 멤버 엔티티 -> DTO 변환
    public static AuthResponse.SignUp toSignUpResponse(Member member) {
        return AuthResponse.SignUp.builder()
                .id(member.getId())
                .nickname(member.getNickname())
                .username(member.getUsername())
                .email(member.getEmail())
                .build();
    }

    // 로그인 - 발급된 토큰 -> DTO 변환
    public static AuthResponse.Login toLoginResponse(String accessToken, String refreshToken) {
        return AuthResponse.Login.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
