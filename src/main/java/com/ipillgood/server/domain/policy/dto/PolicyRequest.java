package com.ipillgood.server.domain.policy.dto;

import jakarta.validation.constraints.NotNull;

public class PolicyRequest {

    /**
     * 약관 동의 항목
     * 로컬/소셜 회원가입 요청 시 사용
     */
    public record Agreement(
            @NotNull(message = "약관 문서 ID가 필요합니다.")
            Long policyDocumentId,

            @NotNull(message = "약관 동의 여부가 필요합니다.")
            Boolean agreed
    ) {
    }
}
