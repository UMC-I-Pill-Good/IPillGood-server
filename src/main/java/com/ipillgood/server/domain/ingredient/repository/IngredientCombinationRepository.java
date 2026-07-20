package com.ipillgood.server.domain.ingredient.repository;

import com.ipillgood.server.domain.ingredient.entity.IngredientCombination;
import com.ipillgood.server.domain.ingredient.entity.enums.CombinationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IngredientCombinationRepository extends JpaRepository<IngredientCombination, Long> {

    @Query("""
            select combination
            from IngredientCombination combination
            join fetch combination.ingredientA
            join fetch combination.ingredientB
            where combination.type = :type
              and (
                    combination.ingredientA.id = :ingredientId
                    or combination.ingredientB.id = :ingredientId
              )
            order by combination.id asc
            """)
    List<IngredientCombination> findWithIngredientsByIngredientIdAndTypeOrderByIdAsc(
            @Param("ingredientId") Long ingredientId,
            @Param("type") CombinationType type
    );
}
