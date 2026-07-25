package com.ipillgood.server.domain.search.service;

import com.ipillgood.server.domain.search.code.SearchErrorCode;
import com.ipillgood.server.domain.search.repository.ProductSearchCondition;
import com.ipillgood.server.domain.search.entity.enums.ProductSearchSort;
import com.ipillgood.server.domain.search.exception.SearchException;
import com.ipillgood.server.domain.search.repository.ProductSearchProjection;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductSearchCursorCodec {

    private static final String DELIMITER = "\\|";
    private static final String JOIN_DELIMITER = "|";
    private static final String NULL_RATING_TOKEN = "";
    private static final int CURSOR_PART_COUNT = 2;

    public static String encode(ProductSearchSort sort, ProductSearchProjection.Product lastRow) {
        String sortValueToken = switch (sort) {
            case REVIEW_COUNT -> String.valueOf(lastRow.reviewCount() == null ? 0L : lastRow.reviewCount());
            case RATING -> lastRow.averageRating() == null
                    ? NULL_RATING_TOKEN
                    : String.valueOf(lastRow.averageRating());
        };
        return lastRow.productId() + JOIN_DELIMITER + sortValueToken;
    }

    public static ProductSearchCondition.Cursor decode(String cursor, ProductSearchSort sort) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }

        String[] parts = cursor.split(DELIMITER, -1);
        if (parts.length != CURSOR_PART_COUNT) {
            throw new SearchException(SearchErrorCode.SEARCH_CURSOR_INVALID);
        }

        try {
            long productId = Long.parseLong(parts[0].trim());
            if (productId < 1) {
                throw new SearchException(SearchErrorCode.SEARCH_CURSOR_INVALID);
            }

            return switch (sort) {
                case REVIEW_COUNT -> {
                    long reviewCount = Long.parseLong(parts[1].trim());
                    if (reviewCount < 0) {
                        throw new SearchException(SearchErrorCode.SEARCH_CURSOR_INVALID);
                    }
                    yield new ProductSearchCondition.Cursor(productId, reviewCount, null, false);
                }
                case RATING -> parts[1].isEmpty()
                        ? new ProductSearchCondition.Cursor(productId, null, null, true)
                        : new ProductSearchCondition.Cursor(productId, null, Double.parseDouble(parts[1].trim()), false);
            };
        } catch (NumberFormatException e) {
            throw new SearchException(SearchErrorCode.SEARCH_CURSOR_INVALID);
        }
    }
}
