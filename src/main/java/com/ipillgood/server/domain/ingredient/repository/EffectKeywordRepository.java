package com.ipillgood.server.domain.ingredient.repository;

import com.ipillgood.server.domain.ingredient.entity.EffectKeyword;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EffectKeywordRepository extends JpaRepository<EffectKeyword, Long> {

    List<EffectKeyword> findByIngredient_IdInOrderByIdAsc(Collection<Long> ingredientIds);
}
