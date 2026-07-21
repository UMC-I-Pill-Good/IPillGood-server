package com.ipillgood.server.domain.policy.converter;

import com.ipillgood.server.domain.policy.dto.PolicyResponse;
import com.ipillgood.server.domain.policy.entity.PolicyDocument;

import java.util.List;

public class PolicyConverter {

    // 목록 조회 - 엔티티 -> 요약 DTO 변환
    public static PolicyResponse.DocumentSummary toSummary(PolicyDocument document) {
        return PolicyResponse.DocumentSummary.builder()
                .policyDocumentId(document.getId())
                .documentType(document.getDocumentType())
                .title(document.getTitle())
                .required(document.isRequired())
                .version(document.getVersion())
                .effectiveAt(document.getEffectiveAt())
                .build();
    }

    // 상세/최신 조회 - 엔티티 -> 상세 DTO 변환
    public static PolicyResponse.DocumentDetail toDetail(PolicyDocument document) {
        return PolicyResponse.DocumentDetail.builder()
                .policyDocumentId(document.getId())
                .documentType(document.getDocumentType())
                .title(document.getTitle())
                .content(document.getContent())
                .required(document.isRequired())
                .version(document.getVersion())
                .effectiveAt(document.getEffectiveAt())
                .build();
    }

    // 목록 조회 - 응답 리스트
    public static PolicyResponse.DocumentList toDocumentList(List<PolicyDocument> documents) {
        return PolicyResponse.DocumentList.builder()
                .documents(documents.stream().map(PolicyConverter::toSummary).toList())
                .build();
    }

    // 최신 조회 - 응답 리스트
    public static PolicyResponse.LatestDocuments toLatestDocuments(List<PolicyDocument> documents) {
        return PolicyResponse.LatestDocuments.builder()
                .documents(documents.stream().map(PolicyConverter::toDetail).toList())
                .build();
    }
}
