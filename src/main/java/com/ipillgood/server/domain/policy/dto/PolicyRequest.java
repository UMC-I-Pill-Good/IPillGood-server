package com.ipillgood.server.domain.policy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public class PolicyRequest {

    /**
     * 약관 동의 항목
     * 로컬/소셜 회원가입 요청 시 사용
     */
    @Schema(description = "약관 동의 항목")
    public record Agreement(
            @Schema(description = "약관 문서 ID", example = "1")
            @NotNull(message = "약관 문서 ID가 필요합니다.")
            Long policyDocumentId,

            @Schema(description = "동의 여부", example = "true")
            @NotNull(message = "약관 동의 여부가 필요합니다.")
            Boolean agreed
    ) {
    }
}
