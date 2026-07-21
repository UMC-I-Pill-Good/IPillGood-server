package com.ipillgood.server.domain.ingredient.repository;

import com.ipillgood.server.domain.ingredient.entity.Ingredient;
import com.ipillgood.server.domain.ingredient.entity.IngredientEffect;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface IngredientEffectRepository extends JpaRepository<IngredientEffect, Long> {

    List<IngredientEffect> findByIngredientIdOrderByIdAsc(Long ingredientId);

    List<IngredientEffect> findAllByIngredientIn(Collection<Ingredient> ingredients);
}