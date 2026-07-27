package com.ipillgood.server.domain.support.dto;

import com.ipillgood.server.domain.support.entity.enums.FaqCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

public class AdminFaqRequest {

    /**
     * FAQ 등록/수정 공통 요청
     */
    @Builder
    public record Upsert(
            @Schema(description = "질문", example = "복용 알림은 어떻게 설정하나요?")
            @NotBlank(message = "질문을 입력해주세요.")
            @Size(max = 200, message = "질문은 200자 이내로 입력해주세요.")
            String question,

            @Schema(description = "답변", example = "마이페이지 > 설정 > 복용 시간 알림에서 설정할 수 있습니다.")
            @NotBlank(message = "답변을 입력해주세요.")
            @Size(max = 2000, message = "답변은 2000자 이내로 입력해주세요.")
            String answer,

            @Schema(description = "카테고리", example = "NOTIFICATION")
            @NotNull(message = "카테고리를 선택해주세요.")
            FaqCategory category
    ) {
    }
}
