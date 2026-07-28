package com.ipillgood.server.domain.support.service;

import com.ipillgood.server.domain.support.converter.SupportConverter;
import com.ipillgood.server.domain.support.dto.AdminFaqRequest;
import com.ipillgood.server.domain.support.dto.AdminFaqResponse;
import com.ipillgood.server.domain.support.entity.Faq;
import com.ipillgood.server.domain.support.entity.enums.FaqCategory;
import com.ipillgood.server.domain.support.repository.FaqRepository;
import com.ipillgood.server.global.apiPayload.code.GeneralErrorCode;
import com.ipillgood.server.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminFaqService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final FaqRepository faqRepository;

    /**
     * 관리자 FAQ 목록 조회
     * category, keyword(질문 부분 일치) 조건으로 페이지네이션 조회
     */
    public AdminFaqResponse.FaqList getFaqs(String categoryValue, String keyword, String pageValue, String sizeValue) {
        FaqCategory category = parseCategory(categoryValue);
        int page = parsePage(pageValue);
        int size = parseSize(sizeValue);

        Page<Faq> faqPage = faqRepository.searchForAdmin(
                category, normalizeKeyword(keyword), PageRequest.of(page, size));

        return SupportConverter.toAdminFaqList(faqPage);
    }

    /**
     * 관리자 FAQ 등록
     * 사용자 화면 노출 순서는 기존 마지막 순번 다음으로 배정하고, 등록 즉시 노출(active=true)한다.
     */
    @Transactional
    public AdminFaqResponse.FaqCreated createFaq(AdminFaqRequest.Upsert request) {
        int nextDisplayOrder = faqRepository.findMaxDisplayOrder() + 1;

        Faq faq = faqRepository.save(Faq.builder()
                .category(request.category())
                .question(request.question())
                .answer(request.answer())
                .displayOrder(nextDisplayOrder)
                .active(true)
                .build());

        return SupportConverter.toFaqCreated(faq);
    }

    /**
     * 관리자 FAQ 수정
     */
    @Transactional
    public AdminFaqResponse.FaqUpdated updateFaq(Long faqId, AdminFaqRequest.Upsert request) {
        Faq faq = getFaq(faqId);
        faq.updateContent(request.category(), request.question(), request.answer());

        return SupportConverter.toFaqUpdated(faq);
    }

    /**
     * 관리자 FAQ 삭제 - 복구 불가능한 영구 삭제
     */
    @Transactional
    public void deleteFaq(Long faqId) {
        Faq faq = getFaq(faqId);
        faqRepository.delete(faq);
    }

    private Faq getFaq(Long faqId) {
        return faqRepository.findById(faqId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND));
    }

    private FaqCategory parseCategory(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return FaqCategory.valueOf(value.trim());
        } catch (IllegalArgumentException e) {
            throw new GeneralException(GeneralErrorCode.VALID_FAIL);
        }
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim().toLowerCase(Locale.ROOT);
    }

    private int parsePage(String value) {
        if (value == null || value.isBlank()) {
            return DEFAULT_PAGE;
        }
        int page = parsePageInteger(value);
        if (page < 0) {
            throw new GeneralException(GeneralErrorCode.INVALID_PAGE);
        }
        return page;
    }

    private int parseSize(String value) {
        if (value == null || value.isBlank()) {
            return DEFAULT_SIZE;
        }
        int size = parsePageInteger(value);
        if (size < 1 || size > MAX_SIZE) {
            throw new GeneralException(GeneralErrorCode.INVALID_PAGE);
        }
        return size;
    }

    private int parsePageInteger(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new GeneralException(GeneralErrorCode.INVALID_PAGE);
        }
    }
}
