package com.ipillgood.server.domain.policy.dto;

import com.ipillgood.server.domain.policy.entity.enums.PolicyDocumentType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class PolicyResponse {

    // 마이페이지 약관 조회 항목
    @Builder
    public record DocumentDetail(
            Long policyDocumentId,
            PolicyDocumentType documentType,
            String title,
            String content,
            Boolean required,
            String version,
            LocalDateTime effectiveAt
    ) {
    }

    // 마이페이지 약관 조회 응답
    @Builder
    public record LatestDocuments(
            List<DocumentDetail> documents
    ) {
    }
}
