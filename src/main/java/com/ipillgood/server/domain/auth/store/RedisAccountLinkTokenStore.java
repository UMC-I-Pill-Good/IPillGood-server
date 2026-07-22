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
 * 계정 연동 임시 토큰의 Redis 저장 구현체
 * - 테스트는 인메모리 fake로 대체함
 * - 보관할 값이 여러 개라 Hash로 저장하고, 키 단위로 만료 시간을 검
 * - 서버가 여러 대여도 같은 Redis를 보므로 어느 서버로 요청이 가든 토큰을 찾을 수 있음
 */
@Component
@Profile("!test")
@RequiredArgsConstructor
public class RedisAccountLinkTokenStore implements AccountLinkTokenStore {

    private static final String KEY_PREFIX = "social:link:";

    // 혹시라도 노출됐을 경우를 대비해 짧은 시간으로 설정 (연동하기엔 충분한 시간)
    private static final Duration TTL = Duration.ofMinutes(5);

    private static final String FIELD_MEMBER_ID = "memberId";
    private static final String FIELD_PROVIDER = "provider";
    private static final String FIELD_PROVIDER_USER_ID = "providerUserId";
    private static final String FIELD_PROVIDER_EMAIL = "providerEmail";

    private final StringRedisTemplate redisTemplate;

    /**
     * 소셜 로그인에서 연동이 필요하다고 판단했을 때 실행
     * - 토큰 값을 저장소가 직접 만들어 추측 가능한 값이 쓰이는 것을 차단
     */
    @Override
    public String issue(PendingSocialLink pendingSocialLink) {

        // 임시 토큰 발급
        String accountLinkToken = UUID.randomUUID().toString();
        String key = key(accountLinkToken);

        Map<String, String> values = new HashMap<>();
        values.put(FIELD_MEMBER_ID, String.valueOf(pendingSocialLink.memberId()));
        values.put(FIELD_PROVIDER, pendingSocialLink.provider().name());
        values.put(FIELD_PROVIDER_USER_ID, pendingSocialLink.providerUserId());

        // 제공자가 이메일 제공을 하지 않은 경우, 이메일은 값이 있을 때만 저장함 (Redis Hash는 null을 못 담음)
        if (pendingSocialLink.providerEmail() != null) {
            values.put(FIELD_PROVIDER_EMAIL, pendingSocialLink.providerEmail());
        }

        hashOps().putAll(key, values);
        redisTemplate.expire(key, TTL);

        return accountLinkToken;
    }

    /**
     * 계정 연동 요청이 들어왔을 때 실행
     * 토큰에 해당하는 연동 대기 정보를 조회하면서 즉시 폐기 (1회용)
     * - 조회와 삭제가 별개 명령이므로, 삭제에 성공한 요청만 통과시켜 동시 사용을 막음
     * - 늦게 도착한 요청은 "이미 없었다" 결과를 받아 empty로 처리됨
     */
    @Override
    public Optional<PendingSocialLink> consume(String accountLinkToken) {
        String key = key(accountLinkToken);
        Map<String, String> values = hashOps().entries(key);

        // 재시도 차단
        if (values.isEmpty()) {
            return Optional.empty();
        }

        // 거의 동시 요청 / 의도적 공격 차단
        if (!Boolean.TRUE.equals(redisTemplate.delete(key))) {
            return Optional.empty();
        }

        return Optional.of(new PendingSocialLink(
                Long.valueOf(values.get(FIELD_MEMBER_ID)),
                SocialProvider.valueOf(values.get(FIELD_PROVIDER)),
                values.get(FIELD_PROVIDER_USER_ID),
                values.get(FIELD_PROVIDER_EMAIL)
        ));
    }

    private HashOperations<String, String, String> hashOps() {
        return redisTemplate.opsForHash();
    }

    private String key(String accountLinkToken) {
        return KEY_PREFIX + accountLinkToken;
    }
}
