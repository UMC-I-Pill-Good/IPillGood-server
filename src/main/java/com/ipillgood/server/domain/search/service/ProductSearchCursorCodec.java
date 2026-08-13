package com.ipillgood.server.domain.search.service;

import com.ipillgood.server.domain.search.repository.ProductSearchCondition;
import com.ipillgood.server.domain.search.entity.enums.ProductSearchSort;
import com.ipillgood.server.domain.search.repository.ProductSearchProjection;
import com.ipillgood.server.global.pagination.CursorCodec;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 영양제 상품 검색 커서 코덱.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductSearchCursorCodec {

    private static final int CURSOR_TOKEN_COUNT = 2;

    public static String encode(ProductSearchSort sort, ProductSearchProjection.Product lastRow) {
        String sortValueToken = switch (sort) {
            case REVIEW_COUNT -> String.valueOf(lastRow.reviewCount() == null ? 0L : lastRow.reviewCount());
            case RATING -> lastRow.averageRating() == null
                    ? CursorCodec.NULL_TOKEN
                    : String.valueOf(lastRow.averageRating());
        };
        return CursorCodec.encode(String.valueOf(lastRow.productId()), sortValueToken);
    }

    public static ProductSearchCondition.Cursor decode(String cursor, ProductSearchSort sort) {
        if (CursorCodec.isEmpty(cursor)) {
            return null;
        }

        String[] tokens = CursorCodec.decode(cursor, CURSOR_TOKEN_COUNT);
        long productId = CursorCodec.parseId(tokens[0]);

        return switch (sort) {
            case REVIEW_COUNT -> {
                long reviewCount = CursorCodec.parseLong(tokens[1]);
                if (reviewCount < 0) {
                    throw CursorCodec.invalidCursor();
                }
                yield new ProductSearchCondition.Cursor(productId, reviewCount, null, false);
            }
            case RATING -> tokens[1].isEmpty()
                    ? new ProductSearchCondition.Cursor(productId, null, null, true)
                    : new ProductSearchCondition.Cursor(
                    productId, null, CursorCodec.parseDouble(tokens[1]), false);
        };
    }
}
