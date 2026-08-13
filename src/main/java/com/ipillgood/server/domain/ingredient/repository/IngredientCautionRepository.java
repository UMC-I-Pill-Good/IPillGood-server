package com.ipillgood.server.domain.ingredient.repository;

import com.ipillgood.server.domain.ingredient.entity.IngredientCaution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IngredientCautionRepository extends JpaRepository<IngredientCaution, Long> {

    List<IngredientCaution> findByIngredientIdOrderByIdAsc(Long ingredientId);
}
