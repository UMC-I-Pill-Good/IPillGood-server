package com.ipillgood.server.domain.support.controller;

import com.ipillgood.server.domain.support.code.SupportSuccessCode;
import com.ipillgood.server.domain.support.controller.docs.AdminFaqApi;
import com.ipillgood.server.domain.support.dto.AdminFaqRequest;
import com.ipillgood.server.domain.support.dto.AdminFaqResponse;
import com.ipillgood.server.domain.support.service.AdminFaqService;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/faqs")
public class AdminFaqController implements AdminFaqApi {

    private final AdminFaqService adminFaqService;

    // 관리자 FAQ 목록 조회
    @Override
    @GetMapping
    public ApiResponse<AdminFaqResponse.FaqList> getFaqs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String page,
            @RequestParam(required = false) String size
    ) {
        AdminFaqResponse.FaqList response = adminFaqService.getFaqs(category, keyword, page, size);
        return ApiResponse.onSuccess(SupportSuccessCode.ADMIN_FAQ_LIST_FOUND, response);
    }

    // 관리자 FAQ 등록
    @Override
    @PostMapping
    public ApiResponse<AdminFaqResponse.FaqCreated> createFaq(
            @Valid @RequestBody AdminFaqRequest.Upsert request
    ) {
        AdminFaqResponse.FaqCreated response = adminFaqService.createFaq(request);
        return ApiResponse.onSuccess(SupportSuccessCode.ADMIN_FAQ_CREATED, response);
    }

    // 관리자 FAQ 수정
    @Override
    @PutMapping("/{faqId}")
    public ApiResponse<AdminFaqResponse.FaqUpdated> updateFaq(
            @PathVariable Long faqId,
            @Valid @RequestBody AdminFaqRequest.Upsert request
    ) {
        AdminFaqResponse.FaqUpdated response = adminFaqService.updateFaq(faqId, request);
        return ApiResponse.onSuccess(SupportSuccessCode.ADMIN_FAQ_UPDATED, response);
    }

    // 관리자 FAQ 삭제
    @Override
    @DeleteMapping("/{faqId}")
    public ApiResponse<Void> deleteFaq(@PathVariable Long faqId) {
        adminFaqService.deleteFaq(faqId);
        return ApiResponse.onSuccess(SupportSuccessCode.ADMIN_FAQ_DELETED, null);
    }
}
