package com.ipillgood.server.domain.auth.config;

import com.ipillgood.server.domain.auth.client.SocialProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * 소셜 로그인 설정(application.yml/social/*)바인딩을 활성화하는 설정 클래스
 */
@Configuration
@EnableConfigurationProperties(SocialProperties.class)
public class SocialConfig {

    /**
     * 소셜 제공자 API 호출에 사용
     * 빈으로 분리하여 테스트에서 가짜 응답을 주는 RestClient로 교체 목적
     */
    @Bean
    public RestClient socialRestClient() {
        return RestClient.builder().build();
    }
}
