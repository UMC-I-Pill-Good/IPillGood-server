package com.ipillgood.server.domain.policy.controller;

import com.ipillgood.server.domain.policy.code.PolicySuccessCode;
import com.ipillgood.server.domain.policy.controller.docs.PolicyApi;
import com.ipillgood.server.domain.policy.dto.PolicyResponse;
import com.ipillgood.server.domain.policy.entity.enums.PolicyDocumentType;
import com.ipillgood.server.domain.policy.service.PolicyService;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/policy-documents")
public class PolicyController implements PolicyApi {

    private final PolicyService policyService;

    // 약관/정책 목록 조회
    @Override
    @GetMapping
    public ApiResponse<PolicyResponse.DocumentList> getDocuments() {
        PolicyResponse.DocumentList response = policyService.getDocuments();
        return ApiResponse.onSuccess(PolicySuccessCode.POLICY_DOCUMENTS_FOUND, response);
    }

    // 최신 약관/정책 조회
    @Override
    @GetMapping("/latest")
    public ApiResponse<PolicyResponse.LatestDocuments> getLatest(@RequestParam PolicyDocumentType documentType) {
        PolicyResponse.LatestDocuments response = policyService.getLatest(documentType);
        return ApiResponse.onSuccess(PolicySuccessCode.POLICY_LATEST_FOUND, response);
    }

    // 약관/정책 상세 조회
    @Override
    @GetMapping("/{policyDocumentId}")
    public ApiResponse<PolicyResponse.DocumentDetail> getDocument(@PathVariable Long policyDocumentId) {
        PolicyResponse.DocumentDetail response = policyService.getDocument(policyDocumentId);
        return ApiResponse.onSuccess(PolicySuccessCode.POLICY_DOCUMENT_FOUND, response);
    }
}
