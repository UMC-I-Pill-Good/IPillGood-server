package com.ipillgood.server.domain.auth.client;

import com.ipillgood.server.domain.member.entity.enums.SocialProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 소셜 제공자에 맞는 SocialProfile 구현체를 찾아주는 클래스
 */
@Component
public class SocialProfileClientResolver {

    private final Map<SocialProvider, SocialProfileClient> clients;

    public SocialProfileClientResolver(List<SocialProfileClient> clients) {
        this.clients = clients.stream()
                .collect(Collectors.toUnmodifiableMap(SocialProfileClient::provider, Function.identity()));
    }

    /**
     * SocialProvider에 해당하는 구현체를 반환
     */
    public SocialProfileClient resolve(SocialProvider provider) {
        SocialProfileClient client = clients.get(provider);
        if (client == null) {
            throw new IllegalStateException("소셜 프로필 조회 구현체가 없습니다: " + provider);
        }
        return client;
    }
}
