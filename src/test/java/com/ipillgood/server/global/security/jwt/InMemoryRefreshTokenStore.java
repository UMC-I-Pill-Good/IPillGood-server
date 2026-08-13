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

    // key: memberId:sessionId, 여러 스레드 접근에 안전한 ConcurrentHashMap 사용
    private final Map<String, String> store = new ConcurrentHashMap<>();

    @Override
    public void save(Long memberId, String sessionId, String refreshToken, Duration ttl) {
        store.put(key(memberId, sessionId), refreshToken);
    }

    @Override
    public Optional<String> find(Long memberId, String sessionId) {
        return Optional.ofNullable(store.get(key(memberId, sessionId)));
    }

    @Override
    public void delete(Long memberId, String sessionId) {
        store.remove(key(memberId, sessionId));
    }

    @Override
    public void deleteAll(Long memberId) {
        store.keySet().removeIf(k -> k.startsWith(memberId + ":"));
    }

    // 매 테스트 시작 전 호출해서 저장소를 비움
    public void clear() {
        store.clear();
    }

    // 테스트 전용: 해당 회원의 세션(기기)이 하나도 저장돼 있지 않은지 확인
    public boolean hasNoSession(Long memberId) {
        return store.keySet().stream().noneMatch(k -> k.startsWith(memberId + ":"));
    }

    private String key(Long memberId, String sessionId) {
        return memberId + ":" + sessionId;
    }
}
