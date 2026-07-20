package com.ipillgood.server.domain.ingredient.repository;

import com.ipillgood.server.domain.ingredient.entity.AlternativeFood;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlternativeFoodRepository extends JpaRepository<AlternativeFood, Long> {

    List<AlternativeFood> findByIngredientIdOrderByIdAsc(Long ingredientId);
}
