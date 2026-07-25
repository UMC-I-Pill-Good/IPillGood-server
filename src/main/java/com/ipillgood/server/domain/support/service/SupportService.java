package com.ipillgood.server.domain.support.service;

import com.ipillgood.server.domain.support.converter.SupportConverter;
import com.ipillgood.server.domain.support.dto.SupportResponse;
import com.ipillgood.server.domain.support.entity.Faq;
import com.ipillgood.server.domain.support.entity.enums.FaqCategory;
import com.ipillgood.server.domain.support.repository.FaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupportService {

    // 문의/고객센터 화면의 FAQ 미리보기 개수
    private static final int SUPPORT_FAQ_PREVIEW_SIZE = 3;

    // 문의처 정보 - 서버 상수 (관리자 화면에서 편집하는 값이 아님, 변경 시 배포 필요)
    private static final String CONTACT_EMAIL = "ipillgood.official@gmail.com";
    private static final String OPERATING_HOURS = "평일 09:00 ~ 18:00";
    private static final String CLOSED_DAYS = "주말 및 공휴일 휴무";

    private final FaqRepository faqRepository;

    /**
     * FAQ 목록 화면
     * category와 keyword를 둘 다 입력하면서, 일치하지 않는 상황 필터링
     * (예: category = INTAKE / keyword = "알림" -> 필터)
     * 페이지네이션 없이 전체 반환하는 방식 - faq는 데이터 수가 많지 않기 때문
     */
    public SupportResponse.FaqList getFaqs(FaqCategory category, String keyword) {
        List<Faq> faqs = faqRepository.findByActiveTrueOrderByDisplayOrderAsc().stream()
                .filter(faq -> category == null || faq.getCategory() == category)
                .filter(faq -> matchesKeyword(faq, keyword))
                .toList();

        return SupportConverter.toFaqList(faqs);
    }

    /**
     * 문의/고객센터 화면
     * FAQ 상위 3개 미리보기 + 문의처 정보 조회
     */
    public SupportResponse.Info getSupportInfo() {
        List<Faq> faqPreview = faqRepository.findByActiveTrueOrderByDisplayOrderAsc().stream()
                .limit(SUPPORT_FAQ_PREVIEW_SIZE)
                .toList();

        return SupportConverter.toInfo(faqPreview, CONTACT_EMAIL, OPERATING_HOURS, CLOSED_DAYS);
    }

    private boolean matchesKeyword(Faq faq, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        return faq.getQuestion().toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }
}
