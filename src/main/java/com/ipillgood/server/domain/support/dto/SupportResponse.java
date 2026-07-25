package com.ipillgood.server.domain.support.dto;

import com.ipillgood.server.domain.support.entity.enums.FaqCategory;
import lombok.Builder;

import java.util.List;

public class SupportResponse {

    /**
     * FAQ 목록, 문의·고객센터 조회 공통 항목
     */
    @Builder
    public record FaqItem(
            Long faqId,
            FaqCategory category,
            String question,
            String answer
    ) {
    }

    /**
     * FAQ 목록 조회 응답
     */
    @Builder
    public record FaqList(
            List<FaqItem> faqs
    ) {
    }
}
