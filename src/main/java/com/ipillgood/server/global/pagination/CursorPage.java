package com.ipillgood.server.global.pagination;

import java.util.List;
import java.util.function.Function;

/**
 * 커서 페이지네이션 조회 결과.
 *
 * @param content    실제로 응답에 내려갈 행 (최대 size건)
 * @param hasNext    다음 페이지 존재 여부
 * @param nextCursor 다음 커서. hasNext가 false면 null
 */
public record CursorPage<T>(
        List<T> content,
        boolean hasNext,
        String nextCursor
) {

    /**
     * @param rows          size + 1건을 limit으로 조회한 결과
     * @param size          클라이언트에 실제로 내려줄 페이지 크기
     * @param cursorEncoder 페이지 마지막 행을 커서 문자열로 변환하는 도메인별 인코더
     */
    public static <T> CursorPage<T> of(List<T> rows, int size, Function<T, String> cursorEncoder) {
        boolean hasNext = rows.size() > size;
        List<T> content = hasNext ? List.copyOf(rows.subList(0, size)) : rows;
        String nextCursor = hasNext ? cursorEncoder.apply(content.get(content.size() - 1)) : null;

        return new CursorPage<>(content, hasNext, nextCursor);
    }
}
