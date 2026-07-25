package com.ipillgood.server.domain.support.controller;

import com.ipillgood.server.domain.support.code.SupportSuccessCode;
import com.ipillgood.server.domain.support.controller.docs.SupportApi;
import com.ipillgood.server.domain.support.dto.SupportResponse;
import com.ipillgood.server.domain.support.entity.enums.FaqCategory;
import com.ipillgood.server.domain.support.service.SupportService;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/support")
public class SupportController implements SupportApi {

    private final SupportService supportService;

    // FAQ 목록 조회
    @Override
    @GetMapping("/faqs")
    public ApiResponse<SupportResponse.FaqList> getFaqs(
            @RequestParam(required = false) FaqCategory category,
            @RequestParam(required = false) String keyword) {
        SupportResponse.FaqList response = supportService.getFaqs(category, keyword);
        return ApiResponse.onSuccess(SupportSuccessCode.FAQ_LIST_FOUND, response);
    }
}
