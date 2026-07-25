package com.ipillgood.server.domain.search.repository;

import java.util.Collection;
import java.util.List;

public interface ProductSearchQueryDsl {

    List<ProductSearchProjection.Product> searchProducts(ProductSearchCondition condition);

    long countProducts(ProductSearchCondition condition);

    List<ProductSearchProjection.Ingredient> findIngredientsByProductIds(Collection<Long> productIds);
}
