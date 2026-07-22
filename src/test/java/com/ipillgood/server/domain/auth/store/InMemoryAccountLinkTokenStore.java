package com.ipillgood.server.domain.auth.store;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 테스트용 인메모리 계정 연동 토큰 저장소
 * - 테스트 파일에서만 로드되며, Redis 없이 연동 흐름을 검증하기 위한 대체 구현
 */
@Component
@Profile("test")
public class InMemoryAccountLinkTokenStore implements AccountLinkTokenStore {

    // 여러 스레드 접근에 안전한 ConcurrentHashMap 사용
    private final Map<String, PendingSocialLink> store = new ConcurrentHashMap<>();

    /**
     * 연동 대기 정보를 저장하고 임시 토큰을 발급
     */
    @Override
    public String issue(PendingSocialLink pendingSocialLink) {
        String accountLinkToken = UUID.randomUUID().toString();
        store.put(accountLinkToken, pendingSocialLink);
        return accountLinkToken;
    }

    /**
     * 연동 대기 정보를 꺼내면서 즉시 폐기 (1회용)
     */
    @Override
    public Optional<PendingSocialLink> consume(String accountLinkToken) {
        return Optional.ofNullable(store.remove(accountLinkToken));
    }

    // 매 테스트 시작 전 저장소 비우기
    public void clear() {
        store.clear();
    }
}
