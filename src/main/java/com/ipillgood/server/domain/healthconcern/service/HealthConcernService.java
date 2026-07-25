package com.ipillgood.server.domain.healthconcern.service;

import com.ipillgood.server.domain.healthconcern.converter.HealthConcernConverter;
import com.ipillgood.server.domain.healthconcern.dto.HealthConcernResponse;
import org.springframework.stereotype.Service;

@Service
public class HealthConcernService {

    public HealthConcernResponse.CategoryList getCategories() {
        return HealthConcernConverter.toCategoryList();
    }
}
