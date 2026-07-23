package com.ipillgood.server.global.s3;

import java.util.regex.Pattern;
import lombok.Getter;

/**
 * S3 이미지 도메인별 규칙(key prefix).
 */
@Getter
public enum ImageDirectory {

    REVIEW("reviews");

    private final String prefix;
    private final Pattern keyPattern;

    ImageDirectory(String prefix) {
        this.prefix = prefix;
        // {prefix}/{uuid}.{jpg|png|webp}
        this.keyPattern = Pattern.compile(
                "^" + Pattern.quote(prefix)
                        + "/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"
                        + "\\.(jpg|png|webp)$");
    }

    public boolean matchesKey(String key) {
        return key != null && keyPattern.matcher(key).matches();
    }
}
