package com.ipillgood.server.domain.ingredient.controller;

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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IngredientControllerTest {

    private static final String CONTRAINDICATIONS_URL = "/api/v1/contraindications";
    private static final String INGREDIENTS_URL = "/api/v1/ingredients";
    private static final long MEMBER_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtProvider jwtProvider;

    private String accessToken;

    @BeforeEach
    void setUp() {
        clearDatabase();

        insertMember(MEMBER_ID);
        insertIngredient(1L, "종합비타민", "ingredients/1.png", "종합비타민 설명", null, "식후 섭취 권장");
        insertIngredient(2L, "비타민 D", "ingredients/2.png", "칼슘 흡수와 뼈 건강에 도움을 주는 성분입니다.", "3 ~ 10μg", "식후 섭취 권장");
        insertIngredient(3L, "비타민 C", "/ingredients/3.png", "항산화에 도움을 주는 성분입니다.", null, null);
        insertIngredient(10L, "칼슘", "ingredients/10.png", "뼈 건강에 도움을 주는 성분입니다.", null, null);
        insertIngredient(13L, "철분", "ingredients/13.png", "혈액 생성에 도움을 주는 성분입니다.", null, null);

        insertIngredientEffect(1L, 2L, "칼슘 흡수에 도움");
        insertIngredientEffect(2L, 2L, "뼈 건강 유지에 도움");
        insertIngredientCaution(1L, 2L, "과다 섭취 시 고칼슘혈증 위험이 있습니다.");
        insertAlternativeFood(1L, 2L, "연어", "100g당 비타민 D 10μg");
        insertAlternativeFood(2L, 2L, "달걀", "100g당 비타민 D 2μg");

        insertIngredientCombination(1L, 2L, 10L, "CAUTION", "동시 복용 시 흡수에 영향을 줄 수 있습니다.");
        insertIngredientCombination(2L, 1L, 2L, "CAUTION", "종합비타민에 포함된 미네랄이 흡수에 영향을 줄 수 있습니다.");
        insertIngredientCombination(3L, 2L, 3L, "GOOD", "함께 섭취하면 좋습니다.");
        insertIngredientCombination(4L, 2L, 13L, "CONTRAINDICATION", "현재 성분 상세 응답에서는 제외합니다.");

        insertContraindication(1L, "MEDICATION", "항생제");
        insertContraindication(2L, "MEDICATION", "면역억제제");
        insertContraindication(20L, "DRINKING", "음주");
        insertContraindication(21L, "PREGNANCY", "임신");
        insertContraindication(22L, "SMOKING", "흡연");
        insertContraindication(23L, "UNDERLYING_DISEASE", "고칼슘혈증");
        insertContraindication(24L, "UNDERLYING_DISEASE", "천식");
        insertContraindication(32L, "ALLERGY", "피부 관련");

        insertProduct(100L, "비타민 D 제품", "테스트브랜드", null);
        insertProduct(101L, "비타민 C 제품", "테스트브랜드", null);
        insertProductIngredient(1L, 100L, 2L);
        insertProductIngredient(2L, 101L, 3L);
        insertMemberProduct(1L, MEMBER_ID, 100L, null);
        insertMemberProduct(2L, MEMBER_ID, 101L, "2026-01-01 00:00:00");

        accessToken = jwtProvider.createAccessToken(MEMBER_ID, "USER");
    }

    @Test
    @DisplayName("인증 없이 영양성분 목록을 조회하면 401을 반환한다")
    void getIngredients_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get(INGREDIENTS_URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("영양성분 목록을 전체 조회한다")
    void getIngredients_returnsAllIngredients() throws Exception {
        mockMvc.perform(get(INGREDIENTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.message").value("요청이 성공적으로 처리되었습니다."))
                .andExpect(jsonPath("$.result.ingredients.length()").value(5))
                .andExpect(jsonPath("$.result.ingredients[*].ingredientId", contains(1, 2, 3, 10, 13)))
                .andExpect(jsonPath("$.result.ingredients[0].name").value("종합비타민"))
                .andExpect(jsonPath("$.result.ingredients[0].imageUrl")
                        .value("https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/ingredients/1.png"))
                .andExpect(jsonPath("$.result.ingredients[2].imageUrl")
                        .value("https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/ingredients/3.png"))
                .andExpect(jsonPath("$.result.page").doesNotExist())
                .andExpect(jsonPath("$.result.size").doesNotExist())
                .andExpect(jsonPath("$.result.totalElements").doesNotExist());
    }

    @Test
    @DisplayName("인증 없이 영양성분 상세를 조회하면 401을 반환한다")
    void getIngredient_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get(INGREDIENTS_URL + "/2"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("영양성분 상세를 조회한다")
    void getIngredient_returnsDetail() throws Exception {
        mockMvc.perform(get(INGREDIENTS_URL + "/2")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.result.ingredientId").value(2))
                .andExpect(jsonPath("$.result.name").value("비타민 D"))
                .andExpect(jsonPath("$.result.description").value("칼슘 흡수와 뼈 건강에 도움을 주는 성분입니다."))
                .andExpect(jsonPath("$.result.imageUrl")
                        .value("https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/ingredients/2.png"))
                .andExpect(jsonPath("$.result.effects", contains("칼슘 흡수에 도움", "뼈 건강 유지에 도움")))
                .andExpect(jsonPath("$.result.cautions", contains("과다 섭취 시 고칼슘혈증 위험이 있습니다.")))
                .andExpect(jsonPath("$.result.contraindicatedCombinations.length()").value(2))
                .andExpect(jsonPath("$.result.contraindicatedCombinations[0].targetIngredientId").value(10))
                .andExpect(jsonPath("$.result.contraindicatedCombinations[0].targetIngredientName").value("칼슘"))
                .andExpect(jsonPath("$.result.contraindicatedCombinations[0].type").value("CAUTION"))
                .andExpect(jsonPath("$.result.contraindicatedCombinations[0].reason")
                        .value("동시 복용 시 흡수에 영향을 줄 수 있습니다."))
                .andExpect(jsonPath("$.result.contraindicatedCombinations[1].targetIngredientId").value(1))
                .andExpect(jsonPath("$.result.contraindicatedCombinations[1].targetIngredientName").value("종합비타민"))
                .andExpect(jsonPath("$.result.contraindicatedCombinations[*].type", not(hasItem("GOOD"))))
                .andExpect(jsonPath("$.result.contraindicatedCombinations[*].type", not(hasItem("CONTRAINDICATION"))))
                .andExpect(jsonPath("$.result.recommendedIntake").value("3 ~ 10μg"))
                .andExpect(jsonPath("$.result.recommendedIntakeTime").value("식후 섭취 권장"))
                .andExpect(jsonPath("$.result.hasCabinetProduct").value(true))
                .andExpect(jsonPath("$.result.alternativeFoods[0].name").value("연어"))
                .andExpect(jsonPath("$.result.alternativeFoods[0].contentPer100g").value("100g당 비타민 D 10μg"))
                .andExpect(jsonPath("$.result.alternativeFoods[1].name").value("달걀"));
    }

    @Test
    @DisplayName("캐비닛의 삭제된 상품만 현재 성분을 포함하면 hasCabinetProduct=false를 반환한다")
    void getIngredient_withDeletedCabinetProduct_returnsFalseCabinetFlag() throws Exception {
        mockMvc.perform(get(INGREDIENTS_URL + "/3")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.hasCabinetProduct").value(false));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1"})
    @DisplayName("ingredientId가 1 미만이면 400을 반환한다")
    void getIngredient_withInvalidId_returnsBadRequest(String ingredientId) throws Exception {
        mockMvc.perform(get(INGREDIENTS_URL + "/" + ingredientId)
                .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INGREDIENT400_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("ingredientId가 숫자 형식이 아니면 공통 400을 반환한다")
    void getIngredient_withNonNumericId_returnsCommonBadRequest() throws Exception {
        mockMvc.perform(get(INGREDIENTS_URL + "/abc")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON400_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("존재하지 않는 영양성분 상세를 조회하면 404를 반환한다")
    void getIngredient_withUnknownId_returnsNotFound() throws Exception {
        mockMvc.perform(get(INGREDIENTS_URL + "/999")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INGREDIENT404_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("인증 없이 금기 조건 목록을 조회하면 401을 반환한다")
    void getContraindications_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get(CONTRAINDICATIONS_URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("설문용 금기 조건 목록을 그룹 순서에 맞게 조회한다")
    void getContraindications_returnsSurveyGroups() throws Exception {
        mockMvc.perform(get(CONTRAINDICATIONS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.message").value("요청이 성공적으로 처리되었습니다."))
                .andExpect(jsonPath("$.result.groups.length()").value(3))
                .andExpect(jsonPath("$.result.groups[*].type",
                        contains("UNDERLYING_DISEASE", "MEDICATION", "ALLERGY")))
                .andExpect(jsonPath("$.result.groups[*].label",
                        contains("기저질환", "현재 복용 중인 약", "알러지")))
                .andExpect(jsonPath("$.result.groups[0].items[*].contraindicationId", contains(23, 24)))
                .andExpect(jsonPath("$.result.groups[1].items[*].contraindicationId", contains(1, 2)))
                .andExpect(jsonPath("$.result.groups[2].items[*].contraindicationId", contains(32)))
                .andExpect(jsonPath("$.result.contraindications").doesNotExist())
                .andExpect(jsonPath("$.result.groupedByType").doesNotExist());
    }

    private void clearDatabase() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.update("DELETE FROM product_review_report");
        jdbcTemplate.update("DELETE FROM product_review_helpful");
        jdbcTemplate.update("DELETE FROM product_review_image");
        jdbcTemplate.update("DELETE FROM product_review");
        jdbcTemplate.update("DELETE FROM intake_record");
        jdbcTemplate.update("DELETE FROM intake_day");
        jdbcTemplate.update("DELETE FROM member_active_product_schedule_history");
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

    private void insertMember(Long id) {
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
                        VALUES (?, '테스터', 'tester', 'tester@example.com', NULL, 'USER', 'ACTIVE',
                                'mascot-default', NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                id
        );
    }

    private void insertIngredient(
            Long id,
            String name,
            String imageKey,
            String description,
            String recommendedIntake,
            String recommendedIntakeTime
    ) {
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
                        VALUES (?, ?, ?, ?, ?, 'BOTH', ?, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                id,
                name,
                description,
                recommendedIntake,
                recommendedIntakeTime,
                imageKey
        );
    }

    private void insertIngredientEffect(Long id, Long ingredientId, String effect) {
        jdbcTemplate.update("""
                        INSERT INTO ingredient_effect (id, ingredient_id, effect, created_at, updated_at)
                        VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                id,
                ingredientId,
                effect
        );
    }

    private void insertIngredientCaution(Long id, Long ingredientId, String caution) {
        jdbcTemplate.update("""
                        INSERT INTO ingredient_caution (id, ingredient_id, caution, created_at, updated_at)
                        VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                id,
                ingredientId,
                caution
        );
    }

    private void insertAlternativeFood(Long id, Long ingredientId, String name, String contentPer100g) {
        jdbcTemplate.update("""
                        INSERT INTO alternative_food (
                            id,
                            ingredient_id,
                            name,
                            content_per_100g,
                            created_at,
                            updated_at
                        )
                        VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                id,
                ingredientId,
                name,
                contentPer100g
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

    private void insertContraindication(Long id, String type, String conditionName) {
        jdbcTemplate.update("""
                        INSERT INTO contraindication (id, type, condition_name, created_at, updated_at)
                        VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                id,
                type,
                conditionName
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

    private void insertMemberProduct(Long id, Long memberId, Long productId, String deletedAt) {
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
                        VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                id,
                memberId,
                productId,
                deletedAt
        );
    }

    private String bearerToken() {
        return "Bearer " + accessToken;
    }
}
