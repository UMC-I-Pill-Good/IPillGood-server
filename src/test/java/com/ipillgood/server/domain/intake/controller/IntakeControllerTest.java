package com.ipillgood.server.domain.intake.controller;

import com.ipillgood.server.global.security.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IntakeControllerTest {

    private static final String ACTIVE_PRODUCTS_URL = "/api/v1/intake/active-products";
    private static final String COMPATIBILITY_CHECKS_URL = "/api/v1/intake/compatibility-checks";
    private static final long MEMBER_ID = 1L;
    private static final long OTHER_MEMBER_ID = 2L;
    private static final long EMPTY_MEMBER_ID = 3L;
    private static final long ONBOARDING_INCOMPLETE_MEMBER_ID = 4L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtProvider jwtProvider;

    private String accessToken;
    private String emptyMemberAccessToken;
    private String onboardingIncompleteAccessToken;

    @BeforeEach
    void setUp() {
        clearDatabase();

        insertMember(MEMBER_ID, "필굿", "2026-07-01 00:00:00");
        insertMember(OTHER_MEMBER_ID, "다른회원", "2026-07-01 00:00:00");
        insertMember(EMPTY_MEMBER_ID, "빈회원", "2026-07-01 00:00:00");
        insertMember(ONBOARDING_INCOMPLETE_MEMBER_ID, "미완료", null);

        insertIngredient(1L, "비타민 D", "ingredients/1.png");
        insertIngredient(2L, "비타민 C", "ingredients/2.png");
        insertIngredient(3L, "아연", "ingredients/3.png");
        insertIngredient(4L, "철", "ingredients/4.png");
        insertIngredient(5L, "마그네슘", "ingredients/5.png");
        insertIngredient(6L, "프로바이오틱스", "ingredients/6.png");

        insertProduct(100L, "비타민 D 제품", "테스트브랜드", null);
        insertProduct(101L, "멀티비타민 제품", "테스트브랜드", null);
        insertProduct(102L, "중단된 제품", "테스트브랜드", null);
        insertProduct(103L, "삭제된 캐비닛 제품", "테스트브랜드", null);
        insertProduct(104L, "삭제된 상품", "테스트브랜드", "2026-07-01 00:00:00");
        insertProduct(105L, "다른 회원 제품", "테스트브랜드", null);
        insertProduct(106L, "먼저 등록한 제품", "테스트브랜드", null);
        insertProduct(107L, "철 마그네슘 제품", "테스트브랜드", null);
        insertProduct(108L, "프로바이오틱스 제품", "테스트브랜드", null);
        insertProduct(109L, "빈회원 철 제품", "테스트브랜드", null);

        insertProductIngredient(1L, 100L, 1L);
        insertProductIngredient(2L, 101L, 1L);
        insertProductIngredient(3L, 101L, 2L);
        insertProductIngredient(4L, 102L, 2L);
        insertProductIngredient(5L, 103L, 2L);
        insertProductIngredient(6L, 104L, 2L);
        insertProductIngredient(7L, 105L, 3L);
        insertProductIngredient(8L, 106L, 3L);
        insertProductIngredient(9L, 107L, 4L);
        insertProductIngredient(10L, 107L, 5L);
        insertProductIngredient(11L, 108L, 6L);
        insertProductIngredient(12L, 109L, 4L);

        insertIngredientCombination(1L, 1L, 4L, "CAUTION", "동시 복용 시 흡수에 영향을 줄 수 있습니다.");
        insertIngredientCombination(2L, 5L, 3L, "CONTRAINDICATION", "함께 복용하는 것이 권장되지 않습니다.");
        insertIngredientCombination(3L, 2L, 4L, "GOOD", "함께 섭취하면 좋습니다.");

        insertMemberProduct(1L, MEMBER_ID, 100L, "2026-07-01 10:00:00", null);
        insertMemberProduct(2L, MEMBER_ID, 101L, "2026-07-01 10:00:00", null);
        insertMemberProduct(3L, MEMBER_ID, 102L, "2026-07-01 10:00:00", null);
        insertMemberProduct(4L, MEMBER_ID, 103L, "2026-07-01 10:00:00", "2026-07-02 00:00:00");
        insertMemberProduct(5L, MEMBER_ID, 104L, "2026-07-01 10:00:00", null);
        insertMemberProduct(6L, OTHER_MEMBER_ID, 105L, "2026-07-01 10:00:00", null);
        insertMemberProduct(7L, MEMBER_ID, 106L, "2026-07-01 10:00:00", null);
        insertMemberProduct(8L, MEMBER_ID, 107L, "2026-07-01 10:00:00", null);
        insertMemberProduct(9L, MEMBER_ID, 108L, "2026-07-01 10:00:00", null);
        insertMemberProduct(16L, EMPTY_MEMBER_ID, 109L, "2026-07-01 10:00:00", null);

        insertMemberActiveProduct(10L, 1L, MEMBER_ID, null, "2026-07-02 09:00:00");
        insertMemberActiveProduct(11L, 2L, MEMBER_ID, null, "2026-07-02 09:00:00");
        insertMemberActiveProduct(12L, 3L, MEMBER_ID, "2026-07-03", "2026-07-01 09:00:00");
        insertMemberActiveProduct(13L, 4L, MEMBER_ID, null, "2026-07-01 08:00:00");
        insertMemberActiveProduct(14L, 5L, MEMBER_ID, null, "2026-07-01 08:00:00");
        insertMemberActiveProduct(15L, 6L, OTHER_MEMBER_ID, null, "2026-07-01 08:00:00");
        insertMemberActiveProduct(20L, 7L, MEMBER_ID, null, "2026-06-30 09:00:00");

        accessToken = jwtProvider.createAccessToken(MEMBER_ID, "USER");
        emptyMemberAccessToken = jwtProvider.createAccessToken(EMPTY_MEMBER_ID, "USER");
        onboardingIncompleteAccessToken = jwtProvider.createAccessToken(ONBOARDING_INCOMPLETE_MEMBER_ID, "USER");
    }

    @Test
    @DisplayName("인증 없이 섭취 중 영양제 목록을 조회하면 401을 반환한다")
    void getActiveProducts_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get(ACTIVE_PRODUCTS_URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("온보딩을 완료하지 않은 회원은 섭취 중 영양제 목록을 조회할 수 없다")
    void getActiveProducts_withoutCompletedOnboarding_returnsForbidden() throws Exception {
        mockMvc.perform(get(ACTIVE_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(onboardingIncompleteAccessToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE403_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("활성 섭취 중 영양제 목록을 등록 순서와 ID 순서로 조회한다")
    void getActiveProducts_returnsOnlyAvailableActiveProductsOrderedByCreatedAtAndId() throws Exception {
        mockMvc.perform(get(ACTIVE_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.message").value("요청이 성공적으로 처리되었습니다."))
                .andExpect(jsonPath("$.result.totalCount").value(3))
                .andExpect(jsonPath("$.result.activeProducts.length()").value(3))
                .andExpect(jsonPath("$.result.activeProducts[*].activeProductId", contains(20, 10, 11)))
                .andExpect(jsonPath("$.result.activeProducts[*].memberProductId", contains(7, 1, 2)))
                .andExpect(jsonPath("$.result.activeProducts[*].productId", contains(106, 100, 101)))
                .andExpect(jsonPath("$.result.activeProducts[0].productName").value("먼저 등록한 제품"))
                .andExpect(jsonPath("$.result.activeProducts[0].thumbnailImageUrl")
                        .value("https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/ingredients/3.png"))
                .andExpect(jsonPath("$.result.activeProducts[1].productName").value("비타민 D 제품"))
                .andExpect(jsonPath("$.result.activeProducts[1].thumbnailImageUrl")
                        .value("https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/ingredients/1.png"))
                .andExpect(jsonPath("$.result.activeProducts[2].productName").value("멀티비타민 제품"))
                .andExpect(jsonPath("$.result.activeProducts[2].thumbnailImageUrl")
                        .value(matchesPattern("https://ipillgood-bucket\\.s3\\.ap-northeast-2\\.amazonaws\\.com/ingredients/other[1-4]\\.png")));
    }

    @Test
    @DisplayName("섭취 중 영양제가 없으면 빈 목록을 반환한다")
    void getActiveProducts_withNoActiveProducts_returnsEmptyList() throws Exception {
        mockMvc.perform(get(ACTIVE_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(emptyMemberAccessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.totalCount").value(0))
                .andExpect(jsonPath("$.result.activeProducts.length()").value(0));
    }

    @Test
    @DisplayName("인증 없이 병용 금기 확인을 요청하면 401을 반환한다")
    void checkCompatibility_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(post(COMPATIBILITY_CHECKS_URL)
                        .contentType("application/json")
                        .content("""
                                {
                                  "memberProductId": 8
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("온보딩을 완료하지 않은 회원은 병용 금기 확인을 요청할 수 없다")
    void checkCompatibility_withoutCompletedOnboarding_returnsForbidden() throws Exception {
        mockMvc.perform(post(COMPATIBILITY_CHECKS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(onboardingIncompleteAccessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "memberProductId": 8
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE403_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("병용 금기 확인 요청 본문이 없으면 400을 반환한다")
    void checkCompatibility_withoutBody_returnsBadRequest() throws Exception {
        mockMvc.perform(post(COMPATIBILITY_CHECKS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE400_2"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("memberProductId가 없으면 400을 반환한다")
    void checkCompatibility_withoutMemberProductId_returnsBadRequest() throws Exception {
        mockMvc.perform(post(COMPATIBILITY_CHECKS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE400_2"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("memberProductId가 1 미만이면 400을 반환한다")
    void checkCompatibility_withInvalidMemberProductId_returnsBadRequest() throws Exception {
        mockMvc.perform(post(COMPATIBILITY_CHECKS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "memberProductId": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE400_2"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("등록 대상 캐비닛 상품이 현재 회원의 활성 보유 상품이 아니면 404를 반환한다")
    void checkCompatibility_withUnavailableTarget_returnsNotFound() throws Exception {
        mockMvc.perform(post(COMPATIBILITY_CHECKS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "memberProductId": 6
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE404_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("등록 대상 캐비닛 상품이 삭제되었으면 404를 반환한다")
    void checkCompatibility_withDeletedCabinetProduct_returnsNotFound() throws Exception {
        mockMvc.perform(post(COMPATIBILITY_CHECKS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "memberProductId": 4
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE404_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("등록 대상 상품이 삭제되었으면 404를 반환한다")
    void checkCompatibility_withDeletedProduct_returnsNotFound() throws Exception {
        mockMvc.perform(post(COMPATIBILITY_CHECKS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "memberProductId": 5
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE404_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("등록 대상 캐비닛 상품이 없으면 404를 반환한다")
    void checkCompatibility_withUnknownTarget_returnsNotFound() throws Exception {
        mockMvc.perform(post(COMPATIBILITY_CHECKS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "memberProductId": 999
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE404_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("이미 섭취 중인 캐비닛 상품이면 409를 반환한다")
    void checkCompatibility_withAlreadyActiveTarget_returnsConflict() throws Exception {
        mockMvc.perform(post(COMPATIBILITY_CHECKS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "memberProductId": 1
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE409_1"))
                .andExpect(jsonPath("$.message").value("이미 섭취 중인 영양제입니다."))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("활성 섭취 중 상품과 등록 대상 상품의 주의/금기 성분 조합을 조회한다")
    void checkCompatibility_withConflicts_returnsWarningsOnly() throws Exception {
        mockMvc.perform(post(COMPATIBILITY_CHECKS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "memberProductId": 8
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.message").value("요청이 성공적으로 처리되었습니다."))
                .andExpect(jsonPath("$.result.hasConflicts").value(true))
                .andExpect(jsonPath("$.result.conflicts.length()").value(2))
                .andExpect(jsonPath("$.result.conflicts[*].combinationType",
                        contains("CAUTION", "CONTRAINDICATION")))
                .andExpect(jsonPath("$.result.conflicts[*].combinationType", not(hasItem("GOOD"))))
                .andExpect(jsonPath("$.result.conflicts[0].currentIngredientId").value(1))
                .andExpect(jsonPath("$.result.conflicts[0].currentIngredientName").value("비타민 D"))
                .andExpect(jsonPath("$.result.conflicts[0].targetIngredientId").value(4))
                .andExpect(jsonPath("$.result.conflicts[0].targetIngredientName").value("철"))
                .andExpect(jsonPath("$.result.conflicts[0].reason")
                        .value("동시 복용 시 흡수에 영향을 줄 수 있습니다."))
                .andExpect(jsonPath("$.result.conflicts[1].currentIngredientId").value(3))
                .andExpect(jsonPath("$.result.conflicts[1].currentIngredientName").value("아연"))
                .andExpect(jsonPath("$.result.conflicts[1].targetIngredientId").value(5))
                .andExpect(jsonPath("$.result.conflicts[1].targetIngredientName").value("마그네슘"))
                .andExpect(jsonPath("$.result.conflicts[1].reason")
                        .value("함께 복용하는 것이 권장되지 않습니다."));
    }

    @Test
    @DisplayName("병용 금기 조합이 없으면 빈 목록을 반환한다")
    void checkCompatibility_withNoMatchingConflicts_returnsEmptyConflicts() throws Exception {
        mockMvc.perform(post(COMPATIBILITY_CHECKS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "memberProductId": 9
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.hasConflicts").value(false))
                .andExpect(jsonPath("$.result.conflicts.length()").value(0));
    }

    @Test
    @DisplayName("현재 활성 섭취 중 상품이 없으면 빈 목록을 반환한다")
    void checkCompatibility_withNoActiveProducts_returnsEmptyConflicts() throws Exception {
        mockMvc.perform(post(COMPATIBILITY_CHECKS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(emptyMemberAccessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "memberProductId": 16
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.hasConflicts").value(false))
                .andExpect(jsonPath("$.result.conflicts.length()").value(0));
    }

    private void clearDatabase() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.update("DELETE FROM product_review_report");
        jdbcTemplate.update("DELETE FROM product_review_helpful");
        jdbcTemplate.update("DELETE FROM product_review_image");
        jdbcTemplate.update("DELETE FROM product_review");
        jdbcTemplate.update("DELETE FROM intake_record");
        jdbcTemplate.update("DELETE FROM intake_day");
        jdbcTemplate.update("DELETE FROM member_active_product");
        jdbcTemplate.update("DELETE FROM member_product");
        jdbcTemplate.update("DELETE FROM product_ingredient");
        jdbcTemplate.update("DELETE FROM health_concern_ingredient");
        jdbcTemplate.update("DELETE FROM contraindication_ingredient");
        jdbcTemplate.update("DELETE FROM ingredient_combination");
        jdbcTemplate.update("DELETE FROM ingredient_age_group");
        jdbcTemplate.update("DELETE FROM ingredient_effect");
        jdbcTemplate.update("DELETE FROM ingredient_caution");
        jdbcTemplate.update("DELETE FROM effect_keyword");
        jdbcTemplate.update("DELETE FROM alternative_food");
        jdbcTemplate.update("DELETE FROM recommendation_item");
        jdbcTemplate.update("DELETE FROM recommendation_feedback_cycle");
        jdbcTemplate.update("DELETE FROM recommendation");
        jdbcTemplate.update("DELETE FROM survey_current_ingredient_selection");
        jdbcTemplate.update("DELETE FROM survey_contraindication_selection");
        jdbcTemplate.update("DELETE FROM survey_onboarding_concern_selection");
        jdbcTemplate.update("DELETE FROM survey_response");
        jdbcTemplate.update("DELETE FROM notification_delivery_log");
        jdbcTemplate.update("DELETE FROM member_push_token");
        jdbcTemplate.update("DELETE FROM member_notification_setting");
        jdbcTemplate.update("DELETE FROM member_search_keyword");
        jdbcTemplate.update("DELETE FROM condition_popup_log");
        jdbcTemplate.update("DELETE FROM condition_weekly_record");
        jdbcTemplate.update("DELETE FROM member_policy_agreement");
        jdbcTemplate.update("DELETE FROM member_social_account");
        jdbcTemplate.update("DELETE FROM product");
        jdbcTemplate.update("DELETE FROM ingredient");
        jdbcTemplate.update("DELETE FROM contraindication");
        jdbcTemplate.update("DELETE FROM member");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }

    private void insertMember(Long id, String nickname, String onboardingCompletedAt) {
        jdbcTemplate.update("""
                        INSERT INTO member (
                            id,
                            nickname,
                            username,
                            email,
                            password,
                            role,
                            status,
                            profile_image_key,
                            onboarding_completed_at,
                            last_login_at,
                            created_at,
                            updated_at
                        )
                        VALUES (?, ?, ?, ?, NULL, 'USER', 'ACTIVE',
                                'mascot-default', ?, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                id,
                nickname,
                "user" + id,
                "user" + id + "@example.com",
                onboardingCompletedAt
        );
    }

    private void insertIngredient(Long id, String name, String imageKey) {
        jdbcTemplate.update("""
                        INSERT INTO ingredient (
                            id,
                            name,
                            description,
                            recommended_intake,
                            recommended_intake_time,
                            recommended_gender,
                            image_key,
                            ad_claim_risk,
                            created_at,
                            updated_at
                        )
                        VALUES (?, ?, '성분 설명', NULL, NULL, 'BOTH', ?, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                id,
                name,
                imageKey
        );
    }

    private void insertIngredientCombination(
            Long id,
            Long ingredientAId,
            Long ingredientBId,
            String type,
            String reason
    ) {
        jdbcTemplate.update("""
                        INSERT INTO ingredient_combination (
                            id,
                            ingredient_a_id,
                            ingredient_b_id,
                            type,
                            reason,
                            created_at,
                            updated_at
                        )
                        VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                id,
                ingredientAId,
                ingredientBId,
                type,
                reason
        );
    }

    private void insertProduct(Long id, String name, String brand, String deletedAt) {
        jdbcTemplate.update("""
                        INSERT INTO product (
                            id,
                            name,
                            brand,
                            description,
                            purchase_url,
                            mfds_certified,
                            deleted_at,
                            created_at,
                            updated_at
                        )
                        VALUES (?, ?, ?, '상품 설명', 'https://example.com', true, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                id,
                name,
                brand,
                deletedAt
        );
    }

    private void insertProductIngredient(Long id, Long productId, Long ingredientId) {
        jdbcTemplate.update("""
                        INSERT INTO product_ingredient (id, product_id, ingredient_id, created_at, updated_at)
                        VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                id,
                productId,
                ingredientId
        );
    }

    private void insertMemberProduct(
            Long id,
            Long memberId,
            Long productId,
            String addedAt,
            String deletedAt
    ) {
        jdbcTemplate.update("""
                        INSERT INTO member_product (
                            id,
                            member_id,
                            product_id,
                            added_at,
                            deleted_at,
                            created_at,
                            updated_at
                        )
                        VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                id,
                memberId,
                productId,
                addedAt,
                deletedAt
        );
    }

    private void insertMemberActiveProduct(
            Long id,
            Long memberProductId,
            Long memberId,
            String stoppedOn,
            String createdAt
    ) {
        jdbcTemplate.update("""
                        INSERT INTO member_active_product (
                            id,
                            member_product_id,
                            member_id,
                            started_on,
                            stopped_on,
                            intake_time,
                            frequency,
                            frequency_interval_days,
                            schedule_anchor_on,
                            notification_enabled,
                            review_prompt_dismissed_at,
                            created_at,
                            updated_at
                        )
                        VALUES (?, ?, ?, '2026-07-01', ?, '09:00:00', 'EVERY_DAY', 1,
                                '2026-07-01', true, NULL, ?, ?)
                        """,
                id,
                memberProductId,
                memberId,
                stoppedOn,
                createdAt,
                createdAt
        );
    }

    private String bearerToken(String token) {
        return "Bearer " + token;
    }
}
