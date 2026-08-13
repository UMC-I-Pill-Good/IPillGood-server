package com.ipillgood.server.domain.cabinet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class CabinetResponse {

    @Schema(description = "캐비닛 추가 후보 검색 응답")
    @Builder
    public record ProductCandidates(
            @Schema(description = "적용된 검색어", nullable = true, example = "비타민D")
            String keyword,

            @Schema(description = "적용된 정렬 기준", example = "REVIEW_COUNT_DESC")
            String sort,

            @Schema(description = "현재 페이지 번호", example = "0")
            Integer page,

            @Schema(description = "페이지 크기", example = "20")
            Integer size,

            @Schema(description = "검색 조건에 맞는 전체 상품 수", example = "42")
            Long totalCount,

            @Schema(description = "다음 페이지 존재 여부", example = "true")
            Boolean hasNext,

            @Schema(description = "캐비닛 추가 후보 상품 목록")
            List<ProductCandidate> products
    ) {
    }

    @Schema(description = "캐비닛 추가 후보 상품 항목")
    @Builder
    public record ProductCandidate(
            @Schema(description = "영양제 상품 ID", example = "112")
            Long productId,

            @Schema(description = "브랜드명", example = "뉴트리코어")
            String brand,

            @Schema(description = "영양제 상품명", example = "뉴트리코어 유기농 비타민D 1000IU")
            String productName,

            @Schema(description = "카드에 표시할 썸네일 이미지 URL")
            String thumbnailImageUrl,

            @Schema(description = "활성 후기 기준 평균 별점", nullable = true, example = "4.7")
            Double averageRating,

            @Schema(description = "활성 후기 수", example = "128")
            Integer reviewCount,

            @Schema(description = "포함 성분의 효능 태그 목록")
            List<String> ingredientTags,

            @Schema(description = "이미 캐비닛에 보유 중인지 여부", example = "true")
            Boolean isOwned,

            @Schema(description = "캐비닛 추가 대상으로 선택 가능한지 여부", example = "false")
            Boolean isSelectable
    ) {
    }

    @Schema(description = "캐비닛 영양제 추가 응답")
    @Builder
    public record AddProducts(
            @Schema(description = "추가된 영양제 수", example = "2")
            Integer addedCount,

            @Schema(description = "추가된 캐비닛 보유 영양제 목록")
            List<AddedProduct> addedProducts
    ) {
    }

    @Schema(description = "추가된 캐비닛 보유 영양제 항목")
    @Builder
    public record AddedProduct(
            @Schema(description = "생성된 회원 캐비닛 상품 ID", example = "21")
            Long memberProductId,

            @Schema(description = "영양제 상품 ID", example = "118")
            Long productId,

            @Schema(description = "브랜드명", example = "솔가")
            String brand,

            @Schema(description = "영양제 상품명", example = "솔가 비타민D3 1000IU")
            String productName,

            @Schema(description = "카드에 표시할 썸네일 이미지 URL")
            String thumbnailImageUrl,

            @Schema(description = "캐비닛에 추가한 일시", example = "2026-07-21T11:30:00")
            LocalDateTime addedAt
    ) {
    }

    @Schema(description = "캐비닛 영양제 복수 삭제 응답")
    @Builder
    public record DeleteProducts(
            @Schema(description = "삭제 처리된 캐비닛 보유 영양제 수", example = "2")
            Integer deletedCount,

            @Schema(description = "삭제 처리된 영양제 목록")
            List<DeletedProduct> deletedProducts
    ) {
    }

    @Schema(description = "삭제 처리된 영양제 항목")
    @Builder
    public record DeletedProduct(
            @Schema(description = "삭제된 회원 캐비닛 상품 ID", example = "15")
            Long memberProductId,

            @Schema(description = "영양제 상품 ID", example = "112")
            Long productId,

            @Schema(description = "영양제 상품명", example = "뉴트리코어 유기농 비타민D 1000IU")
            String productName,

            @Schema(description = "삭제 전 섭취 중 영양제였는지 여부", example = "true")
            Boolean wasActiveIntake,

            @Schema(description = "중단 처리된 활성 섭취 중 상품 ID", nullable = true, example = "7")
            Long stoppedActiveProductId
    ) {
    }

    @Schema(description = "캐비닛 보유 영양제 목록 조회 응답")
    @Builder
    public record ProductList(
            @Schema(description = "상단 안내 문구에 사용할 회원 닉네임", example = "필굿")
            String memberNickname,

            @Schema(description = "캐비닛 보유 영양제 수", example = "2")
            Integer totalCount,

            @Schema(description = "캐비닛 보유 영양제 목록")
            List<ProductSummary> products
    ) {
    }

    @Schema(description = "캐비닛 보유 영양제 요약 항목")
    @Builder
    public record ProductSummary(
            @Schema(description = "회원 캐비닛 상품 ID", example = "15")
            Long memberProductId,

            @Schema(description = "영양제 상품 ID", example = "112")
            Long productId,

            @Schema(description = "영양제 상품명", example = "뉴트리코어 유기농 비타민D 1000IU")
            String productName,

            @Schema(description = "카드에 표시할 썸네일 이미지 URL")
            String thumbnailImageUrl,

            @Schema(description = "식약처 인증 여부", example = "true")
            Boolean mfdsCertified,

            @Schema(description = "현재 섭취 중인 영양제로 등록되어 있는지 여부", example = "true")
            Boolean isActiveIntake,

            @Schema(description = "활성 섭취 중 상품 ID", nullable = true, example = "7")
            Long activeProductId,

            @Schema(description = "캐비닛에 추가한 일시", example = "2026-07-01T10:20:00")
            LocalDateTime addedAt
    ) {
    }

    @Schema(description = "후기 작성 유도 대상 조회 응답")
    @Builder
    public record ReviewPrompts(
            @Schema(description = "노출할 후기 작성 유도 인앱 배너 목록")
            List<ReviewPrompt> duePrompts
    ) {
    }

    @Schema(description = "후기 작성 유도 대상 항목")
    @Builder
    public record ReviewPrompt(
            @Schema(description = "배너 닫힘 기록에 사용할 활성 섭취 중 상품 ID", example = "7")
            Long activeProductId,

            @Schema(description = "후기 작성 화면 이동에 사용할 영양제 상품 ID", example = "112")
            Long productId,

            @Schema(description = "배너 안내 문구에 표시할 영양제 상품명", example = "뉴트리코어 유기농 비타민D 1000IU")
            String productName
    ) {
    }

    @Schema(description = "후기 작성 유도 배너 닫힘 기록 응답")
    @Builder
    public record ReviewPromptDismissed(
            @Schema(description = "닫힘 처리된 활성 섭취 중 상품 ID", example = "7")
            Long activeProductId,

            @Schema(description = "후기 작성 유도 인앱 배너 닫힘 일시", example = "2026-07-21T11:30:00")
            LocalDateTime dismissedAt
    ) {
    }

    @Schema(description = "캐비닛 개별 영양제 조회 응답")
    @Builder
    public record ProductDetail(
            @Schema(description = "회원 캐비닛 상품 ID", example = "15")
            Long memberProductId,

            @Schema(description = "영양제 상품 ID", example = "112")
            Long productId,

            @Schema(description = "브랜드명", example = "뉴트리코어")
            String brand,

            @Schema(description = "영양제 상품명", example = "뉴트리코어 유기농 비타민D 1000IU")
            String productName,

            @Schema(description = "모달에 표시할 썸네일 이미지 URL")
            String thumbnailImageUrl,

            @Schema(description = "현재 섭취 중인 영양제로 등록되어 있는지 여부", example = "true")
            Boolean isActiveIntake,

            @Schema(description = "사용자가 해당 상품에 작성한 활성 후기가 있는지 여부", example = "false")
            Boolean hasMyReview,

            @Schema(description = "포함 성분 목록")
            List<ProductIngredient> ingredients,

            @Schema(description = "활성 섭취 중 설정 정보", nullable = true)
            ActiveProduct activeProduct
    ) {
    }

    @Schema(description = "캐비닛 개별 영양제 포함 성분 항목")
    @Builder
    public record ProductIngredient(
            @Schema(description = "영양성분 ID", example = "2")
            Long ingredientId,

            @Schema(description = "성분명", example = "비타민 D")
            String name,

            @Schema(description = "성분 이미지 URL")
            String imageUrl,

            @Schema(description = "성분 설명")
            String description,

            @Schema(description = "성분 효능 태그 목록")
            List<String> effectTags
    ) {
    }

    @Schema(description = "활성 섭취 중 설정 정보")
    @Builder
    public record ActiveProduct(
            @Schema(description = "활성 섭취 중 상품 ID", example = "7")
            Long activeProductId,

            @Schema(description = "섭취 중으로 추가한 날짜", example = "2026-07-01")
            LocalDate startedOn,

            @Schema(description = "N일째 섭취 중 배지에 사용할 섭취 일수", example = "21")
            Integer intakeDayCount,

            @Schema(description = "개별 복용 알림 ON/OFF 여부", example = "true")
            Boolean notificationEnabled,

            @Schema(description = "복용 시간", example = "08:30")
            String intakeTime,

            @Schema(description = "복용 주기 enum", example = "EVERY_DAY")
            String frequency,

            @Schema(description = "화면에 표시할 복용 주기명", example = "매일")
            String frequencyLabel,

            @Schema(description = "복용 주기 간격 일수", example = "1")
            Integer frequencyIntervalDays,

            @Schema(description = "복용 예정일 계산 기준일", example = "2026-07-01")
            LocalDate scheduleAnchorOn
    ) {
    }
}
