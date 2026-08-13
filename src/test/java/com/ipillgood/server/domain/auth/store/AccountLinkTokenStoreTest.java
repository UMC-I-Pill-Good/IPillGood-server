package com.ipillgood.server.domain.auth.store;

import com.ipillgood.server.domain.member.entity.enums.SocialProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 계정 연동 임시 토큰의 발급/소비 동작을 검증
 * Redis 구현체와 동일한 규칙(1회용 소비)을 따르는 인메모리(fake) 저장소로 확인
 */
class AccountLinkTokenStoreTest {

    private final AccountLinkTokenStore store = new InMemoryAccountLinkTokenStore();

    private static PendingSocialLink pending() {
        return new PendingSocialLink(1L, SocialProvider.KAKAO, "12345678", "kim@example.com");
    }

    @Test
    @DisplayName("발급한 토큰으로 연동 대기 정보를 그대로 꺼낼 수 있다")
    void issueAndConsume() {
        PendingSocialLink expected = pending();

        String accountLinkToken = store.issue(expected);
        PendingSocialLink actual = store.consume(accountLinkToken).orElseThrow();

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("한 번 사용한 토큰은 재사용할 수 없다")
    void consume_isOneTimeUse() {
        String accountLinkToken = store.issue(pending());

        assertTrue(store.consume(accountLinkToken).isPresent());
        assertTrue(store.consume(accountLinkToken).isEmpty());
    }

    @Test
    @DisplayName("존재하지 않는 토큰은 빈 값을 반환한다")
    void consume_returnsEmptyForUnknownToken() {
        assertTrue(store.consume("unknown-token").isEmpty());
    }

    @Test
    @DisplayName("발급할 때마다 서로 다른 토큰이 나온다")
    void issue_generatesUniqueToken() {

        // 토큰 값을 저장소가 만들어야 토큰값 추측이 불가능
        assertNotEquals(store.issue(pending()), store.issue(pending()));
    }
}
