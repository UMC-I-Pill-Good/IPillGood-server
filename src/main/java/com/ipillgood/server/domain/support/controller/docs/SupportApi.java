package com.ipillgood.server.domain.support.controller.docs;

import com.ipillgood.server.domain.support.dto.SupportResponse;
import com.ipillgood.server.domain.support.entity.enums.FaqCategory;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 고객지원 관련 API 문서
 */
@Tag(name = "Support API", description = "FAQ/문의·고객센터 관련 API")
public interface SupportApi {

    @Operation(summary = "FAQ 목록 조회",
            description = "FAQ 목록을 카테고리와 키워드 조건으로 조회합니다. 키워드는 부분 일치로 검색합니다.")
    ApiResponse<SupportResponse.FaqList> getFaqs(FaqCategory category, String keyword);
}
