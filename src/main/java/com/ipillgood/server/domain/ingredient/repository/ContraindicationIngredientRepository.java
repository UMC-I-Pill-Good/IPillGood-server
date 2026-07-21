package com.ipillgood.server.domain.ingredient.repository;

import com.ipillgood.server.domain.ingredient.entity.ContraindicationIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContraindicationIngredientRepository extends JpaRepository<ContraindicationIngredient, Long> {
}
