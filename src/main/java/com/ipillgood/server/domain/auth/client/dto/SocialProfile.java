package com.ipillgood.server.domain.auth.client.dto;

/**
 * 각 소셜 제공자(카카오, 네이버)마다 서로 다른 JSON 구조를
 * 통일된 포맷으로 변환(Adapter)하여 관리하기 위한 DTO 클래스
 * 인증 과정에서만 사용하고 폐기 (DB 저장 X)
 */
public record SocialProfile(

        // 소셜 제공자가 발급한 사용자 고유 ID
        String providerUserId,

        // 사용자가 이메일 제공에 동의 안하면 null이 될 수도 있음
        String email
) {
}
