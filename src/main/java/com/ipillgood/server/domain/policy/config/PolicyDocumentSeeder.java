package com.ipillgood.server.domain.policy.config;

import com.ipillgood.server.domain.policy.entity.PolicyDocument;
import com.ipillgood.server.domain.policy.entity.enums.PolicyDocumentType;
import com.ipillgood.server.domain.policy.repository.PolicyDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

// 앱 기동 시 약관 문서가 비어있으면 기본 4종을 등록 (본문 확정됨)
@Component
@RequiredArgsConstructor
public class PolicyDocumentSeeder implements ApplicationRunner {

    private static final String PLACEHOLDER_CONTENT = "본문은 추후 업데이트될 예정입니다.";
    private static final String INITIAL_VERSION = "v1.0";

    private final PolicyDocumentRepository policyDocumentRepository;

    @Override
    public void run(ApplicationArguments args) {
        // 이미 데이터가 있으면 바로 return (PM이 채운 본문을 덮어쓰지 않기 위함)
        if (policyDocumentRepository.count() > 0) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        policyDocumentRepository.saveAll(List.of(
                document(PolicyDocumentType.SERVICE_TERMS, "서비스 이용약관", true, now),
                document(PolicyDocumentType.PRIVACY_COLLECTION, "개인정보 수집 및 이용", true, now),
                document(PolicyDocumentType.HEALTH_INFO_COLLECTION, "건강 정보 수집 및 이용", true, now),
                document(PolicyDocumentType.MARKETING, "마케팅 정보 수신", false, now)
        ));
    }

    private PolicyDocument document(PolicyDocumentType type, String title, boolean required, LocalDateTime effectiveAt) {
        return PolicyDocument.builder()
                .documentType(type)
                .title(title)
                .content(PLACEHOLDER_CONTENT)
                .required(required)
                .version(INITIAL_VERSION)
                .effectiveAt(effectiveAt)
                .active(true)
                .build();
    }
}
