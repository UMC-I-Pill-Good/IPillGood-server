package com.ipillgood.server.global.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// image_key 마스터 값을 실제 접근 가능한 이미지 URL로 변환
@Component
public class ImageUrlResolver {

    private final String baseUrl;

    public ImageUrlResolver(@Value("${app.image.base-url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String resolve(String imageKey) {
        if (imageKey == null || imageKey.isBlank()) {
            return null;
        }
        return baseUrl + "/" + imageKey + ".png";
    }
}
