package com.ipillgood.server.domain.support.converter;

import com.ipillgood.server.domain.support.dto.SupportResponse;
import com.ipillgood.server.domain.support.entity.Faq;

import java.util.List;

public class SupportConverter {

    private SupportConverter() {
    }

    /**
     * FAQ 목록 조회 - 엔티티 목록 -> DTO 변환
     */
    public static SupportResponse.FaqList toFaqList(List<Faq> faqs) {
        return SupportResponse.FaqList.builder()
                .faqs(faqs.stream()
                        .map(SupportConverter::toFaqItem)
                        .toList())
                .build();
    }

    private static SupportResponse.FaqItem toFaqItem(Faq faq) {
        return SupportResponse.FaqItem.builder()
                .faqId(faq.getId())
                .category(faq.getCategory())
                .question(faq.getQuestion())
                .answer(faq.getAnswer())
                .build();
    }
}
