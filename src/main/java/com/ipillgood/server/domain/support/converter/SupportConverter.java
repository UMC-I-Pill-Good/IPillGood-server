package com.ipillgood.server.domain.support.converter;

import com.ipillgood.server.domain.support.dto.AdminFaqResponse;
import com.ipillgood.server.domain.support.dto.SupportResponse;
import com.ipillgood.server.domain.support.entity.Faq;
import org.springframework.data.domain.Page;

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

    /**
     * 관리자 FAQ 목록 조회 - 페이징된 엔티티 목록 -> DTO 변환
     */
    public static AdminFaqResponse.FaqList toAdminFaqList(Page<Faq> faqPage) {
        return AdminFaqResponse.FaqList.builder()
                .faqs(faqPage.getContent().stream()
                        .map(SupportConverter::toFaqSummary)
                        .toList())
                .totalCount(faqPage.getTotalElements())
                .totalPages(faqPage.getTotalPages())
                .currentPage(faqPage.getNumber())
                .build();
    }

    private static AdminFaqResponse.FaqSummary toFaqSummary(Faq faq) {
        return AdminFaqResponse.FaqSummary.builder()
                .faqId(faq.getId())
                .question(faq.getQuestion())
                .answer(faq.getAnswer())
                .category(faq.getCategory())
                .updatedAt(faq.getUpdatedAt())
                .build();
    }

    /**
     * 관리자 FAQ 등록 - 엔티티 -> DTO 변환
     */
    public static AdminFaqResponse.FaqCreated toFaqCreated(Faq faq) {
        return AdminFaqResponse.FaqCreated.builder()
                .faqId(faq.getId())
                .question(faq.getQuestion())
                .answer(faq.getAnswer())
                .category(faq.getCategory())
                .createdAt(faq.getCreatedAt())
                .build();
    }

    /**
     * 관리자 FAQ 수정 - 엔티티 -> DTO 변환
     */
    public static AdminFaqResponse.FaqUpdated toFaqUpdated(Faq faq) {
        return AdminFaqResponse.FaqUpdated.builder()
                .faqId(faq.getId())
                .question(faq.getQuestion())
                .answer(faq.getAnswer())
                .category(faq.getCategory())
                .updatedAt(faq.getUpdatedAt())
                .build();
    }
}
