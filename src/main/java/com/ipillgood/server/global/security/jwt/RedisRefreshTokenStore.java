package com.ipillgood.server.global.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

/**
 * 리프레시 토큰의 실제 Redis 저장 구현체
 * Service 계층에서 지정한 TTL로 Redis가 토큰 만료를 관리
 * 회원의 세션(기기)마다 별도 키로 저장해 기기별 로그인 상태를 독립적으로 유지
 * 테스트에서는 로드되지 않고 인메모리 fake로 대체
 */
@Component
@Profile("!test")
@RequiredArgsConstructor
public class RedisRefreshTokenStore implements RefreshTokenStore {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void save(Long memberId, String sessionId, String refreshToken, Duration ttl) {
        redisTemplate.opsForValue().set(key(memberId, sessionId), refreshToken, ttl);
    }

    @Override
    public Optional<String> find(Long memberId, String sessionId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key(memberId, sessionId)));
    }

    @Override
    public void delete(Long memberId, String sessionId) {
        redisTemplate.delete(key(memberId, sessionId));
    }

    @Override
    public void deleteAll(Long memberId) {
        Set<String> keys = redisTemplate.keys(keyPrefix(memberId) + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    private String key(Long memberId, String sessionId) {
        return keyPrefix(memberId) + sessionId;
    }

    private String keyPrefix(Long memberId) {
        return "refresh:" + memberId + ":";
    }
}
