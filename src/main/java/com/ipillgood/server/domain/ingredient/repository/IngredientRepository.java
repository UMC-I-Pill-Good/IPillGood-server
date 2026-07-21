package com.ipillgood.server.domain.ingredient.repository;

import com.ipillgood.server.domain.ingredient.entity.Ingredient;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    @Query("""
            select i from Ingredient i
            where i.id not in (
                select ci.ingredient.id from ContraindicationIngredient ci
                where ci.contraindication.id in :excludedContraindicationIds
            )
            """)
    List<Ingredient> findSafeCandidates(@Param("excludedContraindicationIds") List<Long> excludedContraindicationIds);
}
