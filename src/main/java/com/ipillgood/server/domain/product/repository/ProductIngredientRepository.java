package com.ipillgood.server.domain.product.repository;

import com.ipillgood.server.domain.ingredient.entity.Ingredient;
import com.ipillgood.server.domain.product.entity.ProductIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductIngredientRepository extends JpaRepository<ProductIngredient, Long> {

    @Query("""
            select pi.ingredient
            from ProductIngredient pi
            where pi.product.id = :productId
    """)
    List<Ingredient> findIngredientsByProduct(@Param("productId") Long productId);
}
