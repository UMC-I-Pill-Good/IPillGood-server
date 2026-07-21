package com.ipillgood.server.domain.policy.dto;

import com.ipillgood.server.domain.policy.entity.enums.PolicyDocumentType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class PolicyResponse {

    // 목록 항목
    @Builder
    public record DocumentSummary(
            Long policyDocumentId,
            PolicyDocumentType documentType,
            String title,
            Boolean required,
            String version,
            LocalDateTime effectiveAt
    ) {
    }

    // 약관 상세+최신 항목 (자세히 보기 및 설정 페이지 속 지침)
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

    // 목록 조회 응답
    @Builder
    public record DocumentList(
            List<DocumentSummary> documents
    ) {
    }

    // 최신 조회 응답
    @Builder
    public record LatestDocuments(
            List<DocumentDetail> documents
    ) {
    }
}
