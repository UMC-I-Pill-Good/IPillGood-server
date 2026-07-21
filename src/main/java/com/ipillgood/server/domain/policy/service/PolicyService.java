package com.ipillgood.server.domain.policy.service;

import com.ipillgood.server.domain.policy.converter.PolicyConverter;
import com.ipillgood.server.domain.policy.dto.PolicyResponse;
import com.ipillgood.server.domain.policy.entity.PolicyDocument;
import com.ipillgood.server.domain.policy.entity.enums.PolicyDocumentType;
import com.ipillgood.server.domain.policy.repository.PolicyDocumentRepository;
import com.ipillgood.server.global.apiPayload.code.GeneralErrorCode;
import com.ipillgood.server.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PolicyService {

    private final PolicyDocumentRepository policyDocumentRepository;

    // 약관/정책 목록 조회 (활성 여부 필터)
    public PolicyResponse.DocumentList getDocuments(Boolean activeOnly) {

        boolean onlyActive = activeOnly == null || activeOnly;
        List<PolicyDocument> documents = onlyActive
                ? policyDocumentRepository.findByActiveTrue()
                : policyDocumentRepository.findAll();

        List<PolicyDocument> sorted = documents.stream()
                .sorted(Comparator.comparing(PolicyDocument::getId))
                .toList();

        return PolicyConverter.toDocumentList(sorted);
    }

    // 유형별 최신 약관/정책 조회 (마이페이지 > 설정 > 개인정보 처리방침/이용약관)
    // 해당 유형의 활성 문서 중 effectiveAt이 가장 늦은(최신본) 문서 1개 조회
    public PolicyResponse.LatestDocuments getLatest(PolicyDocumentType documentType) {
        List<PolicyDocument> latest = policyDocumentRepository.findByActiveTrue().stream()
                .filter(d -> d.getDocumentType() == documentType)
                .max(Comparator.comparing(PolicyDocument::getEffectiveAt))
                .map(List::of)
                .orElse(List.of());

        return PolicyConverter.toLatestDocuments(latest);
    }

    // 약관/정책 상세 조회 (비활성 또는 미존재 시 404)
    public PolicyResponse.DocumentDetail getDocument(Long policyDocumentId) {
        PolicyDocument document = policyDocumentRepository.findById(policyDocumentId)
                .filter(PolicyDocument::isActive)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND));

        return PolicyConverter.toDetail(document);
    }
}
