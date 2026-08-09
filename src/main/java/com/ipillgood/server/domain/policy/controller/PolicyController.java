package com.ipillgood.server.domain.policy.controller;

import com.ipillgood.server.domain.policy.code.PolicySuccessCode;
import com.ipillgood.server.domain.policy.controller.docs.PolicyApi;
import com.ipillgood.server.domain.policy.dto.PolicyResponse;
import com.ipillgood.server.domain.policy.entity.enums.PolicyDocumentType;
import com.ipillgood.server.domain.policy.service.PolicyService;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/policy-documents")
public class PolicyController implements PolicyApi {

    private final PolicyService policyService;

    // 최신 약관/정책 조회
    @Override
    @GetMapping("/latest")
    public ApiResponse<PolicyResponse.LatestDocuments> getLatest(@RequestParam PolicyDocumentType documentType) {
        PolicyResponse.LatestDocuments response = policyService.getLatest(documentType);
        return ApiResponse.onSuccess(PolicySuccessCode.POLICY_LATEST_FOUND, response);
    }
}
