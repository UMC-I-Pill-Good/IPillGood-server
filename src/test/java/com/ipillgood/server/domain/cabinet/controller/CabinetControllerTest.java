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

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CabinetControllerTest {

    private static final String CABINET_PRODUCTS_URL = "/api/v1/cabinet/products";
    private static final String CABINET_PRODUCT_CANDIDATES_URL = "/api/v1/cabinet/product-candidates";
    private static final String CABINET_REVIEW_PROMPTS_URL = "/api/v1/cabinet/review-prompts";
    private static final String CABINET_REVIEW_PROMPTS_DUE_URL = CABINET_REVIEW_PROMPTS_URL + "/due";
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
    private String defaultActiveProductStartedOn;

    @BeforeEach
    void setUp() {
        clearDatabase();

        insertMember(MEMBER_ID, "필굿", "2026-07-01 00:00:00");
        insertMember(OTHER_MEMBER_ID, "다른회원", "2026-07-01 00:00:00");
        insertMember(EMPTY_MEMBER_ID, "빈회원", "2026-07-01 00:00:00");
        insertMember(ONBOARDING_INCOMPLETE_MEMBER_ID, "미완료", null);
        defaultActiveProductStartedOn = LocalDate.now().minusDays(20).toString();

        insertIngredient(1L, "종합비타민", "ingredients/1.png");
        insertIngredient(2L, "비타민 D", "ingredients/2.png");
        insertIngredient(3L, "비타민 C", "ingredients/3.png");
        insertIngredient(4L, "마그네슘", "ingredients/4.png");
        insertIngredient(5L, "아연", "ingredients/5.png");

        insertEffectKeyword(1L, 2L, "뼈 건강");
        insertEffectKeyword(2L, 2L, "면역");
        insertEffectKeyword(3L, 1L, "기본 영양");
        insertEffectKeyword(4L, 3L, "항산화");

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

        insertProductReview(1L, 101L, MEMBER_ID);

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
    @DisplayName("인증 없이 캐비닛 영양제를 삭제하면 401을 반환한다")
    void deleteProducts_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(delete(CABINET_PRODUCTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "memberProductIds": [1]
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("인증 없이 캐비닛 개별 영양제를 조회하면 401을 반환한다")
    void getProduct_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get(CABINET_PRODUCTS_URL + "/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("인증 없이 후기 작성 유도 대상을 조회하면 401을 반환한다")
    void getDueReviewPrompts_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get(CABINET_REVIEW_PROMPTS_DUE_URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("인증 없이 후기 작성 유도 배너 닫힘을 기록하면 401을 반환한다")
    void dismissReviewPrompt_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(patch(reviewPromptDismissedUrl(10L)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("인증 없이 캐비닛 추가 후보를 검색하면 401을 반환한다")
    void getProductCandidates_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get(CABINET_PRODUCT_CANDIDATES_URL))
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
    @DisplayName("온보딩을 완료하지 않은 회원은 캐비닛 영양제를 삭제할 수 없다")
    void deleteProducts_withoutCompletedOnboarding_returnsForbidden() throws Exception {
        mockMvc.perform(delete(CABINET_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(onboardingIncompleteAccessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "memberProductIds": [1]
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("CABINET403_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("온보딩을 완료하지 않은 회원은 캐비닛 개별 영양제를 조회할 수 없다")
    void getProduct_withoutCompletedOnboarding_returnsForbidden() throws Exception {
        mockMvc.perform(get(CABINET_PRODUCTS_URL + "/1")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(onboardingIncompleteAccessToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("CABINET403_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("온보딩을 완료하지 않은 회원은 후기 작성 유도 대상을 조회할 수 없다")
    void getDueReviewPrompts_withoutCompletedOnboarding_returnsForbidden() throws Exception {
        mockMvc.perform(get(CABINET_REVIEW_PROMPTS_DUE_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(onboardingIncompleteAccessToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("CABINET403_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("온보딩을 완료하지 않은 회원은 후기 작성 유도 배너 닫힘을 기록할 수 없다")
    void dismissReviewPrompt_withoutCompletedOnboarding_returnsForbidden() throws Exception {
        mockMvc.perform(patch(reviewPromptDismissedUrl(10L))
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(onboardingIncompleteAccessToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("CABINET403_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("온보딩을 완료하지 않은 회원은 캐비닛 추가 후보를 검색할 수 없다")
    void getProductCandidates_withoutCompletedOnboarding_returnsForbidden() throws Exception {
        mockMvc.perform(get(CABINET_PRODUCT_CANDIDATES_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(onboardingIncompleteAccessToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("CABINET403_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("캐비닛 추가 후보를 기본 정렬로 조회하고 보유 여부와 태그를 반환한다")
    void getProductCandidates_withDefaultCondition_returnsCandidates() throws Exception {
        mockMvc.perform(get(CABINET_PRODUCT_CANDIDATES_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.message").value("요청이 성공적으로 처리되었습니다."))
                .andExpect(jsonPath("$.result.keyword").doesNotExist())
                .andExpect(jsonPath("$.result.sort").value("REVIEW_COUNT_DESC"))
                .andExpect(jsonPath("$.result.page").value(0))
                .andExpect(jsonPath("$.result.size").value(20))
                .andExpect(jsonPath("$.result.totalCount").value(4))
                .andExpect(jsonPath("$.result.hasNext").value(false))
                .andExpect(jsonPath("$.result.products.length()").value(4))
                .andExpect(jsonPath("$.result.products[*].productId", contains(101, 104, 100, 102)))
                .andExpect(jsonPath("$.result.products[0].brand").value("테스트브랜드"))
                .andExpect(jsonPath("$.result.products[0].productName").value("멀티비타민 제품"))
                .andExpect(jsonPath("$.result.products[0].thumbnailImageUrl")
                        .value(matchesPattern("https://ipillgood-bucket\\.s3\\.ap-northeast-2\\.amazonaws\\.com/ingredients/other[1-4]\\.png")))
                .andExpect(jsonPath("$.result.products[0].averageRating").value(5.0))
                .andExpect(jsonPath("$.result.products[0].reviewCount").value(1))
                .andExpect(jsonPath("$.result.products[0].ingredientTags", contains("기본 영양", "항산화")))
                .andExpect(jsonPath("$.result.products[0].isOwned").value(true))
                .andExpect(jsonPath("$.result.products[0].isSelectable").value(false))
                .andExpect(jsonPath("$.result.products[1].productId").value(104))
                .andExpect(jsonPath("$.result.products[1].isOwned").value(false))
                .andExpect(jsonPath("$.result.products[1].isSelectable").value(true))
                .andExpect(jsonPath("$.result.products[2].productId").value(100))
                .andExpect(jsonPath("$.result.products[2].thumbnailImageUrl")
                        .value("https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/ingredients/2.png"))
                .andExpect(jsonPath("$.result.products[2].ingredientTags", contains("뼈 건강", "면역")))
                .andExpect(jsonPath("$.result.products[2].isOwned").value(true))
                .andExpect(jsonPath("$.result.products[2].isSelectable").value(false))
                .andExpect(jsonPath("$.result.products[3].productId").value(102))
                .andExpect(jsonPath("$.result.products[3].averageRating").doesNotExist())
                .andExpect(jsonPath("$.result.products[3].reviewCount").value(0))
                .andExpect(jsonPath("$.result.products[3].isOwned").value(false))
                .andExpect(jsonPath("$.result.products[3].isSelectable").value(true));
    }

    @Test
    @DisplayName("캐비닛 추가 후보를 브랜드명, 상품명, 성분명으로 부분 일치 검색한다")
    void getProductCandidates_withKeyword_searchesBrandProductNameAndIngredientName() throws Exception {
        insertProduct(105L, "Daily TEST Capsule", "CaseBrand", null);
        insertProductIngredient(7L, 105L, 2L);

        mockMvc.perform(get(CABINET_PRODUCT_CANDIDATES_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .param("keyword", "casebrand"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.keyword").value("casebrand"))
                .andExpect(jsonPath("$.result.totalCount").value(1))
                .andExpect(jsonPath("$.result.products[*].productId", contains(105)));

        mockMvc.perform(get(CABINET_PRODUCT_CANDIDATES_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .param("keyword", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.totalCount").value(1))
                .andExpect(jsonPath("$.result.products[*].productId", contains(105)));

        mockMvc.perform(get(CABINET_PRODUCT_CANDIDATES_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .param("keyword", "마그네슘"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.totalCount").value(1))
                .andExpect(jsonPath("$.result.products[*].productId", contains(102)));

        mockMvc.perform(get(CABINET_PRODUCT_CANDIDATES_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .param("keyword", "아연"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.totalCount").value(0))
                .andExpect(jsonPath("$.result.products.length()").value(0));
    }

    @Test
    @DisplayName("캐비닛 추가 후보를 후기 수와 평점 기준으로 정렬한다")
    void getProductCandidates_withSort_returnsOrderedCandidates() throws Exception {
        insertProduct(200L, "정렬 A", "정렬브랜드", null);
        insertProduct(201L, "정렬 B", "정렬브랜드", null);
        insertProduct(202L, "정렬 C", "정렬브랜드", null);
        insertProductIngredient(2001L, 200L, 2L);
        insertProductIngredient(2002L, 201L, 3L);
        insertProductIngredient(2003L, 202L, 4L);
        insertProductReview(2001L, 200L, MEMBER_ID, 3, null);
        insertProductReview(2002L, 200L, OTHER_MEMBER_ID, 3, null);
        insertProductReview(2003L, 201L, MEMBER_ID, 5, null);

        mockMvc.perform(get(CABINET_PRODUCT_CANDIDATES_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .param("keyword", "정렬")
                        .param("sort", "REVIEW_COUNT_DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.sort").value("REVIEW_COUNT_DESC"))
                .andExpect(jsonPath("$.result.products[*].productId", contains(200, 201, 202)))
                .andExpect(jsonPath("$.result.products[*].reviewCount", contains(2, 1, 0)));

        mockMvc.perform(get(CABINET_PRODUCT_CANDIDATES_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .param("keyword", "정렬")
                        .param("sort", "RATING_DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.sort").value("RATING_DESC"))
                .andExpect(jsonPath("$.result.products[*].productId", contains(201, 200, 202)))
                .andExpect(jsonPath("$.result.products[0].averageRating").value(5.0))
                .andExpect(jsonPath("$.result.products[1].averageRating").value(3.0))
                .andExpect(jsonPath("$.result.products[2].averageRating").doesNotExist());
    }

    @Test
    @DisplayName("캐비닛 추가 후보 검색은 페이지 정보를 반환한다")
    void getProductCandidates_withPaging_returnsPageInfo() throws Exception {
        mockMvc.perform(get(CABINET_PRODUCT_CANDIDATES_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.page").value(0))
                .andExpect(jsonPath("$.result.size").value(2))
                .andExpect(jsonPath("$.result.totalCount").value(4))
                .andExpect(jsonPath("$.result.hasNext").value(true))
                .andExpect(jsonPath("$.result.products.length()").value(2))
                .andExpect(jsonPath("$.result.products[*].productId", contains(101, 104)));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "?sort=UNKNOWN",
            "?sort=rating_desc",
            "?page=-1",
            "?page=abc",
            "?size=0",
            "?size=101",
            "?size=abc"
    })
    @DisplayName("캐비닛 추가 후보 검색 조건이 올바르지 않으면 400을 반환한다")
    void getProductCandidates_withInvalidCondition_returnsBadRequest(String queryString) throws Exception {
        mockMvc.perform(get(CABINET_PRODUCT_CANDIDATES_URL + queryString)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("CABINET400_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("캐비닛 추가 후보 검색어가 100자를 초과하면 400을 반환한다")
    void getProductCandidates_withTooLongKeyword_returnsBadRequest() throws Exception {
        mockMvc.perform(get(CABINET_PRODUCT_CANDIDATES_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .param("keyword", "a".repeat(101)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("CABINET400_1"))
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
    @DisplayName("후기 작성 유도 대상만 정렬해서 조회한다")
    void getDueReviewPrompts_returnsEligiblePromptsOrdered() throws Exception {
        LocalDate currentDate = LocalDate.now();
        String olderStartedOn = currentDate.minusDays(31).toString();
        String exactDueStartedOn = currentDate.minusDays(30).toString();
        String notDueStartedOn = currentDate.minusDays(29).toString();
        String currentDateText = currentDate.toString();

        insertProduct(200L, "A Older Product", "테스트브랜드", null);
        insertProduct(201L, "B Deleted Review Product", "테스트브랜드", null);
        insertProduct(202L, "C Other Review Product", "테스트브랜드", null);
        insertProduct(203L, "Z Same Product", "테스트브랜드", null);
        insertProduct(204L, "Z Same Product", "테스트브랜드", null);
        insertProduct(205L, "Not Due Product", "테스트브랜드", null);
        insertProduct(206L, "Stopped Product", "테스트브랜드", null);
        insertProduct(207L, "Deleted Cabinet Product", "테스트브랜드", null);
        insertProduct(208L, "Deleted Product", "테스트브랜드", "2026-07-01 00:00:00");
        insertProduct(209L, "Dismissed Product", "테스트브랜드", null);
        insertProduct(210L, "My Review Product", "테스트브랜드", null);

        insertMemberProduct(20L, MEMBER_ID, 200L, "2026-06-01 10:00:00", null);
        insertMemberProduct(21L, MEMBER_ID, 201L, "2026-06-01 10:00:00", null);
        insertMemberProduct(22L, MEMBER_ID, 202L, "2026-06-01 10:00:00", null);
        insertMemberProduct(23L, MEMBER_ID, 203L, "2026-06-01 10:00:00", null);
        insertMemberProduct(24L, MEMBER_ID, 204L, "2026-06-01 10:00:00", null);
        insertMemberProduct(25L, MEMBER_ID, 205L, "2026-06-01 10:00:00", null);
        insertMemberProduct(26L, MEMBER_ID, 206L, "2026-06-01 10:00:00", null);
        insertMemberProduct(27L, MEMBER_ID, 207L, "2026-06-01 10:00:00", "2026-07-01 00:00:00");
        insertMemberProduct(28L, MEMBER_ID, 208L, "2026-06-01 10:00:00", null);
        insertMemberProduct(29L, MEMBER_ID, 209L, "2026-06-01 10:00:00", null);
        insertMemberProduct(30L, MEMBER_ID, 210L, "2026-06-01 10:00:00", null);

        insertMemberActiveProduct(20L, 20L, MEMBER_ID, olderStartedOn, null, null);
        insertMemberActiveProduct(21L, 21L, MEMBER_ID, olderStartedOn, null, null);
        insertMemberActiveProduct(22L, 22L, MEMBER_ID, olderStartedOn, null, null);
        insertMemberActiveProduct(23L, 23L, MEMBER_ID, exactDueStartedOn, null, null);
        insertMemberActiveProduct(24L, 24L, MEMBER_ID, exactDueStartedOn, null, null);
        insertMemberActiveProduct(25L, 25L, MEMBER_ID, notDueStartedOn, null, null);
        insertMemberActiveProduct(26L, 26L, MEMBER_ID, olderStartedOn, currentDateText, null);
        insertMemberActiveProduct(27L, 27L, MEMBER_ID, olderStartedOn, null, null);
        insertMemberActiveProduct(28L, 28L, MEMBER_ID, olderStartedOn, null, null);
        insertMemberActiveProduct(29L, 29L, MEMBER_ID, olderStartedOn, null, "2026-07-20 09:00:00");
        insertMemberActiveProduct(30L, 30L, MEMBER_ID, olderStartedOn, null, null);

        insertProductReview(3001L, 201L, MEMBER_ID, 5, "2026-07-20 00:00:00");
        insertProductReview(3002L, 202L, OTHER_MEMBER_ID, 5, null);
        insertProductReview(3003L, 210L, MEMBER_ID, 5, null);

        mockMvc.perform(get(CABINET_REVIEW_PROMPTS_DUE_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.message").value("요청이 성공적으로 처리되었습니다."))
                .andExpect(jsonPath("$.result.duePrompts.length()").value(5))
                .andExpect(jsonPath("$.result.duePrompts[*].activeProductId", contains(20, 21, 22, 23, 24)))
                .andExpect(jsonPath("$.result.duePrompts[*].productId", contains(200, 201, 202, 203, 204)))
                .andExpect(jsonPath("$.result.duePrompts[*].productName", contains(
                        "A Older Product",
                        "B Deleted Review Product",
                        "C Other Review Product",
                        "Z Same Product",
                        "Z Same Product"
                )));
    }

    @Test
    @DisplayName("후기 작성 유도 대상이 없으면 빈 목록을 반환한다")
    void getDueReviewPrompts_withNoDuePrompts_returnsEmptyList() throws Exception {
        mockMvc.perform(get(CABINET_REVIEW_PROMPTS_DUE_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(emptyMemberAccessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.duePrompts.length()").value(0));
    }

    @Test
    @DisplayName("후기 작성 유도 배너 닫힘을 기록한다")
    void dismissReviewPrompt_withActiveProduct_recordsDismissedAt() throws Exception {
        assertNull(findReviewPromptDismissedAt(10L));

        mockMvc.perform(patch(reviewPromptDismissedUrl(10L))
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.message").value("요청이 성공적으로 처리되었습니다."))
                .andExpect(jsonPath("$.result.activeProductId").value(10))
                .andExpect(jsonPath("$.result.dismissedAt")
                        .value(matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?")));

        assertNotNull(findReviewPromptDismissedAt(10L));
    }

    @Test
    @DisplayName("이미 닫힘 기록된 후기 작성 유도 배너는 기존 닫힘 일시를 반환한다")
    void dismissReviewPrompt_withAlreadyDismissedProduct_returnsExistingDismissedAt() throws Exception {
        LocalDateTime existingDismissedAt = LocalDateTime.of(2026, 7, 20, 9, 0);
        jdbcTemplate.update(
                "UPDATE member_active_product SET review_prompt_dismissed_at = ? WHERE id = ?",
                existingDismissedAt,
                10L
        );

        mockMvc.perform(patch(reviewPromptDismissedUrl(10L))
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.result.activeProductId").value(10))
                .andExpect(jsonPath("$.result.dismissedAt").value("2026-07-20T09:00:00"));

        assertEquals(existingDismissedAt, findReviewPromptDismissedAt(10L));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1"})
    @DisplayName("activeProductId가 1 미만이면 400을 반환한다")
    void dismissReviewPrompt_withInvalidActiveProductId_returnsBadRequest(String activeProductId) throws Exception {
        mockMvc.perform(patch(reviewPromptDismissedUrl(activeProductId))
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("CABINET400_4"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("activeProductId가 숫자 형식이 아니면 공통 400을 반환한다")
    void dismissReviewPrompt_withNonNumericActiveProductId_returnsCommonBadRequest() throws Exception {
        mockMvc.perform(patch(reviewPromptDismissedUrl("abc"))
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON400_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("닫힘 기록 대상이 활성 섭취 중 상품이 아니면 404를 반환한다")
    void dismissReviewPrompt_withUnavailableActiveProduct_returnsNotFound() throws Exception {
        insertMemberActiveProduct(30L, 3L, MEMBER_ID, null);
        insertMemberActiveProduct(31L, 4L, MEMBER_ID, null);
        insertMemberActiveProduct(32L, 5L, OTHER_MEMBER_ID, null);

        for (long activeProductId : new long[]{999L, 11L, 30L, 31L, 32L}) {
            mockMvc.perform(patch(reviewPromptDismissedUrl(activeProductId))
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("CABINET404_3"))
                    .andExpect(jsonPath("$.result").doesNotExist());
        }
    }

    @Test
    @DisplayName("섭취 중인 캐비닛 개별 영양제를 조회한다")
    void getProduct_withActiveIntake_returnsProductDetail() throws Exception {
        mockMvc.perform(get(CABINET_PRODUCTS_URL + "/1")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.result.memberProductId").value(1))
                .andExpect(jsonPath("$.result.productId").value(100))
                .andExpect(jsonPath("$.result.brand").value("테스트브랜드"))
                .andExpect(jsonPath("$.result.productName").value("비타민 D 제품"))
                .andExpect(jsonPath("$.result.thumbnailImageUrl")
                        .value("https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/ingredients/2.png"))
                .andExpect(jsonPath("$.result.isActiveIntake").value(true))
                .andExpect(jsonPath("$.result.hasMyReview").value(false))
                .andExpect(jsonPath("$.result.ingredients.length()").value(1))
                .andExpect(jsonPath("$.result.ingredients[0].ingredientId").value(2))
                .andExpect(jsonPath("$.result.ingredients[0].name").value("비타민 D"))
                .andExpect(jsonPath("$.result.ingredients[0].imageUrl")
                        .value("https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/ingredients/2.png"))
                .andExpect(jsonPath("$.result.ingredients[0].description").value("성분 설명"))
                .andExpect(jsonPath("$.result.ingredients[0].effectTags", contains("뼈 건강", "면역")))
                .andExpect(jsonPath("$.result.activeProduct.activeProductId").value(10))
                .andExpect(jsonPath("$.result.activeProduct.startedOn").value(defaultActiveProductStartedOn))
                .andExpect(jsonPath("$.result.activeProduct.intakeDayCount").value(21))
                .andExpect(jsonPath("$.result.activeProduct.notificationEnabled").value(true))
                .andExpect(jsonPath("$.result.activeProduct.intakeTime").value("09:00"))
                .andExpect(jsonPath("$.result.activeProduct.frequency").value("EVERY_DAY"))
                .andExpect(jsonPath("$.result.activeProduct.frequencyLabel").value("매일"))
                .andExpect(jsonPath("$.result.activeProduct.frequencyIntervalDays").value(1))
                .andExpect(jsonPath("$.result.activeProduct.scheduleAnchorOn").value(defaultActiveProductStartedOn));
    }

    @Test
    @DisplayName("섭취 중이 아닌 캐비닛 개별 영양제는 activeProduct 없이 조회한다")
    void getProduct_withoutActiveIntake_returnsProductDetailWithNullActiveProduct() throws Exception {
        mockMvc.perform(get(CABINET_PRODUCTS_URL + "/2")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.memberProductId").value(2))
                .andExpect(jsonPath("$.result.productId").value(101))
                .andExpect(jsonPath("$.result.brand").value("테스트브랜드"))
                .andExpect(jsonPath("$.result.productName").value("멀티비타민 제품"))
                .andExpect(jsonPath("$.result.thumbnailImageUrl")
                        .value(matchesPattern("https://ipillgood-bucket\\.s3\\.ap-northeast-2\\.amazonaws\\.com/ingredients/other[1-4]\\.png")))
                .andExpect(jsonPath("$.result.isActiveIntake").value(false))
                .andExpect(jsonPath("$.result.hasMyReview").value(true))
                .andExpect(jsonPath("$.result.ingredients.length()").value(2))
                .andExpect(jsonPath("$.result.ingredients[*].ingredientId", contains(1, 3)))
                .andExpect(jsonPath("$.result.ingredients[0].effectTags", contains("기본 영양")))
                .andExpect(jsonPath("$.result.ingredients[1].effectTags", contains("항산화")))
                .andExpect(jsonPath("$.result.activeProduct").doesNotExist());
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

    @Test
    @DisplayName("캐비닛 영양제를 복수 삭제하고 활성 섭취 상품을 중단한다")
    void deleteProducts_withValidMemberProducts_returnsOk() throws Exception {
        LocalDate currentDate = LocalDate.now();
        insertMemberActiveProductScheduleHistory(10L, "EVERY_DAY", 1, defaultActiveProductStartedOn,
                defaultActiveProductStartedOn, null);
        insertIntakeDay(1L, MEMBER_ID, "2026-07-20");
        insertIntakeRecord(1L, 1L, 10L, 100L);
        int beforeIntakeDayCount = countIntakeDays();
        int beforeIntakeRecordCount = countIntakeRecords();

        mockMvc.perform(delete(CABINET_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "memberProductIds": [1, 2]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.message").value("요청이 성공적으로 처리되었습니다."))
                .andExpect(jsonPath("$.result.deletedCount").value(2))
                .andExpect(jsonPath("$.result.deletedProducts.length()").value(2))
                .andExpect(jsonPath("$.result.deletedProducts[*].memberProductId", contains(1, 2)))
                .andExpect(jsonPath("$.result.deletedProducts[*].productId", contains(100, 101)))
                .andExpect(jsonPath("$.result.deletedProducts[0].productName").value("비타민 D 제품"))
                .andExpect(jsonPath("$.result.deletedProducts[0].wasActiveIntake").value(true))
                .andExpect(jsonPath("$.result.deletedProducts[0].stoppedActiveProductId").value(10))
                .andExpect(jsonPath("$.result.deletedProducts[1].productName").value("멀티비타민 제품"))
                .andExpect(jsonPath("$.result.deletedProducts[1].wasActiveIntake").value(false))
                .andExpect(jsonPath("$.result.deletedProducts[1].stoppedActiveProductId").doesNotExist());

        assertEquals(1, countDeletedMemberProduct(1L));
        assertEquals(1, countDeletedMemberProduct(2L));
        assertEquals(0, countActiveMemberProducts(MEMBER_ID, 100L));
        assertEquals(0, countActiveMemberProducts(MEMBER_ID, 101L));
        assertEquals(currentDate, findStoppedOn(10L));
        assertEquals(0, countActiveScheduleHistories(10L));
        assertEquals(1, countClosedScheduleHistories(10L, "EVERY_DAY", currentDate));
        assertEquals(beforeIntakeDayCount, countIntakeDays());
        assertEquals(beforeIntakeRecordCount, countIntakeRecords());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}",
            "{\"memberProductIds\":null}",
            "{\"memberProductIds\":[]}",
            "{\"memberProductIds\":[1,1]}",
            "{\"memberProductIds\":[null]}",
            "{\"memberProductIds\":[0]}",
            "{\"memberProductIds\":[-1]}"
    })
    @DisplayName("캐비닛 삭제 상품 ID 목록이 올바르지 않으면 400을 반환하고 삭제하지 않는다")
    void deleteProducts_withInvalidMemberProductIds_returnsBadRequest(String requestBody) throws Exception {
        LocalDate currentDate = LocalDate.now();
        insertMemberActiveProductScheduleHistory(10L, "EVERY_DAY", 1, defaultActiveProductStartedOn,
                defaultActiveProductStartedOn, null);

        mockMvc.perform(delete(CABINET_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("CABINET400_3"))
                .andExpect(jsonPath("$.result").doesNotExist());

        assertEquals(0, countDeletedMemberProduct(1L));
        assertEquals(0, countStoppedActiveProduct(10L));
        assertEquals(1, countActiveScheduleHistories(10L));
        assertEquals(0, countClosedScheduleHistories(10L, "EVERY_DAY", currentDate));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"memberProductIds\":[1,999]}",
            "{\"memberProductIds\":[1,3]}",
            "{\"memberProductIds\":[1,4]}",
            "{\"memberProductIds\":[1,5]}"
    })
    @DisplayName("삭제할 수 없는 캐비닛 상품이 포함되면 404를 반환하고 일부만 삭제하지 않는다")
    void deleteProducts_withUnavailableMemberProduct_returnsNotFound(String requestBody) throws Exception {
        LocalDate currentDate = LocalDate.now();
        insertMemberActiveProductScheduleHistory(10L, "EVERY_DAY", 1, defaultActiveProductStartedOn,
                defaultActiveProductStartedOn, null);

        mockMvc.perform(delete(CABINET_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("CABINET404_2"))
                .andExpect(jsonPath("$.result").doesNotExist());

        assertEquals(0, countDeletedMemberProduct(1L));
        assertEquals(0, countStoppedActiveProduct(10L));
        assertEquals(1, countActiveScheduleHistories(10L));
        assertEquals(0, countClosedScheduleHistories(10L, "EVERY_DAY", currentDate));
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

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1"})
    @DisplayName("memberProductId가 1 미만이면 400을 반환한다")
    void getProduct_withInvalidMemberProductId_returnsBadRequest(String memberProductId) throws Exception {
        mockMvc.perform(get(CABINET_PRODUCTS_URL + "/" + memberProductId)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("CABINET400_3"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("memberProductId가 숫자 형식이 아니면 공통 400을 반환한다")
    void getProduct_withNonNumericMemberProductId_returnsCommonBadRequest() throws Exception {
        mockMvc.perform(get(CABINET_PRODUCTS_URL + "/abc")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON400_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @ParameterizedTest
    @ValueSource(longs = {999L, 3L, 4L, 5L})
    @DisplayName("조회 대상이 활성 캐비닛 보유 상품이 아니면 404를 반환한다")
    void getProduct_withUnavailableMemberProduct_returnsNotFound(long memberProductId) throws Exception {
        mockMvc.perform(get(CABINET_PRODUCTS_URL + "/" + memberProductId)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("CABINET404_2"))
                .andExpect(jsonPath("$.result").doesNotExist());
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

    private void insertEffectKeyword(Long id, Long ingredientId, String keyword) {
        jdbcTemplate.update("""
                        INSERT INTO effect_keyword (id, ingredient_id, keyword, created_at, updated_at)
                        VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                id,
                ingredientId,
                keyword
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
        insertMemberActiveProduct(id, memberProductId, memberId, defaultActiveProductStartedOn, stoppedOn, null);
    }

    private void insertMemberActiveProduct(
            Long id,
            Long memberProductId,
            Long memberId,
            String startedOn,
            String stoppedOn,
            String reviewPromptDismissedAt
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
                        VALUES (?, ?, ?, ?, ?, '09:00:00', 'EVERY_DAY', 1,
                                ?, true, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                id,
                memberProductId,
                memberId,
                startedOn,
                stoppedOn,
                startedOn,
                reviewPromptDismissedAt
        );
    }

    private void insertMemberActiveProductScheduleHistory(
            Long activeProductId,
            String frequency,
            int frequencyIntervalDays,
            String scheduleAnchorOn,
            String effectiveFrom,
            String effectiveTo
    ) {
        jdbcTemplate.update("""
                        INSERT INTO member_active_product_schedule_history (
                            member_active_product_id,
                            frequency,
                            frequency_interval_days,
                            schedule_anchor_on,
                            effective_from,
                            effective_to,
                            created_at,
                            updated_at
                        )
                        VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                activeProductId,
                frequency,
                frequencyIntervalDays,
                scheduleAnchorOn,
                effectiveFrom,
                effectiveTo
        );
    }

    private void insertIntakeDay(Long id, Long memberId, String intakeOn) {
        jdbcTemplate.update("""
                        INSERT INTO intake_day (
                            id,
                            member_id,
                            intake_on,
                            auto_popup_shown_at,
                            all_completed,
                            completed_at,
                            created_at,
                            updated_at
                        )
                        VALUES (?, ?, ?, NULL, true, '2026-07-20 09:10:00',
                                CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                id,
                memberId,
                intakeOn
        );
    }

    private void insertIntakeRecord(Long id, Long intakeDayId, Long memberActiveProductId, Long productId) {
        jdbcTemplate.update("""
                        INSERT INTO intake_record (
                            id,
                            intake_day_id,
                            member_active_product_id,
                            product_id,
                            scheduled,
                            taken,
                            taken_at,
                            created_at,
                            updated_at
                        )
                        VALUES (?, ?, ?, ?, true, true, '2026-07-20 09:10:00',
                                CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                id,
                intakeDayId,
                memberActiveProductId,
                productId
        );
    }

    private void insertProductReview(Long id, Long productId, Long memberId) {
        insertProductReview(id, productId, memberId, 5, null);
    }

    private void insertProductReview(Long id, Long productId, Long memberId, int rating, String deletedAt) {
        jdbcTemplate.update("""
                        INSERT INTO product_review (
                            id,
                            product_id,
                            member_id,
                            reviewer_age_group,
                            reviewer_gender,
                            rating,
                            content,
                            helpful_count,
                            deleted_at,
                            created_at,
                            updated_at
                        )
                        VALUES (?, ?, ?, 'TWENTIES', 'FEMALE', ?, '좋아요', 0, ?,
                                CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                id,
                productId,
                memberId,
                rating,
                deletedAt
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

    private int countDeletedMemberProduct(Long memberProductId) {
        Integer count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM member_product
                        WHERE id = ?
                          AND deleted_at IS NOT NULL
                        """,
                Integer.class,
                memberProductId
        );
        return count == null ? 0 : count;
    }

    private int countStoppedActiveProduct(Long activeProductId) {
        Integer count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM member_active_product
                        WHERE id = ?
                          AND stopped_on IS NOT NULL
                        """,
                Integer.class,
                activeProductId
        );
        return count == null ? 0 : count;
    }

    private LocalDate findStoppedOn(Long activeProductId) {
        return jdbcTemplate.queryForObject("""
                        SELECT stopped_on
                        FROM member_active_product
                        WHERE id = ?
                        """,
                LocalDate.class,
                activeProductId
        );
    }

    private int countActiveScheduleHistories(Long activeProductId) {
        Integer count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM member_active_product_schedule_history
                        WHERE member_active_product_id = ?
                          AND effective_to IS NULL
                        """,
                Integer.class,
                activeProductId
        );
        return count == null ? 0 : count;
    }

    private int countClosedScheduleHistories(Long activeProductId, String frequency, LocalDate effectiveTo) {
        Integer count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM member_active_product_schedule_history
                        WHERE member_active_product_id = ?
                          AND frequency = ?
                          AND effective_to = ?
                        """,
                Integer.class,
                activeProductId,
                frequency,
                effectiveTo
        );
        return count == null ? 0 : count;
    }

    private LocalDateTime findReviewPromptDismissedAt(Long activeProductId) {
        return jdbcTemplate.queryForObject("""
                        SELECT review_prompt_dismissed_at
                        FROM member_active_product
                        WHERE id = ?
                        """,
                LocalDateTime.class,
                activeProductId
        );
    }

    private int countIntakeDays() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM intake_day", Integer.class);
        return count == null ? 0 : count;
    }

    private int countIntakeRecords() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM intake_record", Integer.class);
        return count == null ? 0 : count;
    }

    private String bearerToken(String token) {
        return "Bearer " + token;
    }

    private String reviewPromptDismissedUrl(Object activeProductId) {
        return CABINET_REVIEW_PROMPTS_URL + "/" + activeProductId + "/dismissed";
    }
}
