package com.ipillgood.server.domain.auth.client;

import com.ipillgood.server.domain.auth.client.dto.SocialProfile;
import com.ipillgood.server.domain.member.entity.enums.SocialProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 제공자별로 구현체가 잘 선택되는지 검증
 * 실제 카카오/네이버 클라이언트 대신 가짜 구현체를 넣어 선택 로직만 확인
 */
class SocialProfileClientResolverTest {

    private static SocialProfileClient fakeClient(SocialProvider provider) {
        return new SocialProfileClient() {

            @Override
            public SocialProvider provider() {
                return provider;
            }

            @Override
            public SocialProfile fetch(String providerAccessToken) {
                return new SocialProfile("id", "email@example.com");
            }
        };
    }

    @Test
    @DisplayName("제공자에 해당하는 구현체를 반환한다")
    void resolve_returnsMatchingClient() {
        SocialProfileClient kakao = fakeClient(SocialProvider.KAKAO);
        SocialProfileClient naver = fakeClient(SocialProvider.NAVER);
        SocialProfileClientResolver resolver = new SocialProfileClientResolver(List.of(kakao, naver));

        assertSame(kakao, resolver.resolve(SocialProvider.KAKAO));
        assertSame(naver, resolver.resolve(SocialProvider.NAVER));
    }

    @Test
    @DisplayName("구현체가 없는 제공자를 요청하면 서버 오류로 드러낸다")
    void resolve_throwsWhenClientMissing() {

        // 사용자 입력 문제가 아니라 구현체 미제작 문제이므로 인증 예외가 아닌 IllegalStateException
        SocialProfileClientResolver resolver =
                new SocialProfileClientResolver(List.of(fakeClient(SocialProvider.KAKAO)));

        assertThrows(IllegalStateException.class, () -> resolver.resolve(SocialProvider.NAVER));
    }
}
