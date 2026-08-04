package com.ipillgood.server.domain.auth.store;

import com.ipillgood.server.domain.member.entity.enums.SocialProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 소셜 회원가입 임시 토큰의 Redis 저장 구현체
 * - 테스트는 인메모리 fake로 대체함
 * - accountLinkToken과 동일한 구조(Hash로 저장, 키 단위 만료)를 그대로 사용
 */
@Component
@Profile("!test")
@RequiredArgsConstructor
public class RedisSocialSignupTokenStore implements SocialSignupTokenStore {

    private static final String KEY_PREFIX = "social:signup:";

    // 혹시라도 노출됐을 경우를 대비해 짧은 시간으로 설정 (약관 화면 진입~제출에 충분한 시간)
    private static final Duration TTL = Duration.ofMinutes(5);

    private static final String FIELD_PROVIDER = "provider";
    private static final String FIELD_PROVIDER_USER_ID = "providerUserId";
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_NICKNAME = "nickname";

    private final StringRedisTemplate redisTemplate;

    /**
     * 소셜 로그인 콜백에서 완전 신규 사용자로 판정했을 때 실행
     * - 토큰 값을 저장소가 직접 랜덤한 값으로 만들어 추측 못하게 함
     */
    @Override
    public String issue(PendingSocialSignup pendingSocialSignup) {
        String socialSignupToken = UUID.randomUUID().toString();
        String key = key(socialSignupToken);

        Map<String, String> values = new HashMap<>();
        values.put(FIELD_PROVIDER, pendingSocialSignup.provider().name());
        values.put(FIELD_PROVIDER_USER_ID, pendingSocialSignup.providerUserId());
        values.put(FIELD_EMAIL, pendingSocialSignup.email());
        values.put(FIELD_NICKNAME, pendingSocialSignup.nickname());

        hashOps().putAll(key, values);
        redisTemplate.expire(key, TTL);

        return socialSignupToken;
    }

    /**
     * 소셜 회원가입 요청이 들어왔을 때 실행
     * 토큰에 해당하는 회원가입 대기 정보를 조회하면서 즉시 폐기 (1회용)
     * - 조회와 삭제가 별개 명령이므로, 삭제에 성공한 요청만 통과시켜 동시 사용을 막음
     */
    @Override
    public Optional<PendingSocialSignup> consume(String socialSignupToken) {
        String key = key(socialSignupToken);

        // 조회 요청
        Map<String, String> values = hashOps().entries(key);

        // 재시도 차단
        if (values.isEmpty()) {
            return Optional.empty();
        }

        // 삭제 요청 -> 거의 동시 요청 / 의도적 공격 차단
        if (!Boolean.TRUE.equals(redisTemplate.delete(key))) {
            return Optional.empty();
        }

        return Optional.of(new PendingSocialSignup(
                SocialProvider.valueOf(values.get(FIELD_PROVIDER)),
                values.get(FIELD_PROVIDER_USER_ID),
                values.get(FIELD_EMAIL),
                values.get(FIELD_NICKNAME)
        ));
    }

    private HashOperations<String, String, String> hashOps() {
        return redisTemplate.opsForHash();
    }

    private String key(String socialSignupToken) {
        return KEY_PREFIX + socialSignupToken;
    }
}
