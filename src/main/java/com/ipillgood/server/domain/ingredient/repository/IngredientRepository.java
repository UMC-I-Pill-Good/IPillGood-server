package com.ipillgood.server.domain.ingredient.repository;

import com.ipillgood.server.domain.ingredient.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    List<Ingredient> findAllByOrderByIdAsc();

    @Query("""
            select count(mp)
            from MemberProduct mp
            join ProductIngredient pi on pi.product = mp.product
            where mp.member.id = :memberId
              and pi.ingredient.id = :ingredientId
              and mp.deletedAt is null
              and mp.product.deletedAt is null
            """)
    long countActiveCabinetProductsContainingIngredient(
            @Param("memberId") Long memberId,
            @Param("ingredientId") Long ingredientId
    );
}
