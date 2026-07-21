package com.ipillgood.server.domain.auth.config;

import com.ipillgood.server.domain.auth.client.SocialProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 소셜 로그인 설정(application.yml/social/*)바인딩을 활성화하는 설정 클래스
 * SocialProperties를 빈으로 등록해 yml 값이 채워지도록 함
 */
@Configuration
@EnableConfigurationProperties(SocialProperties.class)
public class SocialConfig {
}
