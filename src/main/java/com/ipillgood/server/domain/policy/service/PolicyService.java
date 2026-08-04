package com.ipillgood.server.domain.policy.service;

import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.policy.code.PolicyErrorCode;
import com.ipillgood.server.domain.policy.converter.PolicyConverter;
import com.ipillgood.server.domain.policy.dto.PolicyRequest;
import com.ipillgood.server.domain.policy.dto.PolicyResponse;
import com.ipillgood.server.domain.policy.entity.MemberPolicyAgreement;
import com.ipillgood.server.domain.policy.entity.PolicyDocument;
import com.ipillgood.server.domain.policy.entity.enums.PolicyDocumentType;
import com.ipillgood.server.domain.policy.exception.PolicyException;
import com.ipillgood.server.domain.policy.repository.MemberPolicyAgreementRepository;
import com.ipillgood.server.domain.policy.repository.PolicyDocumentRepository;
import com.ipillgood.server.global.apiPayload.code.GeneralErrorCode;
import com.ipillgood.server.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PolicyService {

    private final PolicyDocumentRepository policyDocumentRepository;
    private final MemberPolicyAgreementRepository memberPolicyAgreementRepository;

    // 약관/정책 목록 조회 (활성 문서만)
    public PolicyResponse.DocumentList getDocuments() {

        List<PolicyDocument> sorted = policyDocumentRepository.findByActiveTrue().stream()
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

    /**
     * 소셜 회원가입 시 토큰 제거에 앞서, 회원이 제출한 약관 동의 목록 확인을 먼저 진행
     * 약관 동의 검증에 문제가 없는 경우(필수 약관/빈 값 등) 토큰 제거로 이어짐
     */
    @Transactional(readOnly = true)
    public void validateAgreements(List<PolicyRequest.Agreement> agreements) {

        // 약관 목록이 아예 비어있는 경우 차단
        if (agreements == null || agreements.isEmpty()) {
            throw new PolicyException(PolicyErrorCode.REQUIRED_TERMS_NOT_AGREED);
        }

        Map<Long, Boolean> agreedByDocument = toAgreementMap(agreements);

        // 약관들이 전부 활성 문서인지 확인
        findActiveDocuments(agreedByDocument.keySet());

        // 필수 약관 동의 확인
        validateRequiredAgreements(agreedByDocument);
    }

    /**
     * 로컬/소셜 회원가입 시 실행하는 메서드
     * 제출한 약관 동의 목록 검증 + 동의 이력 저장 (약관 처리의 단일 진입점)
     * agreeToPolicies는 member의 signUp 트랜잭션에 묶여 있음
     * -> 약관 검증과 이력 저장 로직에서 문제가 생길 경우, 저장한 회원도 같이 롤백 처리되므로 안전한 구조
     */
    @Transactional
    public void agreeToPolicies(Member member, List<PolicyRequest.Agreement> agreements) {

        // 동의 목록이 비어있는 경우를 차단 (빈 IN 쿼리 방지 겸용)
        if (agreements == null || agreements.isEmpty()) {
            throw new PolicyException(PolicyErrorCode.REQUIRED_TERMS_NOT_AGREED);
        }

        Map<Long, Boolean> agreedByDocument = toAgreementMap(agreements);
        List<PolicyDocument> submittedDocuments = findActiveDocuments(agreedByDocument.keySet());

        // 약관 검증
        validateRequiredAgreements(agreedByDocument);

        // 이력 저장을 하기 위해서 회원 저장을 먼저 진행해놓은 상태
        saveAgreements(member, submittedDocuments, agreedByDocument);
    }

    // 동의 목록: {문서 ID: 동의 여부} key-value 형식 맵
    private Map<Long, Boolean> toAgreementMap(List<PolicyRequest.Agreement> agreements) {
        Map<Long, Boolean> agreedByDocument = new LinkedHashMap<>();
        for (PolicyRequest.Agreement agreement : agreements) {
            Boolean previous = agreedByDocument.putIfAbsent(agreement.policyDocumentId(), agreement.agreed());

            // 같은 문서를 서로 다른 동의값으로 중복 제출하면 예외처리
            if (previous != null && !previous.equals(agreement.agreed())) {
                throw new GeneralException(GeneralErrorCode.VALID_FAIL);
            }
        }
        return agreedByDocument;
    }

    // 제출한 문서가 모두 실재하는 활성 문서인지 확인 (존재하지 않거나 비활성 문서가 섞이는 문제 방지)
    private List<PolicyDocument> findActiveDocuments(Set<Long> documentIds) {
        List<PolicyDocument> documents = policyDocumentRepository.findByIdInAndActiveTrue(documentIds);

        // 요청한 문서 수와 조회된 문서 수가 다르면 미존재/비활성 문서가 섞인 것으로 판단하여 예외처리
        if (documents.size() != documentIds.size()) {
            throw new GeneralException(GeneralErrorCode.VALID_FAIL);
        }
        return documents;
    }

    // 활성 필수 약관이 모두 동의(true) 상태인지 확인 (누락하거나 false로 보내면 차단)
    private void validateRequiredAgreements(Map<Long, Boolean> agreedByDocument) {
        boolean allRequiredAgreed = policyDocumentRepository.findByActiveTrueAndRequiredTrue().stream()
                .allMatch(document -> Boolean.TRUE.equals(agreedByDocument.get(document.getId())));
        if (!allRequiredAgreed) {
            throw new PolicyException(PolicyErrorCode.REQUIRED_TERMS_NOT_AGREED);
        }
    }

    // 제출한 약관 동의 이력을 회원에 저장 (동의/거부 모두 기록)
    private void saveAgreements(Member member, List<PolicyDocument> documents, Map<Long, Boolean> agreedByDocument) {
        List<MemberPolicyAgreement> agreements = documents.stream()
                .map(document -> MemberPolicyAgreement.of(
                        member, document, Boolean.TRUE.equals(agreedByDocument.get(document.getId()))))
                .toList();
        memberPolicyAgreementRepository.saveAll(agreements);
    }
}
