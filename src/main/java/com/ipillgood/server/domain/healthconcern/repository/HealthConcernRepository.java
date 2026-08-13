package com.ipillgood.server.domain.healthconcern.repository;

import com.ipillgood.server.domain.healthconcern.entity.HealthConcern;
import com.ipillgood.server.domain.healthconcern.entity.enums.MajorCategory;
import com.ipillgood.server.domain.healthconcern.entity.enums.MinorCategory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HealthConcernRepository extends JpaRepository<HealthConcern, Long> {

    Optional<HealthConcern> findByMajorCategoryAndMinorCategory(MajorCategory majorCategory, MinorCategory minorCategory);
}
