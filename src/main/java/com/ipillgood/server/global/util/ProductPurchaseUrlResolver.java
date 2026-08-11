package com.ipillgood.server.global.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * 상품명을 검색어로 하는 쇼핑몰 검색 URL을 생성하는 유틸
 */
public final class ProductPurchaseUrlResolver {

    private static final String SEARCH_URL_FORMAT = "https://www.coupang.com/np/search?q=%s";

    private static final String BRACKET_PATTERN = "[\\[\\](){}<>]";
    private static final String WHITESPACE_PATTERN = "\\s+";
    private static final String SPACE = " ";

    private static final int MAX_QUERY_LENGTH = 100;

    /**
     * 브랜드명과 상품명으로 검색 URL을 생성한다.
     */
    public static String resolve(String brand, String name) {
        String query = buildQuery(brand, name);
        return SEARCH_URL_FORMAT.formatted(URLEncoder.encode(query, StandardCharsets.UTF_8));
    }

    private static String buildQuery(String brand, String name) {
        String normalizedName = normalize(name);
        if (normalizedName.isEmpty()) {
            throw new IllegalArgumentException("상품명은 비어 있을 수 없습니다.");
        }

        String normalizedBrand = normalize(brand);
        if (normalizedBrand.isEmpty() || containsBrand(normalizedName, normalizedBrand)) {
            return truncate(normalizedName);
        }
        return truncate(normalizedBrand + SPACE + normalizedName);
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll(BRACKET_PATTERN, SPACE)
                .replaceAll(WHITESPACE_PATTERN, SPACE)
                .trim();
    }

    private static boolean containsBrand(String name, String brand) {
        return removeSpaces(name).contains(removeSpaces(brand));
    }

    private static String removeSpaces(String value) {
        return value.replaceAll(WHITESPACE_PATTERN, "").toLowerCase(Locale.ROOT);
    }

    private static String truncate(String query) {
        return query.length() <= MAX_QUERY_LENGTH
                ? query
                : query.substring(0, MAX_QUERY_LENGTH).trim();
    }
}
