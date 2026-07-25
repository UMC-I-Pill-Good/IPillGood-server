package com.ipillgood.server.domain.healthconcern.dto;

import com.ipillgood.server.domain.healthconcern.entity.enums.MajorCategory;
import com.ipillgood.server.domain.healthconcern.entity.enums.MinorCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

public class HealthConcernResponse {

    @Schema(description = "건강 상태 카테고리 목록 조회 응답")
    @Builder
    public record CategoryList(
            @Schema(description = "대분류 목록")
            List<MajorCategoryGroup> majorCategories
    ) {
    }

    @Schema(description = "대분류 카테고리 그룹")
    @Builder
    public record MajorCategoryGroup(
            @Schema(description = "대분류 타입", example = "NERVOUS_SYSTEM")
            MajorCategory type,

            @Schema(description = "대분류 명", example = "신경계")
            String label,

            @Schema(description = "해당 대분류에 속한 소분류 목록")
            List<MinorCategoryItem> minorCategories
    ) {
    }

    @Schema(description = "소분류 카테고리 항목")
    @Builder
    public record MinorCategoryItem(
            @Schema(description = "소분류 타입", example = "SLEEP_QUALITY")
            MinorCategory type,

            @Schema(description = "소분류 명", example = "수면의 질")
            String label
    ) {
    }
}
