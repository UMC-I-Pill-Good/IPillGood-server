package com.ipillgood.server.domain.support.dto;

import com.ipillgood.server.domain.support.entity.enums.FaqCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

public class SupportResponse {

    /**
     * FAQ 목록, 문의·고객센터 조회 공통 항목
     */
    @Schema(description = "FAQ 목록, 문의·고객센터 조회 공통 항목")
    @Builder
    public record FaqItem(
            @Schema(description = "FAQ ID", example = "2")
            Long faqId,

            @Schema(description = "카테고리. RECOMMENDATION_INGREDIENT(추천/성분), INTAKE(복용/섭취), "
                    + "NOTIFICATION(알림), ETC(기타)", example = "INTAKE")
            FaqCategory category,

            @Schema(description = "질문", example = "영양제를 언제 먹는 게 좋나요?")
            String question,

            @Schema(description = "답변", example = "성분에 따라 달라요. 지용성 비타민은 식후에 드시는 것을 권장해요.")
            String answer
    ) {
    }

    /**
     * FAQ 목록 조회 응답
     */
    @Schema(description = "FAQ 목록 조회 응답")
    @Builder
    public record FaqList(
            @Schema(description = "조회 조건에 맞는 활성 FAQ 목록. 조건에 맞는 FAQ가 없으면 빈 배열")
            List<FaqItem> faqs
    ) {
    }

    /**
     * 문의/고객센터 조회 응답
     */
    @Schema(description = "문의/고객센터 조회 응답")
    @Builder
    public record Info(
            @Schema(description = "고객센터 화면에 노출할 활성 FAQ 목록")
            List<FaqItem> faqs,

            @Schema(description = "문의 접수 이메일", example = "ipillgood.official@gmail.com")
            String contactEmail,

            @Schema(description = "고객센터 운영 시간", example = "평일 09:00 ~ 18:00")
            String operatingHours,

            @Schema(description = "고객센터 휴무일", example = "주말 및 공휴일 휴무")
            String closedDays
    ) {
    }
}
