package com.ipillgood.server.domain.support.controller;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SupportControllerTest {

    private static final String FAQS_URL = "/api/v1/support/faqs";
    private static final String SUPPORT_URL = "/api/v1/support";
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

        // displayOrder: 11 -> 1, 12 -> 2, 10 -> 3, 13 -> 4 (id 순서와 다르게 섞어 정렬이 displayOrder 기준임을 검증)
        insertFaq(11L, "RECOMMENDATION_INGREDIENT", "영양 성분 추천은 어떻게 이루어지나요?",
                "설문 조사 결과와 개인의 건강 정보를 분석해 추천해 드려요.", 1, true);
        insertFaq(12L, "INTAKE", "연속 섭취일 복구 방법은 없나요?",
                "연속 섭취일은 복구할 수 없어요.", 2, true);
        insertFaq(10L, "NOTIFICATION", "복용 알림은 어떻게 설정하나요?",
                "마이페이지 > 설정 > 복용 시간 알림에서 설정할 수 있어요.", 3, true);
        insertFaq(13L, "ETC", "탈퇴는 어떻게 하나요?",
                "마이페이지 > 설정에서 탈퇴할 수 있어요.", 4, true);
        // 비활성 FAQ: displayOrder가 가장 낮아도 목록/미리보기 모두에서 제외되어야 함
        insertFaq(14L, "ETC", "비활성 문의입니다", "비활성 처리된 FAQ입니다.", 0, false);

        accessToken = jwtProvider.createAccessToken(MEMBER_ID, "USER");
    }

    // ---------- FAQ 목록 조회 ----------

    @Test
    @DisplayName("인증 없이 FAQ 목록을 조회하면 401을 반환한다")
    void getFaqs_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get(FAQS_URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("필터 없이 조회하면 활성 FAQ 전체를 표시 순서대로 반환한다")
    void getFaqs_withoutFilter_returnsActiveFaqsOrderByDisplayOrder() throws Exception {
        mockMvc.perform(get(FAQS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUPPORT200_1"))
                .andExpect(jsonPath("$.result.faqs.length()").value(4))
                .andExpect(jsonPath("$.result.faqs[*].faqId").value(contains(11, 12, 10, 13)));
    }

    @Test
    @DisplayName("category로 필터링하면 해당 카테고리의 활성 FAQ만 반환한다")
    void getFaqs_withCategory_returnsMatchingCategoryOnly() throws Exception {
        mockMvc.perform(get(FAQS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken())
                        .param("category", "ETC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.faqs.length()").value(1))
                .andExpect(jsonPath("$.result.faqs[0].faqId").value(13));
    }

    @Test
    @DisplayName("keyword는 질문에 부분일치하는 FAQ만 반환하고 답변 내용은 대상이 아니다")
    void getFaqs_withKeyword_matchesQuestionOnlyNotAnswer() throws Exception {
        // "어떻게"는 질문 3건(11, 10, 13)에 포함
        mockMvc.perform(get(FAQS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken())
                        .param("keyword", "어떻게"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.faqs[*].faqId").value(contains(11, 10, 13)));

        // "설문 조사"는 11번 답변에만 있고 질문에는 없으므로 매칭되지 않음
        mockMvc.perform(get(FAQS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken())
                        .param("keyword", "설문 조사"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.faqs.length()").value(0));
    }

    @Test
    @DisplayName("category와 keyword를 함께 보내면 둘 다 만족하는 FAQ만 반환한다")
    void getFaqs_withCategoryAndKeyword_appliesBothConditions() throws Exception {
        // NOTIFICATION 카테고리(10)의 질문에 "알림"이 포함되어 일치
        mockMvc.perform(get(FAQS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken())
                        .param("category", "NOTIFICATION")
                        .param("keyword", "알림"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.faqs[*].faqId").value(contains(10)));

        // INTAKE 카테고리(12)의 질문에는 "알림"이 없어 카테고리는 맞아도 키워드가 불일치 -> 빈 배열
        mockMvc.perform(get(FAQS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken())
                        .param("category", "INTAKE")
                        .param("keyword", "알림"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.faqs.length()").value(0));
    }

    @Test
    @DisplayName("존재하지 않는 category 값을 보내면 400(COMMON400_1)을 반환한다")
    void getFaqs_withInvalidCategory_returnsBadRequest() throws Exception {
        mockMvc.perform(get(FAQS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken())
                        .param("category", "INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON400_1"));
    }

    @Test
    @DisplayName("keyword가 빈 값이거나 공백뿐이면 키워드 필터를 적용하지 않는다")
    void getFaqs_withBlankKeyword_appliesNoKeywordFilter() throws Exception {
        mockMvc.perform(get(FAQS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken())
                        .param("keyword", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.faqs.length()").value(4));

        mockMvc.perform(get(FAQS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken())
                        .param("keyword", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.faqs.length()").value(4));
    }

    @Test
    @DisplayName("keyword 매칭은 대소문자를 구분하지 않는다")
    void getFaqs_withKeyword_isCaseInsensitive() throws Exception {
        insertFaq(15L, "ETC", "iOS 앱에서도 사용할 수 있나요?", "네, iOS와 안드로이드 모두 지원해요.", 5, true);

        mockMvc.perform(get(FAQS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken())
                        .param("keyword", "IOS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.faqs[*].faqId").value(contains(15)));
    }

    @Test
    @DisplayName("활성 FAQ가 하나도 없으면 빈 배열을 반환한다")
    void getFaqs_withNoActiveFaqs_returnsEmptyList() throws Exception {
        jdbcTemplate.update("DELETE FROM faq");

        mockMvc.perform(get(FAQS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.faqs.length()").value(0));
    }

    @Test
    @DisplayName("FAQ 항목은 category/question/answer를 그대로 매핑해 반환한다")
    void getFaqs_returnsFaqItemFieldsMappedCorrectly() throws Exception {
        mockMvc.perform(get(FAQS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken())
                        .param("category", "NOTIFICATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.faqs[0].faqId").value(10))
                .andExpect(jsonPath("$.result.faqs[0].category").value("NOTIFICATION"))
                .andExpect(jsonPath("$.result.faqs[0].question").value("복용 알림은 어떻게 설정하나요?"))
                .andExpect(jsonPath("$.result.faqs[0].answer")
                        .value("마이페이지 > 설정 > 복용 시간 알림에서 설정할 수 있어요."));
    }

    // ---------- 문의/고객센터 조회 ----------

    @Test
    @DisplayName("인증 없이 문의/고객센터를 조회하면 401을 반환한다")
    void getSupportInfo_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get(SUPPORT_URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("활성 FAQ 상위 3개 미리보기와 문의처 정보를 반환한다")
    void getSupportInfo_returnsTopThreeFaqsAndContactInfo() throws Exception {
        mockMvc.perform(get(SUPPORT_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUPPORT200_2"))
                // 활성 FAQ 4개 중 displayOrder 상위 3개(11, 12, 10)만, 4번째(13)와 비활성(14)은 제외
                .andExpect(jsonPath("$.result.faqs.length()").value(3))
                .andExpect(jsonPath("$.result.faqs[*].faqId").value(contains(11, 12, 10)))
                .andExpect(jsonPath("$.result.contactEmail").value("ipillgood.official@gmail.com"))
                .andExpect(jsonPath("$.result.operatingHours").value("평일 09:00 ~ 18:00"))
                .andExpect(jsonPath("$.result.closedDays").value("주말 및 공휴일 휴무"));
    }

    @Test
    @DisplayName("활성 FAQ가 3개 미만이면 있는 만큼만 미리보기로 반환한다")
    void getSupportInfo_withFewerThanThreeFaqs_returnsAllOfThem() throws Exception {
        // 활성 FAQ를 1개(11)만 남기고 나머지는 삭제
        jdbcTemplate.update("DELETE FROM faq WHERE id IN (10, 12, 13, 14)");

        mockMvc.perform(get(SUPPORT_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.faqs.length()").value(1))
                .andExpect(jsonPath("$.result.faqs[0].faqId").value(11));
    }

    @Test
    @DisplayName("활성 FAQ가 하나도 없어도 문의처 정보는 그대로 반환하고 faqs는 빈 배열이다")
    void getSupportInfo_withNoActiveFaqs_returnsEmptyFaqsWithContactInfo() throws Exception {
        jdbcTemplate.update("DELETE FROM faq");

        mockMvc.perform(get(SUPPORT_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.faqs.length()").value(0))
                .andExpect(jsonPath("$.result.contactEmail").value("ipillgood.official@gmail.com"));
    }

    private void clearDatabase() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.update("DELETE FROM faq");
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

    private void insertFaq(
            Long id,
            String category,
            String question,
            String answer,
            int displayOrder,
            boolean active
    ) {
        jdbcTemplate.update("""
                        INSERT INTO faq (
                            id,
                            category,
                            question,
                            answer,
                            display_order,
                            active,
                            created_at,
                            updated_at
                        )
                        VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                id,
                category,
                question,
                answer,
                displayOrder,
                active
        );
    }

    private String bearerToken() {
        return "Bearer " + accessToken;
    }
}
