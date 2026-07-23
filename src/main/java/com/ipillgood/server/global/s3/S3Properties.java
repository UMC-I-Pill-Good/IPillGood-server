package com.ipillgood.server.global.s3;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.storage")
public record S3Properties(
        String bucket,
        String region,
        String accessKey,
        String secretKey,
        String publicBaseUrl,
        Duration presignExpiration
) {
}
