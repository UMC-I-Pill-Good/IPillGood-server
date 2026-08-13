package com.ipillgood.server.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

public class MemberResponse {

    /**
     * 내 정보 조회 응답
     */
    @Schema(description = "내 정보 조회 응답")
    @Builder
    public record MyInfo(
            @Schema(description = "회원 ID", example = "1")
            Long memberId,

            @Schema(description = "닉네임", example = "아필굿")
            String nickname,

            @Schema(description = "프로필 이미지 URL")
            String profileImageUrl,

            @Schema(description = "이 회원이 쓸 수 있는 로그인 수단. LOCAL·KAKAO·NAVER 중 최소 1개",
                    example = "[\"LOCAL\"]")
            List<String> loginProviders,

            @Schema(description = "온보딩 완료 여부. false면 온보딩 화면으로 유도", example = "true")
            Boolean onboardingCompleted
    ) {
    }

    /**
     * 프로필 수정 응답
     */
    @Schema(description = "프로필 수정 응답")
    @Builder
    public record ProfileUpdated(
            @Schema(description = "회원 ID", example = "1")
            Long memberId,

            @Schema(description = "수정된 닉네임", example = "아필굿")
            String nickname
    ) {
    }
}
