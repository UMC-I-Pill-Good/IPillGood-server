package com.ipillgood.server.global.util;

import java.util.List;

/**
 * 성분이 2개 이상 포함된 상품의 "기타" 대표 이미지 키를 결정하는 유틸
 */
public final class EtcProductImageKeyResolver {

    private static final List<String> ETC_IMAGE_KEYS = List.of(
            "ingredients/other1.png",
            "ingredients/other2.png",
            "ingredients/other3.png",
            "ingredients/other4.png"
    );

    /**
     * 상품 ID를 기반으로 기타 이미지 키 하나를 결정론적으로 반환한다.
     */
    public static String resolve(Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException("productId는 null일 수 없습니다.");
        }
        int index = Math.floorMod(productId, ETC_IMAGE_KEYS.size());
        return ETC_IMAGE_KEYS.get(index);
    }
}
