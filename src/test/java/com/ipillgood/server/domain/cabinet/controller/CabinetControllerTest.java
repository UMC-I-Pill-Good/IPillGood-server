package com.ipillgood.server.domain.cabinet.controller;

import com.ipillgood.server.global.security.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CabinetControllerTest {

    private static final String CABINET_PRODUCTS_URL = "/api/v1/cabinet/products";
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

        insertIngredient(1L, "종합비타민", "ingredients/1.png");
        insertIngredient(2L, "비타민 D", "ingredients/2.png");
        insertIngredient(3L, "비타민 C", "ingredients/3.png");
        insertIngredient(4L, "마그네슘", "ingredients/4.png");
        insertIngredient(5L, "아연", "ingredients/5.png");

        insertProduct(100L, "비타민 D 제품", "테스트브랜드", null);
        insertProduct(101L, "멀티비타민 제품", "테스트브랜드", null);
        insertProduct(102L, "삭제된 보유 제품", "테스트브랜드", null);
        insertProduct(103L, "삭제된 상품", "테스트브랜드", "2026-07-01 00:00:00");
        insertProduct(104L, "다른 회원 제품", "테스트브랜드", null);

        insertProductIngredient(1L, 100L, 2L);
        insertProductIngredient(2L, 101L, 1L);
        insertProductIngredient(3L, 101L, 3L);
        insertProductIngredient(4L, 102L, 4L);
        insertProductIngredient(5L, 103L, 5L);
        insertProductIngredient(6L, 104L, 2L);

        insertMemberProduct(1L, MEMBER_ID, 100L, "2026-07-20 10:00:00", null);
        insertMemberProduct(2L, MEMBER_ID, 101L, "2026-07-21 10:00:00", null);
        insertMemberProduct(3L, MEMBER_ID, 102L, "2026-07-22 10:00:00", "2026-07-23 00:00:00");
        insertMemberProduct(4L, MEMBER_ID, 103L, "2026-07-23 10:00:00", null);
        insertMemberProduct(5L, OTHER_MEMBER_ID, 104L, "2026-07-24 10:00:00", null);
        restartMemberProductIdentity();

        insertMemberActiveProduct(10L, 1L, MEMBER_ID, null);
        insertMemberActiveProduct(11L, 2L, MEMBER_ID, "2026-07-22");

        accessToken = jwtProvider.createAccessToken(MEMBER_ID, "USER");
        emptyMemberAccessToken = jwtProvider.createAccessToken(EMPTY_MEMBER_ID, "USER");
        onboardingIncompleteAccessToken = jwtProvider.createAccessToken(ONBOARDING_INCOMPLETE_MEMBER_ID, "USER");
    }

    @Test
    @DisplayName("인증 없이 캐비닛 보유 영양제 목록을 조회하면 401을 반환한다")
    void getProducts_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get(CABINET_PRODUCTS_URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("인증 없이 캐비닛 영양제를 추가하면 401을 반환한다")
    void addProducts_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(post(CABINET_PRODUCTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productIds": [102]
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("온보딩을 완료하지 않은 회원은 캐비닛 보유 영양제 목록을 조회할 수 없다")
    void getProducts_withoutCompletedOnboarding_returnsForbidden() throws Exception {
        mockMvc.perform(get(CABINET_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(onboardingIncompleteAccessToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("CABINET403_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("온보딩을 완료하지 않은 회원은 캐비닛 영양제를 추가할 수 없다")
    void addProducts_withoutCompletedOnboarding_returnsForbidden() throws Exception {
        mockMvc.perform(post(CABINET_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(onboardingIncompleteAccessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productIds": [102]
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("CABINET403_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("캐비닛 보유 영양제 목록을 추가일 내림차순으로 조회한다")
    void getProducts_returnsActiveCabinetProductsOrderedByAddedAtDesc() throws Exception {
        mockMvc.perform(get(CABINET_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.message").value("요청이 성공적으로 처리되었습니다."))
                .andExpect(jsonPath("$.result.memberNickname").value("필굿"))
                .andExpect(jsonPath("$.result.totalCount").value(2))
                .andExpect(jsonPath("$.result.products.length()").value(2))
                .andExpect(jsonPath("$.result.products[*].memberProductId", contains(2, 1)))
                .andExpect(jsonPath("$.result.products[*].productId", contains(101, 100)))
                .andExpect(jsonPath("$.result.products[0].productName").value("멀티비타민 제품"))
                .andExpect(jsonPath("$.result.products[0].thumbnailImageUrl")
                        .value(matchesPattern("https://ipillgood-bucket\\.s3\\.ap-northeast-2\\.amazonaws\\.com/ingredients/other[1-4]\\.png")))
                .andExpect(jsonPath("$.result.products[0].isActiveIntake").value(false))
                .andExpect(jsonPath("$.result.products[0].activeProductId").doesNotExist())
                .andExpect(jsonPath("$.result.products[0].addedAt").value("2026-07-21T10:00:00"))
                .andExpect(jsonPath("$.result.products[1].productName").value("비타민 D 제품"))
                .andExpect(jsonPath("$.result.products[1].thumbnailImageUrl")
                        .value("https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/ingredients/2.png"))
                .andExpect(jsonPath("$.result.products[1].isActiveIntake").value(true))
                .andExpect(jsonPath("$.result.products[1].activeProductId").value(10))
                .andExpect(jsonPath("$.result.products[1].addedAt").value("2026-07-20T10:00:00"));
    }

    @Test
    @DisplayName("캐비닛 보유 영양제가 없으면 빈 목록을 반환한다")
    void getProducts_withNoCabinetProducts_returnsEmptyList() throws Exception {
        mockMvc.perform(get(CABINET_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(emptyMemberAccessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.memberNickname").value("빈회원"))
                .andExpect(jsonPath("$.result.totalCount").value(0))
                .andExpect(jsonPath("$.result.products.length()").value(0));
    }

    @Test
    @DisplayName("캐비닛에 영양제를 복수 추가한다")
    void addProducts_withValidProducts_returnsCreated() throws Exception {
        int beforeCount = countMemberProducts();

        mockMvc.perform(post(CABINET_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productIds": [102, 104]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS201_1"))
                .andExpect(jsonPath("$.message").value("리소스가 성공적으로 생성되었습니다."))
                .andExpect(jsonPath("$.result.addedCount").value(2))
                .andExpect(jsonPath("$.result.addedProducts.length()").value(2))
                .andExpect(jsonPath("$.result.addedProducts[*].productId", contains(102, 104)))
                .andExpect(jsonPath("$.result.addedProducts[*].brand", contains("테스트브랜드", "테스트브랜드")))
                .andExpect(jsonPath("$.result.addedProducts[0].productName").value("삭제된 보유 제품"))
                .andExpect(jsonPath("$.result.addedProducts[0].thumbnailImageUrl")
                        .value("https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/ingredients/4.png"))
                .andExpect(jsonPath("$.result.addedProducts[0].addedAt")
                        .value(matchesPattern("\\d{4}-\\d{2}-\\d{2}T.+")))
                .andExpect(jsonPath("$.result.addedProducts[1].productName").value("다른 회원 제품"))
                .andExpect(jsonPath("$.result.addedProducts[1].thumbnailImageUrl")
                        .value("https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/ingredients/2.png"));

        assertEquals(beforeCount + 2, countMemberProducts());
        assertEquals(2, countMemberProducts(MEMBER_ID, 102L));
        assertEquals(1, countActiveMemberProducts(MEMBER_ID, 102L));
        assertEquals(1, countActiveMemberProducts(MEMBER_ID, 104L));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}",
            "{\"productIds\":null}",
            "{\"productIds\":[]}",
            "{\"productIds\":[104,104]}",
            "{\"productIds\":[null]}",
            "{\"productIds\":[0]}",
            "{\"productIds\":[-1]}"
    })
    @DisplayName("캐비닛 추가 상품 목록이 올바르지 않으면 400을 반환하고 추가하지 않는다")
    void addProducts_withInvalidProductIds_returnsBadRequest(String requestBody) throws Exception {
        int beforeCount = countMemberProducts();

        mockMvc.perform(post(CABINET_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("CABINET400_2"))
                .andExpect(jsonPath("$.result").doesNotExist());

        assertEquals(beforeCount, countMemberProducts());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"productIds\":[104,999]}",
            "{\"productIds\":[104,103]}"
    })
    @DisplayName("존재하지 않거나 삭제된 상품이 포함되면 404를 반환하고 일부만 추가하지 않는다")
    void addProducts_withUnknownOrDeletedProduct_returnsNotFound(String requestBody) throws Exception {
        int beforeCount = countMemberProducts();

        mockMvc.perform(post(CABINET_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("CABINET404_1"))
                .andExpect(jsonPath("$.result").doesNotExist());

        assertEquals(beforeCount, countMemberProducts());
        assertEquals(0, countActiveMemberProducts(MEMBER_ID, 104L));
    }

    @Test
    @DisplayName("이미 보유 중인 영양제가 포함되면 409를 반환하고 일부만 추가하지 않는다")
    void addProducts_withAlreadyOwnedProduct_returnsConflict() throws Exception {
        int beforeCount = countMemberProducts();

        mockMvc.perform(post(CABINET_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productIds": [100, 104]
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("CABINET409_1"))
                .andExpect(jsonPath("$.result").doesNotExist());

        assertEquals(beforeCount, countMemberProducts());
        assertEquals(0, countActiveMemberProducts(MEMBER_ID, 104L));
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

    private void restartMemberProductIdentity() {
        jdbcTemplate.execute("ALTER TABLE member_product ALTER COLUMN id RESTART WITH 100");
    }

    private void insertMemberActiveProduct(Long id, Long memberProductId, Long memberId, String stoppedOn) {
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
                            created_at,
                            updated_at
                        )
                        VALUES (?, ?, ?, '2026-07-01', ?, '09:00:00', 'EVERY_DAY', 1,
                                '2026-07-01', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                id,
                memberProductId,
                memberId,
                stoppedOn
        );
    }

    private int countMemberProducts() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM member_product", Integer.class);
        return count == null ? 0 : count;
    }

    private int countMemberProducts(Long memberId, Long productId) {
        Integer count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM member_product
                        WHERE member_id = ?
                          AND product_id = ?
                        """,
                Integer.class,
                memberId,
                productId
        );
        return count == null ? 0 : count;
    }

    private int countActiveMemberProducts(Long memberId, Long productId) {
        Integer count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM member_product
                        WHERE member_id = ?
                          AND product_id = ?
                          AND deleted_at IS NULL
                        """,
                Integer.class,
                memberId,
                productId
        );
        return count == null ? 0 : count;
    }

    private String bearerToken(String token) {
        return "Bearer " + token;
    }
}
