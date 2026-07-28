package com.ipillgood.server.domain.support.dto;

import com.ipillgood.server.domain.support.entity.enums.FaqCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class AdminFaqResponse {

    /**
     * 관리자 FAQ 목록 조회 응답
     */
    @Builder
    public record FaqList(
            @Schema(description = "FAQ 목록")
            List<FaqSummary> faqs,

            @Schema(description = "검색 조건에 맞는 전체 FAQ 수", example = "12")
            Long totalCount,

            @Schema(description = "전체 페이지 수", example = "1")
            Integer totalPages,

            @Schema(description = "현재 페이지 번호", example = "0")
            Integer currentPage
    ) {
    }

    /**
     * 관리자 FAQ 목록 항목
     */
    @Builder
    public record FaqSummary(
            @Schema(description = "FAQ ID", example = "12")
            Long faqId,

            @Schema(description = "질문", example = "회원 탈퇴는 어떻게 하나요?")
            String question,

            @Schema(description = "답변", example = "마이페이지 > 회원 정보 > 탈퇴하기에서 진행할 수 있습니다.")
            String answer,

            @Schema(description = "카테고리", example = "ETC")
            FaqCategory category,

            @Schema(description = "수정일", example = "2026-07-06T10:00:00")
            LocalDateTime updatedAt
    ) {
    }

    /**
     * 관리자 FAQ 등록 응답
     */
    @Builder
    public record FaqCreated(
            @Schema(description = "등록된 FAQ ID", example = "13")
            Long faqId,

            @Schema(description = "질문", example = "복용 알림은 어떻게 설정하나요?")
            String question,

            @Schema(description = "답변", example = "마이페이지 > 설정 > 복용 시간 알림에서 설정할 수 있습니다.")
            String answer,

            @Schema(description = "카테고리", example = "NOTIFICATION")
            FaqCategory category,

            @Schema(description = "등록일", example = "2026-07-22T18:00:00")
            LocalDateTime createdAt
    ) {
    }

    /**
     * 관리자 FAQ 수정 응답
     */
    @Builder
    public record FaqUpdated(
            @Schema(description = "FAQ ID", example = "13")
            Long faqId,

            @Schema(description = "질문", example = "복용 알림은 어떻게 설정하나요?")
            String question,

            @Schema(description = "답변", example = "마이페이지 > 설정 > 복용 시간 알림에서 설정할 수 있습니다.")
            String answer,

            @Schema(description = "카테고리", example = "NOTIFICATION")
            FaqCategory category,

            @Schema(description = "수정일", example = "2026-07-22T18:10:00")
            LocalDateTime updatedAt
    ) {
    }
}
