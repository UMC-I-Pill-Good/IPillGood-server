package com.ipillgood.server.domain.review.service;

import com.ipillgood.server.domain.review.entity.enums.ProductReviewSort;
import com.ipillgood.server.domain.review.repository.ProductReviewCondition;
import com.ipillgood.server.domain.review.repository.ProductReviewProjection;
import com.ipillgood.server.global.pagination.CursorCodec;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 상품 후기 목록 커서 코덱.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductReviewCursorCodec {

    private static final int CURSOR_TOKEN_COUNT = 2;

    public static String encode(ProductReviewSort sort, ProductReviewProjection.Review lastRow) {
        String sortValueToken = switch (sort) {
            case LATEST -> lastRow.createdAt().toString();
            case LIKE_COUNT_DESC -> String.valueOf(lastRow.helpfulCount());
        };
        return CursorCodec.encode(String.valueOf(lastRow.reviewId()), sortValueToken);
    }

    public static ProductReviewCondition.Cursor decode(String cursor, ProductReviewSort sort) {
        if (CursorCodec.isEmpty(cursor)) {
            return null;
        }

        String[] tokens = CursorCodec.decode(cursor, CURSOR_TOKEN_COUNT);
        long reviewId = CursorCodec.parseId(tokens[0]);

        return switch (sort) {
            case LATEST -> new ProductReviewCondition.Cursor(
                    reviewId, CursorCodec.parseDateTime(tokens[1]), null);
            case LIKE_COUNT_DESC -> {
                int helpfulCount = CursorCodec.parseInt(tokens[1]);
                if (helpfulCount < 0) {
                    throw CursorCodec.invalidCursor();
                }
                yield new ProductReviewCondition.Cursor(reviewId, null, helpfulCount);
            }
        };
    }
}
