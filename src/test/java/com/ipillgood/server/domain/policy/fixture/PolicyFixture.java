package com.ipillgood.server.domain.policy.fixture;

import com.ipillgood.server.domain.policy.entity.PolicyDocument;
import com.ipillgood.server.domain.policy.entity.enums.PolicyDocumentType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 약관 문서 테스트 헬퍼 함수
 * AuthControllerSocialTest', AuthServiceSignUpTest', 'SocialAuthServiceTest' 사용
 */
public class PolicyFixture {

    private static final String CONTENT = "테스트용 약관 본문";
    private static final String VERSION = "v1.0";
    private static final LocalDateTime EFFECTIVE_AT = LocalDateTime.of(2026, 1, 1, 0, 0);

    // 필수 동의 약관
    public static PolicyDocument requiredDocument(PolicyDocumentType documentType) {
        return document(documentType, true);
    }

    // 선택 동의 약관
    public static PolicyDocument optionalDocument(PolicyDocumentType documentType) {
        return document(documentType, false);
    }

    public static List<PolicyDocument> defaultDocuments() {
        return List.of(
                requiredDocument(PolicyDocumentType.SERVICE_TERMS),
                requiredDocument(PolicyDocumentType.PRIVACY_COLLECTION),
                requiredDocument(PolicyDocumentType.HEALTH_INFO_COLLECTION),
                optionalDocument(PolicyDocumentType.MARKETING)
        );
    }

    private static PolicyDocument document(PolicyDocumentType documentType, boolean required) {
        return PolicyDocument.builder()
                .documentType(documentType)
                .title(documentType.getLabel())
                .content(CONTENT)
                .required(required)
                .version(VERSION)
                .effectiveAt(EFFECTIVE_AT)
                .active(true)
                .build();
    }
}
