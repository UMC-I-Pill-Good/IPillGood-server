package com.ipillgood.server.domain.ingredient.repository;

import com.ipillgood.server.domain.ingredient.entity.Contraindication;
import com.ipillgood.server.domain.ingredient.entity.enums.ContraindicationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ContraindicationRepository extends JpaRepository<Contraindication, Long> {

    List<Contraindication> findByTypeInOrderByIdAsc(Collection<ContraindicationType> types);
}
