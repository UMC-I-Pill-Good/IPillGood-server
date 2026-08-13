package com.ipillgood.server.domain.search.repository;

import com.ipillgood.server.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductSearchRepository
        extends JpaRepository<Product, Long>, ProductSearchQueryDsl {
}
