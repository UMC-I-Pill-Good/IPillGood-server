package com.ipillgood.server.domain.policy.controller.docs;

import com.ipillgood.server.domain.policy.dto.PolicyResponse;
import com.ipillgood.server.domain.policy.entity.enums.PolicyDocumentType;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 약관/정책 관련 API 문서
 * 약관/정책 목록/최신/상세 조회
 */
@Tag(name = "Policy API", description = "약관/정책 조회 관련 API")
public interface PolicyApi {

    @Operation(summary = "약관/정책 목록 조회",
            description = "활성 약관/정책 목록을 조회합니다.")
    ApiResponse<PolicyResponse.DocumentList> getDocuments();

    @Operation(summary = "최신 약관/정책 조회",
            description = "문서 유형 기준 최신 약관/정책을 본문과 함께 조회합니다.")
    ApiResponse<PolicyResponse.LatestDocuments> getLatest(PolicyDocumentType documentType);

    @Operation(summary = "약관/정책 상세 조회",
            description = "특정 약관/정책 문서의 본문을 조회합니다.")
    ApiResponse<PolicyResponse.DocumentDetail> getDocument(Long policyDocumentId);
}
