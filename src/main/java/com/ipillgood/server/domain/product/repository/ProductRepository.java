package com.ipillgood.server.domain.product.repository;

import com.ipillgood.server.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("""
            select p
            from Product p
            where p.id in :productIds
              and p.deletedAt is null
            """)
    List<Product> findActiveByIdIn(@Param("productIds") Collection<Long> productIds);

    @Query("""
            select p
            from Product p
            where p.id = :productId
              and p.deletedAt is null
            """)
    Optional<Product> findActiveById(@Param("productId") Long productId);
}
