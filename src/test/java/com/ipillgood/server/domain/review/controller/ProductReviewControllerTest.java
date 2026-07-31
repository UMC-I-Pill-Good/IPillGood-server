package com.ipillgood.server.domain.review.controller;

import com.ipillgood.server.global.security.jwt.JwtProvider;
import com.jayway.jsonpath.JsonPath;
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

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductReviewControllerTest {

    private static final String REVIEWS_URL = "/api/v1/reviews/";
    private static final long PRODUCT_ID = 100L;
    private static final long EMPTY_PRODUCT_ID = 101L;
    private static final long DELETED_PRODUCT_ID = 102L;
    private static final long OTHER_PRODUCT_ID = 103L;

    // 설문 응답 회원 / 설문 미응답 회원 / 설문 id 순서와 작성 시각 순서가 어긋난 회원
    private static final long MEMBER_ID = 1L;
    private static final long NO_SURVEY_MEMBER_ID = 2L;
    private static final long UNORDERED_SURVEY_MEMBER_ID = 3L;

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

        insertMember(MEMBER_ID, "필굿");
        insertMember(NO_SURVEY_MEMBER_ID, "설문안한회원");
        insertMember(UNORDERED_SURVEY_MEMBER_ID, "순서뒤집힌회원");

        // MEMBER_ID는 재설문까지 마쳤고, 작성 시각이 더 늦은 응답(2번)의 출생연도/성별이 반영되어야 한다
        insertSurveyResponse(1L, MEMBER_ID, "INITIAL", (short) 1990, "MALE",
                "2026-07-01 00:00:00", "2026-07-01 00:00:00");
        insertSurveyResponse(2L, MEMBER_ID, "REVISION", (short) 2000, "FEMALE",
                "2026-07-10 00:00:00", "2026-07-10 00:00:00");
        // 완료되지 않은 설문은 무시되어야 한다
        insertSurveyResponse(3L, NO_SURVEY_MEMBER_ID, "INITIAL", (short) 1985, "MALE",
                null, "2026-07-05 00:00:00");
        // id 순서와 작성 시각 순서가 어긋난 경우: 최신 판정은 id가 아니라 작성 시각을 따라야 한다
        insertSurveyResponse(4L, UNORDERED_SURVEY_MEMBER_ID, "INITIAL", (short) 2010, "MALE",
                "2026-07-20 00:00:00", "2026-07-20 00:00:00");
        insertSurveyResponse(5L, UNORDERED_SURVEY_MEMBER_ID, "REVISION", (short) 1970, "FEMALE",
                "2026-07-02 00:00:00", "2026-07-02 00:00:00");

        insertProduct(PRODUCT_ID, "비타민 D 1000IU", null);
        insertProduct(EMPTY_PRODUCT_ID, "후기 없는 상품", null);
        insertProduct(DELETED_PRODUCT_ID, "삭제된 상품", "2026-07-01 00:00:00");
        insertProduct(OTHER_PRODUCT_ID, "마그네슘 파워", null);

        // 작성 일시 오름차순: 1 -> 2 -> 3, 도움됨 수: 3(5) > 1(2) > 2(0)
        // 삭제된 4번을 제외한 평점 평균은 (4+5+5)/3 = 4.666... 이라 반올림 결과가 4.7이어야 한다
        insertProductReview(1L, PRODUCT_ID, MEMBER_ID, 4, 2, "2026-07-10 10:00:00", null);
        insertProductReview(2L, PRODUCT_ID, NO_SURVEY_MEMBER_ID, 5, 0, "2026-07-11 10:00:00", null);
        insertProductReview(3L, PRODUCT_ID, MEMBER_ID, 5, 5, "2026-07-12 10:00:00", null);
        insertProductReview(4L, PRODUCT_ID, MEMBER_ID, 1, 0, "2026-07-13 10:00:00", "2026-07-14 10:00:00");
        insertProductReview(5L, OTHER_PRODUCT_ID, UNORDERED_SURVEY_MEMBER_ID, 4, 0, "2026-07-15 10:00:00", null);

        insertProductReviewImage(1L, 1L, "reviews/a.jpg", (short) 1);
        insertProductReviewImage(2L, 1L, "reviews/b.jpg", (short) 2);

        insertProductReviewHelpful(1L, 3L, MEMBER_ID);

        accessToken = jwtProvider.createAccessToken(MEMBER_ID, "USER", "test-session");
    }

    @Test
    @DisplayName("인증 없이 후기 목록을 조회하면 401을 반환한다")
    void getReviews_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get(REVIEWS_URL + PRODUCT_ID))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("정렬을 생략하면 최신순으로 후기 목록과 요약 정보를 반환한다")
    void getReviews_withoutSort_returnsLatestFirst() throws Exception {
        mockMvc.perform(get(REVIEWS_URL + PRODUCT_ID)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("REVIEW200_2"))
                .andExpect(jsonPath("$.result.productId").value(100))
                .andExpect(jsonPath("$.result.sort").value("LATEST"))
                .andExpect(jsonPath("$.result.size").value(20))
                // 삭제된 후기(4번)는 집계와 목록 모두에서 제외된다
                .andExpect(jsonPath("$.result.reviewCount").value(3))
                // 평균 별점은 소수 둘째 자리에서 반올림한다 (4.666... -> 4.7)
                .andExpect(jsonPath("$.result.ratingAverage").value(4.7))
                .andExpect(jsonPath("$.result.hasNext").value(false))
                .andExpect(jsonPath("$.result.nextCursor").doesNotExist())
                .andExpect(jsonPath("$.result.reviews[*].reviewId").value(contains(3, 2, 1)));
    }

    @Test
    @DisplayName("좋아요순으로 조회하면 도움됨 수 내림차순으로 반환한다")
    void getReviews_withLikeCountSort_returnsHelpfulCountDesc() throws Exception {
        mockMvc.perform(get(REVIEWS_URL + PRODUCT_ID)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .param("sort", "LIKE_COUNT_DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.sort").value("LIKE_COUNT_DESC"))
                .andExpect(jsonPath("$.result.reviews[*].reviewId").value(contains(3, 1, 2)))
                .andExpect(jsonPath("$.result.reviews[*].helpfulCount").value(contains(5, 2, 0)));
    }

    @Test
    @DisplayName("설문에 응답한 작성자는 최신 설문 기준 연령대와 성별을 반환한다")
    void getReviews_withSurveyRespondent_returnsLatestSurveyProfile() throws Exception {
        mockMvc.perform(get(REVIEWS_URL + PRODUCT_ID)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                // 3번 후기 작성자 = MEMBER_ID, 최신 설문의 2000년생/FEMALE이 반영된다
                .andExpect(jsonPath("$.result.reviews[0].reviewId").value(3))
                .andExpect(jsonPath("$.result.reviews[0].ageGroup").value("TWENTIES"))
                .andExpect(jsonPath("$.result.reviews[0].gender").value("FEMALE"));
    }

    @Test
    @DisplayName("설문 id 순서와 작성 시각 순서가 어긋나면 작성 시각이 가장 늦은 설문을 따른다")
    void getReviews_withUnorderedSurveyIds_followsLatestCreatedAt() throws Exception {
        mockMvc.perform(get(REVIEWS_URL + OTHER_PRODUCT_ID)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                // id는 5번이 크지만 작성 시각은 4번(2026-07-20)이 더 늦다 -> 2010년생/MALE이 반영된다
                .andExpect(jsonPath("$.result.reviews[0].reviewId").value(5))
                .andExpect(jsonPath("$.result.reviews[0].ageGroup").value("TEENS"))
                .andExpect(jsonPath("$.result.reviews[0].gender").value("MALE"));
    }

    @Test
    @DisplayName("설문에 응답하지 않은 작성자는 연령대와 성별을 null로 반환한다")
    void getReviews_withoutSurveyRespondent_returnsNullDemographics() throws Exception {
        mockMvc.perform(get(REVIEWS_URL + PRODUCT_ID)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                // 2번 후기 작성자 = NO_SURVEY_MEMBER_ID (완료되지 않은 설문만 보유)
                .andExpect(jsonPath("$.result.reviews[1].reviewId").value(2))
                .andExpect(jsonPath("$.result.reviews[1].ageGroup").doesNotExist())
                .andExpect(jsonPath("$.result.reviews[1].gender").doesNotExist());
    }

    @Test
    @DisplayName("본인 여부와 도움됨 여부, 첨부 이미지를 함께 반환한다")
    void getReviews_returnsMineAndHelpedByMeAndImages() throws Exception {
        mockMvc.perform(get(REVIEWS_URL + PRODUCT_ID)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.reviews[*].mine").value(contains(true, false, true)))
                .andExpect(jsonPath("$.result.reviews[*].helpedByMe").value(contains(true, false, false)))
                // 1번 후기의 첨부 이미지는 display_order 순으로 내려간다
                .andExpect(jsonPath("$.result.reviews[2].reviewImageUrls.length()").value(2))
                .andExpect(jsonPath("$.result.reviews[0].reviewImageUrls.length()").value(0));
    }

    @Test
    @DisplayName("최신순 커서로 다음 페이지를 이어서 조회한다")
    void getReviews_withLatestCursor_returnsNextPage() throws Exception {
        String firstPage = mockMvc.perform(get(REVIEWS_URL + PRODUCT_ID)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.hasNext").value(true))
                .andExpect(jsonPath("$.result.reviews[*].reviewId").value(contains(3, 2)))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        String nextCursor = JsonPath.read(firstPage, "$.result.nextCursor");

        mockMvc.perform(get(REVIEWS_URL + PRODUCT_ID)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .param("size", "2")
                        .param("cursor", nextCursor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.hasNext").value(false))
                .andExpect(jsonPath("$.result.nextCursor").doesNotExist())
                .andExpect(jsonPath("$.result.reviews[*].reviewId").value(contains(1)));
    }

    @Test
    @DisplayName("좋아요순 커서로 다음 페이지를 이어서 조회한다")
    void getReviews_withLikeCountCursor_returnsNextPage() throws Exception {
        String firstPage = mockMvc.perform(get(REVIEWS_URL + PRODUCT_ID)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .param("sort", "LIKE_COUNT_DESC")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.hasNext").value(true))
                .andExpect(jsonPath("$.result.reviews[*].reviewId").value(contains(3, 1)))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        String nextCursor = JsonPath.read(firstPage, "$.result.nextCursor");

        mockMvc.perform(get(REVIEWS_URL + PRODUCT_ID)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .param("sort", "LIKE_COUNT_DESC")
                        .param("size", "2")
                        .param("cursor", nextCursor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.hasNext").value(false))
                .andExpect(jsonPath("$.result.reviews[*].reviewId").value(contains(2)));
    }

    @Test
    @DisplayName("후기가 없는 상품은 빈 목록과 0건 요약을 반환한다")
    void getReviews_withoutReviews_returnsEmptyList() throws Exception {
        mockMvc.perform(get(REVIEWS_URL + EMPTY_PRODUCT_ID)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.reviewCount").value(0))
                .andExpect(jsonPath("$.result.hasNext").value(false))
                .andExpect(jsonPath("$.result.reviews.length()").value(0));
    }

    @Test
    @DisplayName("삭제된 상품의 후기를 조회하면 404를 반환한다")
    void getReviews_withDeletedProduct_returnsNotFound() throws Exception {
        mockMvc.perform(get(REVIEWS_URL + DELETED_PRODUCT_ID)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-a-valid-cursor", "abc|3", "3", "0|2026-07-12T10:00:00", "3|어제"})
    @DisplayName("형식이 잘못된 커서를 보내면 400(COMMON400_4)을 반환한다")
    void getReviews_withMalformedCursor_returnsBadRequest(String cursor) throws Exception {
        mockMvc.perform(get(REVIEWS_URL + PRODUCT_ID)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .param("cursor", cursor))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON400_4"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "101", "abc"})
    @DisplayName("허용 범위를 벗어난 페이지 크기를 보내면 400(COMMON400_1)을 반환한다")
    void getReviews_withInvalidSize_returnsBadRequest(String size) throws Exception {
        mockMvc.perform(get(REVIEWS_URL + PRODUCT_ID)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .param("size", size))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON400_1"));
    }

    // ---------- fixture ----------

    private void clearDatabase() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.update("DELETE FROM product_review_helpful");
        jdbcTemplate.update("DELETE FROM product_review_image");
        jdbcTemplate.update("DELETE FROM product_review");
        jdbcTemplate.update("DELETE FROM survey_response");
        jdbcTemplate.update("DELETE FROM product");
        jdbcTemplate.update("DELETE FROM member");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }

    private void insertMember(Long id, String nickname) {
        jdbcTemplate.update("""
                        INSERT INTO member (
                            id, nickname, username, email, password, role, status,
                            profile_image_key, onboarding_completed_at, last_login_at, created_at, updated_at
                        )
                        VALUES (?, ?, ?, ?, NULL, 'USER', 'ACTIVE',
                                'profileImage/profile1.png', '2026-07-01 00:00:00', NULL,
                                CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                id,
                nickname,
                "user" + id,
                "user" + id + "@example.com"
        );
    }

    private void insertSurveyResponse(
            Long id, Long memberId, String submissionType, Short birthYear, String gender,
            String completedAt, String createdAt
    ) {
        jdbcTemplate.update("""
                        INSERT INTO survey_response (
                            id, member_id, submission_type, birth_year, gender, job_type,
                            menstrual_cycle_days, last_period_started_on,
                            smoking_status, drinking_status, diet_type, exercise_frequency, pregnant,
                            underlying_disease_none, medication_none, allergy_none, current_ingredient_none,
                            completed_at, created_at, updated_at
                        )
                        VALUES (?, ?, ?, ?, ?, 'OFFICE', NULL, NULL,
                                'NONE', 'NONE', 'MIXED', 'RARELY', NULL,
                                TRUE, TRUE, TRUE, TRUE, ?, ?, CURRENT_TIMESTAMP)
                        """,
                id,
                memberId,
                submissionType,
                birthYear,
                gender,
                completedAt,
                createdAt
        );
    }

    private void insertProduct(Long id, String name, String deletedAt) {
        jdbcTemplate.update("""
                        INSERT INTO product (
                            id, name, brand, description, purchase_url, mfds_certified,
                            deleted_at, created_at, updated_at
                        )
                        VALUES (?, ?, '브랜드A', '상품 설명', 'https://example.com', TRUE, ?,
                                CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                id,
                name,
                deletedAt
        );
    }

    private void insertProductReview(
            Long id, Long productId, Long memberId, int rating, int helpfulCount, String createdAt, String deletedAt
    ) {
        jdbcTemplate.update("""
                        INSERT INTO product_review (
                            id, product_id, member_id, reviewer_age_group, reviewer_gender,
                            rating, content, helpful_count, deleted_at, created_at, updated_at
                        )
                        VALUES (?, ?, ?, 'TWENTIES', 'FEMALE', ?, '좋아요', ?, ?, ?, CURRENT_TIMESTAMP)
                        """,
                id,
                productId,
                memberId,
                rating,
                helpfulCount,
                deletedAt,
                createdAt
        );
    }

    private void insertProductReviewImage(Long id, Long reviewId, String imageKey, Short displayOrder) {
        jdbcTemplate.update("""
                        INSERT INTO product_review_image (
                            id, review_id, image_key, display_order, created_at, updated_at
                        )
                        VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                id,
                reviewId,
                imageKey,
                displayOrder
        );
    }

    private void insertProductReviewHelpful(Long id, Long reviewId, Long memberId) {
        jdbcTemplate.update("""
                        INSERT INTO product_review_helpful (
                            id, review_id, member_id, created_at, updated_at
                        )
                        VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                id,
                reviewId,
                memberId
        );
    }

    private String bearerToken(String token) {
        return "Bearer " + token;
    }
}
