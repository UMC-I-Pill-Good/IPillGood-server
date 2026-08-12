package com.ipillgood.server.domain.policy.dto;

import com.ipillgood.server.domain.policy.entity.enums.PolicyDocumentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class PolicyResponse {

    /**
     * 마이페이지 약관 조회 항목
     */
    @Schema(description = "마이페이지 약관 조회 항목")
    @Builder
    public record DocumentDetail(
            @Schema(description = "약관 문서 ID", example = "1")
            Long policyDocumentId,

            @Schema(description = "문서 유형. SERVICE_TERMS(서비스 이용약관), PRIVACY_COLLECTION(개인정보 수집 및 이용), "
                    + "HEALTH_INFO_COLLECTION(건강 정보 수집 및 이용), MARKETING(마케팅 정보 수신)",
                    example = "SERVICE_TERMS")
            PolicyDocumentType documentType,

            @Schema(description = "문서 제목", example = "서비스 이용약관")
            String title,

            @Schema(description = "약관 전문", example = "아필굿 서비스 이용약관\n제1조(목적) ...")
            String content,

            @Schema(description = "필수 동의 여부", example = "true")
            Boolean required,

            @Schema(description = "문서 버전", example = "v1.0")
            String version,

            @Schema(description = "시행일시", example = "2026-08-01T00:00:00")
            LocalDateTime effectiveAt
    ) {
    }

    /**
     * 마이페이지 약관 조회 응답
     */
    @Schema(description = "마이페이지 약관 조회 응답")
    @Builder
    public record LatestDocuments(
            @Schema(description = "조회한 유형의 최신 약관 문서. 활성 문서가 없으면 빈 배열")
            List<DocumentDetail> documents
    ) {
    }
}
