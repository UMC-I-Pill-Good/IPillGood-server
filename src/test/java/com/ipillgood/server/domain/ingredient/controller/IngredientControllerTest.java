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
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IngredientControllerTest {

    private static final String URL = "/api/v1/contraindications";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtProvider jwtProvider;

    private String accessToken;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM survey_contraindication_selection");
        jdbcTemplate.update("DELETE FROM contraindication_ingredient");
        jdbcTemplate.update("DELETE FROM contraindication");

        insertContraindication(1L, "MEDICATION", "항생제");
        insertContraindication(2L, "MEDICATION", "와파린");
        insertContraindication(20L, "DRINKING", "음주");
        insertContraindication(21L, "PREGNANCY", "임신");
        insertContraindication(22L, "SMOKING", "흡연");
        insertContraindication(23L, "UNDERLYING_DISEASE", "고혈압");
        insertContraindication(32L, "ALLERGY", "대두");

        accessToken = jwtProvider.createAccessToken(1L, "USER");
    }

    @Test
    @DisplayName("인증 없이 금기 조건 목록을 조회하면 401을 반환한다")
    void getContraindications_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("type을 생략하면 설문에서 사용하는 세 타입만 조회한다")
    void getContraindications_withoutType_returnsSurveyTypesOnly() throws Exception {
        mockMvc.perform(get(URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.message").value("금기 조건 목록 조회에 성공했습니다."))
                .andExpect(jsonPath("$.result.contraindications.length()").value(4))
                .andExpect(jsonPath("$.result.contraindications[*].type",
                        contains("MEDICATION", "MEDICATION", "UNDERLYING_DISEASE", "ALLERGY")))
                .andExpect(jsonPath("$.result.contraindications[*].type", not(hasItem("DRINKING"))))
                .andExpect(jsonPath("$.result.contraindications[*].type", not(hasItem("PREGNANCY"))))
                .andExpect(jsonPath("$.result.contraindications[*].type", not(hasItem("SMOKING"))))
                .andExpect(jsonPath("$.result.groupedByType.MEDICATION.length()").value(2))
                .andExpect(jsonPath("$.result.groupedByType.UNDERLYING_DISEASE.length()").value(1))
                .andExpect(jsonPath("$.result.groupedByType.ALLERGY.length()").value(1));
    }

    @Test
    @DisplayName("type=MEDICATION이면 복용약 금기 조건만 조회한다")
    void getContraindications_withMedicationType_returnsMedicationOnly() throws Exception {
        mockMvc.perform(get(URL)
                        .param("type", "MEDICATION")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.contraindications.length()").value(2))
                .andExpect(jsonPath("$.result.contraindications[*].type", everyItem(org.hamcrest.Matchers.is("MEDICATION"))))
                .andExpect(jsonPath("$.result.groupedByType.MEDICATION.length()").value(2))
                .andExpect(jsonPath("$.result.groupedByType.UNDERLYING_DISEASE.length()").value(0))
                .andExpect(jsonPath("$.result.groupedByType.ALLERGY.length()").value(0));
    }

    @Test
    @DisplayName("keyword가 있으면 조건명을 부분 검색한다")
    void getContraindications_withKeyword_filtersByConditionName() throws Exception {
        mockMvc.perform(get(URL)
                        .param("keyword", "고혈")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.contraindications.length()").value(1))
                .andExpect(jsonPath("$.result.contraindications[0].type").value("UNDERLYING_DISEASE"))
                .andExpect(jsonPath("$.result.contraindications[0].conditionName").value("고혈압"))
                .andExpect(jsonPath("$.result.groupedByType.MEDICATION.length()").value(0))
                .andExpect(jsonPath("$.result.groupedByType.UNDERLYING_DISEASE.length()").value(1))
                .andExpect(jsonPath("$.result.groupedByType.ALLERGY.length()").value(0));
    }

    @Test
    @DisplayName("blank keyword는 검색 조건으로 사용하지 않는다")
    void getContraindications_withBlankKeyword_ignoresKeyword() throws Exception {
        mockMvc.perform(get(URL)
                        .param("keyword", "   ")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.contraindications.length()").value(4));
    }

    @ParameterizedTest
    @ValueSource(strings = {"DRINKING", "UNKNOWN"})
    @DisplayName("허용되지 않는 type이면 400을 반환한다")
    void getContraindications_withInvalidType_returnsBadRequest(String invalidType) throws Exception {
        mockMvc.perform(get(URL)
                        .param("type", invalidType)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON400_2"))
                .andExpect(jsonPath("$.message").value("요청값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.result").doesNotExist());
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

    private String bearerToken() {
        return "Bearer " + accessToken;
    }
}
