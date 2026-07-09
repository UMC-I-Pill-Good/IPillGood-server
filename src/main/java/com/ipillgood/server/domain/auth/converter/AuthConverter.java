package com.ipillgood.server.domain.auth.converter;

import com.ipillgood.server.domain.auth.dto.AuthRequest;
import com.ipillgood.server.domain.auth.dto.AuthResponse;
import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.member.entity.Role;

public class AuthConverter {

    public static Member toMember(AuthRequest.SignUp request, String encodedPassword) {
        return Member.builder()
                .nickname(request.nickname())
                .username(request.username())
                .email(request.email())
                .password(encodedPassword)
                .role(Role.USER)
                .build();
    }

    public static AuthResponse.SignUp toSignUpResponse(Member member) {
        return AuthResponse.SignUp.builder()
                .id(member.getId())
                .nickname(member.getNickname())
                .username(member.getUsername())
                .email(member.getEmail())
                .build();
    }
}
