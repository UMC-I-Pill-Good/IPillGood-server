package com.ipillgood.server.domain.ingredient.repository;

import com.ipillgood.server.domain.ingredient.entity.Ingredient;
import com.ipillgood.server.domain.ingredient.entity.IngredientEffect;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientEffectRepository extends JpaRepository<IngredientEffect, Long> {

    List<IngredientEffect> findAllByIngredientIn(Collection<Ingredient> ingredients);
}
