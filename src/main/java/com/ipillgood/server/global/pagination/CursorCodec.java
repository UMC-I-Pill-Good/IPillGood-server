package com.ipillgood.server.global.pagination;

import com.ipillgood.server.global.apiPayload.code.GeneralErrorCode;
import com.ipillgood.server.global.apiPayload.exception.GeneralException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * 커서 페이지네이션의 커서 문자열 직렬화 공용 유틸.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CursorCodec {

    public static final String NULL_TOKEN = "";

    private static final String JOIN_DELIMITER = "|";
    private static final String SPLIT_DELIMITER = "\\|";

    public static String encode(String... tokens) {
        return String.join(JOIN_DELIMITER, tokens);
    }

    public static boolean isEmpty(String cursor) {
        return cursor == null || cursor.isBlank();
    }

    /**
     * 커서를 토큰 배열로 분리한다. 토큰 개수가 기대와 다르면 400으로 처리한다.
     */
    public static String[] decode(String cursor, int tokenCount) {
        String[] tokens = cursor.split(SPLIT_DELIMITER, -1);
        if (tokens.length != tokenCount) {
            throw invalidCursor();
        }
        return tokens;
    }

    public static long parseId(String token) {
        long id = parseLong(token);
        if (id < 1) {
            throw invalidCursor();
        }
        return id;
    }

    public static long parseLong(String token) {
        try {
            return Long.parseLong(token.trim());
        } catch (NumberFormatException e) {
            throw invalidCursor();
        }
    }

    public static int parseInt(String token) {
        try {
            return Integer.parseInt(token.trim());
        } catch (NumberFormatException e) {
            throw invalidCursor();
        }
    }

    public static double parseDouble(String token) {
        try {
            return Double.parseDouble(token.trim());
        } catch (NumberFormatException e) {
            throw invalidCursor();
        }
    }

    public static LocalDateTime parseDateTime(String token) {
        try {
            return LocalDateTime.parse(token.trim());
        } catch (DateTimeParseException e) {
            throw invalidCursor();
        }
    }

    public static GeneralException invalidCursor() {
        return new GeneralException(GeneralErrorCode.INVALID_CURSOR);
    }
}
