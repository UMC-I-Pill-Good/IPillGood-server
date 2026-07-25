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

    /**
     * 문의/고객센터 조회 - FAQ 미리보기 엔티티 목록 + 문의처 정보 -> DTO 변환
     */
    public static SupportResponse.Info toInfo(List<Faq> faqs, String contactEmail,
                                              String operatingHours, String closedDays) {
        return SupportResponse.Info.builder()
                .faqs(faqs.stream()
                        .map(SupportConverter::toFaqItem)
                        .toList())
                .contactEmail(contactEmail)
                .operatingHours(operatingHours)
                .closedDays(closedDays)
                .build();
    }
}
