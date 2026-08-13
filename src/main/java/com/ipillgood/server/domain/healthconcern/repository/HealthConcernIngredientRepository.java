package com.ipillgood.server.domain.healthconcern.repository;

import com.ipillgood.server.domain.healthconcern.entity.HealthConcernIngredient;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HealthConcernIngredientRepository extends JpaRepository<HealthConcernIngredient, Long> {

    @Query("""
            select hci from HealthConcernIngredient hci
            join fetch hci.ingredient
            where hci.healthConcern.id = :healthConcernId
            order by hci.id asc
            """)
    List<HealthConcernIngredient> findByHealthConcernIdOrderByIdAsc(@Param("healthConcernId") Long healthConcernId);
}
