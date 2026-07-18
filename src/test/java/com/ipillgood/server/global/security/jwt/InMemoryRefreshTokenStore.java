package com.ipillgood.server.global.security.jwt;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 테스트용 인메모리 리프레시 토큰 저장소
 * 테스트에서만 로드되며, Redis 없이 재발급/로그아웃 로직을 검증
 * TTL은 검증 대상이 아니므로 무시
 */
@Component
@Profile("test")
public class InMemoryRefreshTokenStore implements RefreshTokenStore {

    // 여러 스레드 접근에 안전한 ConcurrentHashMap 사용
    private final Map<Long, String> store = new ConcurrentHashMap<>();

    @Override
    public void save(Long memberId, String refreshToken, Duration ttl) {
        store.put(memberId, refreshToken);
    }

    @Override
    public Optional<String> find(Long memberId) {
        return Optional.ofNullable(store.get(memberId));
    }

    @Override
    public void delete(Long memberId) {
        store.remove(memberId);
    }

    // 매 테스트 시작 전 호출해서 저장소를 비움
    public void clear() {
        store.clear();
    }
}
