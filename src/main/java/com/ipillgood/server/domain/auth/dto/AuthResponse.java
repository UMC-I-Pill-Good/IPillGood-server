package com.ipillgood.server.domain.auth.dto;

import com.ipillgood.server.domain.member.entity.enums.SocialProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

public class AuthResponse {

    /**
     * 로컬 회원가입 응답
     */
    @Schema(description = "로컬 회원가입 응답")
    @Builder
    public record SignUp(
            @Schema(description = "회원 ID", example = "1")
            Long memberId,

            @Schema(description = "닉네임", example = "아필굿")
            String nickname,

            @Schema(description = "아이디", example = "demouser")
            String username,

            @Schema(description = "이메일", example = "demo@ipillgood.com")
            String email,

            @Schema(description = "가입 시 지정된 기본 프로필 이미지 URL")
            String profileImageUrl,

            @Schema(description = "온보딩 완료 여부. 가입 직후에는 항상 false", example = "false")
            Boolean onboardingCompleted,

            @Schema(description = "가입일시", example = "2026-08-11T10:00:00")
            LocalDateTime createdAt
    ) {
    }

    /**
     * 로컬 로그인 응답
     */
    @Schema(description = "로컬 로그인 응답")
    @Builder
    public record Login(
            @Schema(description = "액세스 토큰. Authorization 헤더에 Bearer로 전달",
                    example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwicm9sZSI6IlVTRVIifQ.xxxxx")
            String accessToken,

            @Schema(description = "토큰 타입", example = "Bearer")
            String tokenType,

            @Schema(description = "액세스 토큰 만료까지 남은 시간(초)", example = "3600")
            Long expiresIn,

            @Schema(description = "회원 ID", example = "1")
            Long memberId,

            @Schema(description = "온보딩 완료 여부. false면 온보딩 화면으로 유도", example = "false")
            Boolean onboardingCompleted
    ) {
    }

    /**
     * 소셜 회원가입 응답
     * 회원가입과 동시에 로그인 처리되므로 로그인 토큰을 함께 담음
     */
    @Schema(description = "소셜 회원가입 응답")
    @Builder
    public record SocialSignUp(
            @Schema(description = "회원 ID", example = "2")
            Long memberId,

            @Schema(description = "가입에 사용한 소셜 제공자. KAKAO 또는 NAVER", example = "KAKAO")
            SocialProvider provider,

            @Schema(description = "닉네임. 소셜 프로필에서 가져온 값", example = "아필굿")
            String nickname,

            @Schema(description = "이메일. 소셜 프로필에서 가져온 값", example = "demo@kakao.com")
            String email,

            @Schema(description = "가입 시 지정된 기본 프로필 이미지 URL")
            String profileImageUrl,

            @Schema(description = "온보딩 완료 여부. 가입 직후에는 항상 false", example = "false")
            Boolean onboardingCompleted,

            @Schema(description = "가입일시", example = "2026-08-11T10:00:00")
            LocalDateTime createdAt,

            @Schema(description = "액세스 토큰. 가입과 동시에 로그인 처리되어 함께 발급",
                    example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyIiwicm9sZSI6IlVTRVIifQ.xxxxx")
            String accessToken,

            @Schema(description = "토큰 타입", example = "Bearer")
            String tokenType,

            @Schema(description = "액세스 토큰 만료까지 남은 시간(초)", example = "3600")
            Long expiresIn
    ) {
    }

    /**
     * 소셜 계정 연동 응답
     * 연동 즉시 로그인 처리하므로 로그인 토큰을 함께 발급
     */
    @Schema(description = "소셜 계정 연동 응답")
    @Builder
    public record SocialLink(
            @Schema(description = "연동 성공 여부. 항상 true", example = "true")
            Boolean linked,

            @Schema(description = "연동된 소셜 제공자. KAKAO 또는 NAVER", example = "KAKAO")
            SocialProvider provider,

            @Schema(description = "연동이 기록된 시각", example = "2026-08-11T10:00:00")
            LocalDateTime linkedAt,

            @Schema(description = "액세스 토큰. 연동과 동시에 로그인 처리되어 함께 발급",
                    example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwicm9sZSI6IlVTRVIifQ.xxxxx")
            String accessToken,

            @Schema(description = "토큰 타입", example = "Bearer")
            String tokenType,

            @Schema(description = "액세스 토큰 만료까지 남은 시간(초)", example = "3600")
            Long expiresIn,

            @Schema(description = "회원 ID", example = "1")
            Long memberId,

            @Schema(description = "온보딩 완료 여부. false면 온보딩 화면으로 유도", example = "true")
            Boolean onboardingCompleted
    ) {
    }
}
