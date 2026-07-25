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

    private boolean matchesKeyword(Faq faq, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        return faq.getQuestion().toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }
}
