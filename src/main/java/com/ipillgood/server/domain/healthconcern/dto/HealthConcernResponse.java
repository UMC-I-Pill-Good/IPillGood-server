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

    @Schema(description = "건강 상태 추천 성분 조회 응답")
    @Builder
    public record RecommendedIngredients(
            @Schema(description = "건강 고민 ID", example = "1")
            Long healthConcernId,

            @Schema(description = "대분류", example = "NERVOUS_SYSTEM")
            MajorCategory majorCategory,

            @Schema(description = "소분류", example = "SLEEP_QUALITY")
            MinorCategory minorCategory,

            @Schema(description = "감퇴 원인 설명", example = "수면 리듬 변화가 원인일 수 있습니다.")
            String declineCause,

            @Schema(description = "추천 성분 목록")
            List<RecommendedIngredient> recommendedIngredients
    ) {
    }

    @Schema(description = "추천 성분 항목")
    @Builder
    public record RecommendedIngredient(
            @Schema(description = "영양성분 ID", example = "1")
            Long ingredientId,

            @Schema(description = "성분명", example = "마그네슘")
            String name,

            @Schema(description = "성분 설명")
            String description,

            @Schema(description = "성분 이미지 URL")
            String imageUrl,

            @Schema(description = "효능 키워드 목록")
            List<String> effectKeywords,

            @Schema(description = "사용자의 캐비닛에 현재 성분을 포함한 영양제가 있는지 여부")
            Boolean hasCabinetProduct
    ) {
    }
}
