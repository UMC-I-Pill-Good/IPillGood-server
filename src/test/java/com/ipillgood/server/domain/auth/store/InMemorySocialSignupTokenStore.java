package com.ipillgood.server.domain.auth.store;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 테스트용 인메모리 소셜 회원가입 토큰 저장소
 * - 테스트 파일에서만 로드되며, Redis 없이 회원가입 흐름을 검증하기 위한 대체 구현
 */
@Component
@Profile("test")
public class InMemorySocialSignupTokenStore implements SocialSignupTokenStore {

    // 여러 스레드 접근에 안전한 ConcurrentHashMap 사용
    private final Map<String, PendingSocialSignup> store = new ConcurrentHashMap<>();

    /**
     * 회원가입 대기 정보를 저장하고 임시 토큰을 발급
     */
    @Override
    public String issue(PendingSocialSignup pendingSocialSignup) {
        String socialSignupToken = UUID.randomUUID().toString();
        store.put(socialSignupToken, pendingSocialSignup);
        return socialSignupToken;
    }

    /**
     * 회원가입 대기 정보를 꺼내면서 즉시 폐기 (1회용)
     */
    @Override
    public Optional<PendingSocialSignup> consume(String socialSignupToken) {
        return Optional.ofNullable(store.remove(socialSignupToken));
    }

    // 매 테스트 시작 전 저장소 비우기
    public void clear() {
        store.clear();
    }
}
