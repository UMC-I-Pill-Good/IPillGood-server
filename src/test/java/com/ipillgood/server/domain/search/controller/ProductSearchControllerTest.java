package com.ipillgood.server.domain.search.controller;

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
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductSearchControllerTest {

    private static final String SEARCH_PRODUCTS_URL = "/api/v1/search/products";
    private static final String RECENT_KEYWORDS_URL = "/api/v1/search/recent-keywords";
    private static final long MEMBER_ID = 1L;
    private static final long OTHER_MEMBER_ID = 2L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtProvider jwtProvider;

    private String accessToken;
    private String otherAccessToken;

    @BeforeEach
    void setUp() {
        clearDatabase();

        insertMember(MEMBER_ID, "필굿", "2026-07-01 00:00:00");

        insertIngredient(1L, "비타민 D", "ingredients/1.png", "BOTH");
        insertIngredient(2L, "오메가 3", "ingredients/2.png", "MALE");
        insertIngredient(3L, "마그네슘", "ingredients/3.png", "FEMALE");
        insertIngredient(4L, "비타민 C", "ingredients/4.png", "BOTH");

        insertIngredientAgeGroup(1L, 1L, "TWENTIES");
        insertIngredientAgeGroup(2L, 2L, "TEENS");
        insertIngredientAgeGroup(3L, 4L, "ALL");

        insertHealthConcern(1L, "NERVOUS_SYSTEM", "TENSION");
        insertHealthConcern(2L, "MUSCULOSKELETAL", "BONE");
        insertHealthConcernIngredient(1L, 1L, 2L);
        insertHealthConcernIngredient(2L, 2L, 1L);

        insertProduct(100L, "비타민 D 1000IU", "브랜드A", true, null);
        insertProduct(101L, "멀티비타민", "브랜드B", false, null);
        insertProduct(102L, "오메가3 프리미엄", "브랜드C", true, null);
        insertProduct(103L, "삭제된 상품", "브랜드A", true, "2026-07-01 00:00:00");
        insertProduct(104L, "마그네슘 파워", "브랜드D", false, null);

        insertProductIngredient(1L, 100L, 1L);
        insertProductIngredient(2L, 101L, 1L);
        insertProductIngredient(3L, 101L, 4L);
        insertProductIngredient(4L, 102L, 2L);
        insertProductIngredient(5L, 103L, 1L);
        insertProductIngredient(6L, 104L, 3L);

        // product 100: 후기 3개, 평균 4.6667 -> 4.7 / product 101: 1개, 5.0 / product 102: 1개, 3.0 / product 104: 0개
        insertProductReview(1L, 100L, MEMBER_ID, 4, null);
        insertProductReview(2L, 100L, MEMBER_ID, 5, null);
        insertProductReview(3L, 100L, MEMBER_ID, 5, null);
        insertProductReview(4L, 101L, MEMBER_ID, 5, null);
        insertProductReview(5L, 102L, MEMBER_ID, 3, null);
        insertProductReview(6L, 102L, MEMBER_ID, 1, "2026-07-01 00:00:00");

        insertMember(OTHER_MEMBER_ID, "다른회원", "2026-07-01 00:00:00");
        // 최근 검색어: MEMBER_ID 2개(오메가3가 더 최신), OTHER_MEMBER_ID 1개
        insertMemberSearchKeyword(1L, MEMBER_ID, "비타민", "2026-07-20 10:00:00");
        insertMemberSearchKeyword(2L, MEMBER_ID, "오메가3", "2026-07-21 10:00:00");
        insertMemberSearchKeyword(3L, OTHER_MEMBER_ID, "마그네슘", "2026-07-20 10:00:00");
        restartMemberSearchKeywordIdentity(); // JPA IDENTITY 생성 id가 수동 삽입분과 충돌하지 않도록

        accessToken = jwtProvider.createAccessToken(MEMBER_ID, "USER", "test-session");
        otherAccessToken = jwtProvider.createAccessToken(OTHER_MEMBER_ID, "USER", "test-session");
    }

    @Test
    @DisplayName("인증 없이 영양제 상품 목록을 조회하면 401을 반환한다")
    void searchProducts_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get(SEARCH_PRODUCTS_URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("검색어 없이 조회하면 후기 많은 순 기본 목록을 반환한다")
    void searchProducts_withoutKeyword_returnsDefaultListOrderByReviewCount() throws Exception {
        mockMvc.perform(get(SEARCH_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SEARCH200_1"))
                .andExpect(jsonPath("$.result.keyword").doesNotExist())
                .andExpect(jsonPath("$.result.products[*].productId").value(contains(100, 102, 101, 104)))
                .andExpect(jsonPath("$.result.size").value(20))
                .andExpect(jsonPath("$.result.totalCount").value(4))
                .andExpect(jsonPath("$.result.hasNext").value(false))
                .andExpect(jsonPath("$.result.nextCursor").doesNotExist());
    }

    @Test
    @DisplayName("단일 성분 상품은 성분명 하나와 성분 이미지를, 복수 성분 상품은 성분명 전체와 기타 이미지를 반환한다")
    void searchProducts_returnsIngredientNamesAndThumbnailByIngredientCount() throws Exception {
        mockMvc.perform(get(SEARCH_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.products[0].productId").value(100))
                .andExpect(jsonPath("$.result.products[0].productName").value("비타민 D 1000IU"))
                .andExpect(jsonPath("$.result.products[0].brand").value("브랜드A"))
                .andExpect(jsonPath("$.result.products[0].ingredientNames").value(contains("비타민 D")))
                .andExpect(jsonPath("$.result.products[0].imageUrl")
                        .value(matchesPattern(".+/ingredients/1\\.png")))
                .andExpect(jsonPath("$.result.products[0].mfdsCertified").value(true))
                .andExpect(jsonPath("$.result.products[0].averageRating").value(4.7))
                .andExpect(jsonPath("$.result.products[0].reviewCount").value(3))
                .andExpect(jsonPath("$.result.products[2].productId").value(101))
                .andExpect(jsonPath("$.result.products[2].ingredientNames").value(contains("비타민 D", "비타민 C")))
                .andExpect(jsonPath("$.result.products[2].imageUrl")
                        .value(matchesPattern(".+/ingredients/other[1-4]\\.png")));
    }

    @Test
    @DisplayName("검색어는 브랜드명, 영양제명, 포함 성분명을 대상으로 적용한다")
    void searchProducts_withKeyword_matchesBrandNameAndIngredientName() throws Exception {
        mockMvc.perform(get(SEARCH_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .param("keyword", "비타민"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.keyword").value("비타민"))
                .andExpect(jsonPath("$.result.products[*].productId").value(contains(100, 101)))
                .andExpect(jsonPath("$.result.totalCount").value(2));

        mockMvc.perform(get(SEARCH_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .param("keyword", "오메가 3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.products[*].productId").value(contains(102)))
                .andExpect(jsonPath("$.result.totalCount").value(1));
    }

    @Test
    @DisplayName("RATING 정렬은 평균 별점 내림차순이며 별점 없는 상품은 맨 뒤로 배치한다")
    void searchProducts_withRatingSort_ordersByRatingWithNullsLast() throws Exception {
        mockMvc.perform(get(SEARCH_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .param("sort", "RATING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.products[*].productId").value(contains(101, 100, 102, 104)))
                .andExpect(jsonPath("$.result.products[0].averageRating").value(5.0))
                .andExpect(jsonPath("$.result.products[2].averageRating").value(3.0))
                .andExpect(jsonPath("$.result.products[3].averageRating").doesNotExist());
    }

    @Test
    @DisplayName("REVIEW_COUNT 정렬 커서로 다음 페이지를 이어서 조회한다")
    void searchProducts_withReviewCountCursor_returnsNextPage() throws Exception {
        String response = mockMvc.perform(get(SEARCH_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.products[*].productId").value(contains(100, 102)))
                .andExpect(jsonPath("$.result.size").value(2))
                .andExpect(jsonPath("$.result.totalCount").value(4))
                .andExpect(jsonPath("$.result.hasNext").value(true))
                .andExpect(jsonPath("$.result.nextCursor").exists())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        String nextCursor = JsonPath.read(response, "$.result.nextCursor");

        // 후기 수가 동일한(1개) 상품 102 -> 101 경계에서 상품 ID 내림차순 커서가 이어진다
        mockMvc.perform(get(SEARCH_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .param("size", "2")
                        .param("cursor", nextCursor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.products[*].productId").value(contains(101, 104)))
                .andExpect(jsonPath("$.result.totalCount").value(4))
                .andExpect(jsonPath("$.result.hasNext").value(false))
                .andExpect(jsonPath("$.result.nextCursor").doesNotExist());
    }

    @Test
    @DisplayName("RATING 정렬 커서로 별점 없는 상품까지 이어서 조회한다")
    void searchProducts_withRatingCursor_returnsNextPageIncludingNullRating() throws Exception {
        String response = mockMvc.perform(get(SEARCH_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .param("sort", "RATING")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.products[*].productId").value(contains(101, 100, 102)))
                .andExpect(jsonPath("$.result.hasNext").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        String nextCursor = JsonPath.read(response, "$.result.nextCursor");

        mockMvc.perform(get(SEARCH_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .param("sort", "RATING")
                        .param("size", "3")
                        .param("cursor", nextCursor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.products[*].productId").value(contains(104)))
                .andExpect(jsonPath("$.result.hasNext").value(false));
    }

    @Test
    @DisplayName("mfdsCertified=true면 식약처 인증 제품만 반환하고 false면 필터를 적용하지 않는다")
    void searchProducts_withMfdsCertifiedFilter_filtersOnlyWhenTrue() throws Exception {
        mockMvc.perform(get(SEARCH_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .param("mfdsCertified", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.products[*].productId").value(contains(100, 102)))
                .andExpect(jsonPath("$.result.totalCount").value(2));

        mockMvc.perform(get(SEARCH_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .param("mfdsCertified", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.totalCount").value(4));
    }

    @Test
    @DisplayName("연령대 필터는 해당 연령대 또는 전체 대상 성분을 포함한 상품을 반환한다")
    void searchProducts_withAgeGroupsFilter_matchesRecommendedAgeGroups() throws Exception {
        mockMvc.perform(get(SEARCH_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .param("ageGroups", "TWENTIES"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.products[*].productId").value(contains(100, 101)))
                .andExpect(jsonPath("$.result.totalCount").value(2));

        mockMvc.perform(get(SEARCH_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .param("ageGroups", "TEENS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.products[*].productId").value(contains(102, 101)));
    }

    @Test
    @DisplayName("성별 필터는 해당 성별 또는 전체 대상 성분을 포함한 상품을 반환한다")
    void searchProducts_withGenderFilter_matchesRecommendedGender() throws Exception {
        mockMvc.perform(get(SEARCH_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .param("gender", "FEMALE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.products[*].productId").value(contains(100, 101, 104)));

        mockMvc.perform(get(SEARCH_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .param("gender", "MALE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.products[*].productId").value(contains(100, 102, 101)));
    }

    @Test
    @DisplayName("건강 상태 대분류 필터는 해당 대분류와 연결된 성분을 포함한 상품을 반환한다")
    void searchProducts_withHealthConcernFilter_matchesMappedIngredients() throws Exception {
        mockMvc.perform(get(SEARCH_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .param("healthConcernMajorCategories", "NERVOUS_SYSTEM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.products[*].productId").value(contains(102)));

        mockMvc.perform(get(SEARCH_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .param("healthConcernMajorCategories", "NERVOUS_SYSTEM,MUSCULOSKELETAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.products[*].productId").value(contains(100, 102, 101)));
    }

    @Test
    @DisplayName("검색어와 필터를 함께 적용할 수 있다")
    void searchProducts_withKeywordAndFilter_combinesConditions() throws Exception {
        mockMvc.perform(get(SEARCH_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .param("keyword", "비타민")
                        .param("mfdsCertified", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.products[*].productId").value(contains(100)))
                .andExpect(jsonPath("$.result.totalCount").value(1));
    }

    @Test
    @DisplayName("연령대 필터에 ALL(전체)을 보내면 연령 필터를 적용하지 않는다")
    void searchProducts_withAllAgeGroup_appliesNoAgeFilter() throws Exception {
        mockMvc.perform(get(SEARCH_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .param("ageGroups", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.products[*].productId").value(contains(100, 102, 101, 104)))
                .andExpect(jsonPath("$.result.totalCount").value(4));
    }

    @Test
    @DisplayName("100자를 초과하는 검색어를 보내면 400(COMMON400_1)을 반환한다")
    void searchProducts_withTooLongKeyword_returnsBadRequest() throws Exception {
        mockMvc.perform(get(SEARCH_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .param("keyword", "가".repeat(101)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON400_1"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"INVALID", "review_count"})
    @DisplayName("허용되지 않는 정렬 기준을 보내면 400(COMMON400_1)을 반환한다")
    void searchProducts_withInvalidSort_returnsBadRequest(String sort) throws Exception {
        mockMvc.perform(get(SEARCH_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .param("sort", sort))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON400_1"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "101", "abc"})
    @DisplayName("허용 범위를 벗어난 페이지 크기를 보내면 400(COMMON400_1)을 반환한다")
    void searchProducts_withInvalidSize_returnsBadRequest(String size) throws Exception {
        mockMvc.perform(get(SEARCH_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .param("size", size))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON400_1"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-a-valid-cursor", "abc|3", "101"})
    @DisplayName("형식이 잘못된 커서를 보내면 400(COMMON400_4)을 반환한다")
    void searchProducts_withMalformedCursor_returnsBadRequest(String cursor) throws Exception {
        mockMvc.perform(get(SEARCH_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .param("cursor", cursor))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON400_4"));
    }

    // ---------- 최근 검색어 조회 ----------

    @Test
    @DisplayName("인증 없이 최근 검색어를 조회하면 401을 반환한다")
    void getRecentSearchKeywords_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get(RECENT_KEYWORDS_URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("최근 검색어를 최신순으로 조회한다")
    void getRecentSearchKeywords_returnsKeywordsOrderBySearchedAtDesc() throws Exception {
        mockMvc.perform(get(RECENT_KEYWORDS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SEARCH200_2"))
                .andExpect(jsonPath("$.result.keywords[*].keyword").value(contains("오메가3", "비타민")))
                .andExpect(jsonPath("$.result.keywords.length()").value(2));
    }

    // ---------- 최근 검색어 저장 ----------

    @Test
    @DisplayName("인증 없이 최근 검색어를 저장하면 401을 반환한다")
    void storeRecentSearchKeyword_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(post(RECENT_KEYWORDS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"keyword\":\"루테인\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("새 검색어를 저장하면 201과 저장된 검색어를 반환한다")
    void storeRecentSearchKeyword_withNewKeyword_returnsCreated() throws Exception {
        mockMvc.perform(post(RECENT_KEYWORDS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"keyword\":\"루테인\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("SEARCH201_1"))
                .andExpect(jsonPath("$.result.keyword").value("루테인"))
                .andExpect(jsonPath("$.result.keywordId").isNumber())
                .andExpect(jsonPath("$.result.searchedAt").exists());

        // 신규 저장 → 총 3개, 최신인 루테인이 맨 앞
        mockMvc.perform(get(RECENT_KEYWORDS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(jsonPath("$.result.keywords.length()").value(3))
                .andExpect(jsonPath("$.result.keywords[0].keyword").value("루테인"));
    }

    @Test
    @DisplayName("이미 있는 검색어를 재검색하면 새로 추가하지 않고 검색 일시만 최신화한다")
    void storeRecentSearchKeyword_withExistingKeyword_updatesSearchedAt() throws Exception {
        mockMvc.perform(post(RECENT_KEYWORDS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"keyword\":\"비타민\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result.keywordId").value(1))
                .andExpect(jsonPath("$.result.keyword").value("비타민"));

        // 개수 그대로 2개, 재검색한 비타민이 맨 앞으로 최신화
        mockMvc.perform(get(RECENT_KEYWORDS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(jsonPath("$.result.keywords.length()").value(2))
                .andExpect(jsonPath("$.result.keywords[*].keyword").value(contains("비타민", "오메가3")));
    }

    @Test
    @DisplayName("공백 검색어를 저장하면 400(COMMON400_1)을 반환한다")
    void storeRecentSearchKeyword_withBlankKeyword_returnsBadRequest() throws Exception {
        mockMvc.perform(post(RECENT_KEYWORDS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"keyword\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON400_1"));
    }

    // ---------- 최근 검색어 개별 삭제 ----------

    @Test
    @DisplayName("최근 검색어를 개별 삭제한다")
    void deleteRecentSearchKeyword_returnsDeleted() throws Exception {
        mockMvc.perform(delete(RECENT_KEYWORDS_URL + "/1")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SEARCH200_3"))
                .andExpect(jsonPath("$.result.deleted").value(true))
                .andExpect(jsonPath("$.result.keywordId").value(1));

        // 삭제 후 남은 건 오메가3 하나
        mockMvc.perform(get(RECENT_KEYWORDS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(jsonPath("$.result.keywords[*].keyword").value(contains("오메가3")));
    }

    @Test
    @DisplayName("존재하지 않는 최근 검색어를 삭제하면 404를 반환한다")
    void deleteRecentSearchKeyword_withUnknownId_returnsNotFound() throws Exception {
        mockMvc.perform(delete(RECENT_KEYWORDS_URL + "/999")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SEARCH404_1"));
    }

    @Test
    @DisplayName("다른 회원의 최근 검색어를 삭제하면 403을 반환한다")
    void deleteRecentSearchKeyword_withOtherMembersKeyword_returnsForbidden() throws Exception {
        // keywordId 3 은 OTHER_MEMBER_ID 소유
        mockMvc.perform(delete(RECENT_KEYWORDS_URL + "/3")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("SEARCH403_1"));
    }

    // ---------- 최근 검색어 전체 삭제 ----------

    @Test
    @DisplayName("최근 검색어를 전체 삭제하면 삭제 개수를 반환하고 본인 것만 지운다")
    void deleteAllRecentSearchKeywords_returnsDeletedCount() throws Exception {
        mockMvc.perform(delete(RECENT_KEYWORDS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SEARCH200_4"))
                .andExpect(jsonPath("$.result.deletedCount").value(2));

        // 본인 것은 0개
        mockMvc.perform(get(RECENT_KEYWORDS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(jsonPath("$.result.keywords.length()").value(0));

        // 다른 회원 것은 그대로 유지
        mockMvc.perform(get(RECENT_KEYWORDS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(otherAccessToken)))
                .andExpect(jsonPath("$.result.keywords.length()").value(1));
    }

    private void clearDatabase() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.update("DELETE FROM product_review");
        jdbcTemplate.update("DELETE FROM product_ingredient");
        jdbcTemplate.update("DELETE FROM health_concern_ingredient");
        jdbcTemplate.update("DELETE FROM health_concern");
        jdbcTemplate.update("DELETE FROM ingredient_age_group");
        jdbcTemplate.update("DELETE FROM member_search_keyword");
        jdbcTemplate.update("DELETE FROM product");
        jdbcTemplate.update("DELETE FROM ingredient");
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

    private void insertIngredient(Long id, String name, String imageKey, String recommendedGender) {
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
                        VALUES (?, ?, '성분 설명', NULL, NULL, ?, ?, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                id,
                name,
                recommendedGender,
                imageKey
        );
    }

    private void insertIngredientAgeGroup(Long id, Long ingredientId, String ageGroup) {
        jdbcTemplate.update("""
                        INSERT INTO ingredient_age_group (id, ingredient_id, age_group, created_at, updated_at)
                        VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                id,
                ingredientId,
                ageGroup
        );
    }

    private void insertHealthConcern(Long id, String majorCategory, String minorCategory) {
        jdbcTemplate.update("""
                        INSERT INTO health_concern (
                            id,
                            major_category,
                            minor_category,
                            decline_cause,
                            created_at,
                            updated_at
                        )
                        VALUES (?, ?, ?, '저하 원인', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                id,
                majorCategory,
                minorCategory
        );
    }

    private void insertHealthConcernIngredient(Long id, Long healthConcernId, Long ingredientId) {
        jdbcTemplate.update("""
                        INSERT INTO health_concern_ingredient (
                            id,
                            health_concern_id,
                            ingredient_id,
                            created_at,
                            updated_at
                        )
                        VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                id,
                healthConcernId,
                ingredientId
        );
    }

    private void insertProduct(Long id, String name, String brand, boolean mfdsCertified, String deletedAt) {
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
                        VALUES (?, ?, ?, '상품 설명', 'https://example.com', ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                id,
                name,
                brand,
                mfdsCertified,
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

    private void insertProductReview(Long id, Long productId, Long memberId, int rating, String deletedAt) {
        jdbcTemplate.update("""
                        INSERT INTO product_review (
                            id,
                            product_id,
                            member_id,
                            rating,
                            content,
                            helpful_count,
                            hidden,
                            deleted_at,
                            created_at,
                            updated_at
                        )
                        VALUES (?, ?, ?, ?, '좋아요', 0, FALSE, ?,
                                CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                id,
                productId,
                memberId,
                rating,
                deletedAt
        );
    }

    private void insertMemberSearchKeyword(Long id, Long memberId, String keyword, String searchedAt) {
        jdbcTemplate.update("""
                        INSERT INTO member_search_keyword (id, member_id, keyword, searched_at, created_at, updated_at)
                        VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                id,
                memberId,
                keyword,
                searchedAt
        );
    }

    private void restartMemberSearchKeywordIdentity() {
        jdbcTemplate.execute("ALTER TABLE member_search_keyword ALTER COLUMN id RESTART WITH 100");
    }

    private String bearerToken(String token) {
        return "Bearer " + token;
    }
}
