package com.ipillgood.server.domain.auth.dto;

import lombok.Builder;

public class AuthResponse {

    @Builder
    public record SignUp(
            Long id,
            String nickname,
            String username,
            String email
    ) {
    }
}
