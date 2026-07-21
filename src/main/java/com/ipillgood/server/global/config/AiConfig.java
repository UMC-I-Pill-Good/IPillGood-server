package com.ipillgood.server.global.config;

import com.ipillgood.server.global.ai.GeminiProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GeminiProperties.class)
public class AiConfig {
}
