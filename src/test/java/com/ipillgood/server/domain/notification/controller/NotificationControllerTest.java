package com.ipillgood.server.domain.notification.controller;

import com.ipillgood.server.global.security.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationControllerTest {

    private static final String APP_PUSH_SETTING_URL = "/api/v1/notification-settings/me";
    private static final long MEMBER_ID = 1L;
    private static final long ONBOARDING_INCOMPLETE_MEMBER_ID = 2L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtProvider jwtProvider;

    private String accessToken;
    private String onboardingIncompleteAccessToken;

    @BeforeEach
    void setUp() {
        clearDatabase();

        insertMember(MEMBER_ID, "알림유저", "2026-07-01 00:00:00");
        insertMember(ONBOARDING_INCOMPLETE_MEMBER_ID, "미완료", null);

        accessToken = jwtProvider.createAccessToken(MEMBER_ID, "USER");
        onboardingIncompleteAccessToken = jwtProvider.createAccessToken(ONBOARDING_INCOMPLETE_MEMBER_ID, "USER");
    }

    @Test
    @DisplayName("인증 없이 앱 푸시 설정을 조회하면 401을 반환한다")
    void getAppPushSetting_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get(APP_PUSH_SETTING_URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("온보딩을 완료하지 않은 회원은 앱 푸시 설정을 조회할 수 없다")
    void getAppPushSetting_withoutCompletedOnboarding_returnsForbidden() throws Exception {
        mockMvc.perform(get(APP_PUSH_SETTING_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(onboardingIncompleteAccessToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("NOTIFICATION403_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("설정 행이 없으면 기본값 true를 반환하고 설정 행을 생성하지 않는다")
    void getAppPushSetting_withoutSetting_returnsDefaultTrueWithoutCreatingSetting() throws Exception {
        int beforeSettingCount = countMemberNotificationSettings();

        mockMvc.perform(get(APP_PUSH_SETTING_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.result.pushEnabled").value(true));

        assertEquals(beforeSettingCount, countMemberNotificationSettings());
    }

    @Test
    @DisplayName("설정 행의 앱 푸시 설정이 false이면 false를 반환한다")
    void getAppPushSetting_withDisabledSetting_returnsFalse() throws Exception {
        insertMemberNotificationSetting(MEMBER_ID, false, true);

        mockMvc.perform(get(APP_PUSH_SETTING_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.result.pushEnabled").value(false));
    }

    @Test
    @DisplayName("설정 행의 앱 푸시 설정이 true이면 true를 반환한다")
    void getAppPushSetting_withEnabledSetting_returnsTrue() throws Exception {
        insertMemberNotificationSetting(MEMBER_ID, true, false);

        mockMvc.perform(get(APP_PUSH_SETTING_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.result.pushEnabled").value(true));
    }

    @Test
    @DisplayName("인증 없이 앱 푸시 설정을 변경하면 401을 반환한다")
    void updateAppPushSetting_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(patch(APP_PUSH_SETTING_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pushEnabled": false
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("온보딩을 완료하지 않은 회원은 앱 푸시 설정을 변경할 수 없다")
    void updateAppPushSetting_withoutCompletedOnboarding_returnsForbidden() throws Exception {
        mockMvc.perform(patch(APP_PUSH_SETTING_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(onboardingIncompleteAccessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pushEnabled": false
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("NOTIFICATION403_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("설정 행이 없으면 생성한 뒤 앱 푸시 설정을 변경한다")
    void updateAppPushSetting_withoutSetting_createsSettingAndReturnsUpdatedValue() throws Exception {
        int beforeSettingCount = countMemberNotificationSettings();

        mockMvc.perform(patch(APP_PUSH_SETTING_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pushEnabled": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.result.pushEnabled").value(false));

        assertEquals(beforeSettingCount + 1, countMemberNotificationSettings());
        assertEquals(Boolean.FALSE, findPushEnabled(MEMBER_ID));
        assertEquals(Boolean.TRUE, findIntakePushEnabled(MEMBER_ID));
    }

    @Test
    @DisplayName("기존 설정 행이 있으면 앱 푸시 설정만 true로 변경한다")
    void updateAppPushSetting_withExistingSetting_updatesPushEnabledToTrueOnly() throws Exception {
        insertMemberNotificationSetting(MEMBER_ID, false, false);

        mockMvc.perform(patch(APP_PUSH_SETTING_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pushEnabled": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.result.pushEnabled").value(true));

        assertEquals(1, countMemberNotificationSettings());
        assertEquals(Boolean.TRUE, findPushEnabled(MEMBER_ID));
        assertEquals(Boolean.FALSE, findIntakePushEnabled(MEMBER_ID));
    }

    @Test
    @DisplayName("기존 설정 행이 있으면 앱 푸시 설정만 false로 변경한다")
    void updateAppPushSetting_withExistingSetting_updatesPushEnabledToFalseOnly() throws Exception {
        insertMemberNotificationSetting(MEMBER_ID, true, true);

        mockMvc.perform(patch(APP_PUSH_SETTING_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pushEnabled": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.result.pushEnabled").value(false));

        assertEquals(1, countMemberNotificationSettings());
        assertEquals(Boolean.FALSE, findPushEnabled(MEMBER_ID));
        assertEquals(Boolean.TRUE, findIntakePushEnabled(MEMBER_ID));
    }

    @Test
    @DisplayName("앱 푸시 설정 변경 요청 본문이 없으면 400을 반환한다")
    void updateAppPushSetting_withoutBody_returnsBadRequest() throws Exception {
        mockMvc.perform(patch(APP_PUSH_SETTING_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("NOTIFICATION400_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("앱 푸시 설정 변경 요청의 pushEnabled가 Boolean이 아니면 400을 반환한다")
    void updateAppPushSetting_withInvalidPushEnabled_returnsBadRequest() throws Exception {
        List<String> invalidBodies = List.of(
                "{}",
                """
                        {
                          "pushEnabled": null
                        }
                        """,
                """
                        {
                          "pushEnabled": "false"
                        }
                        """,
                """
                        {
                          "pushEnabled": 1
                        }
                        """
        );

        for (String invalidBody : invalidBodies) {
            mockMvc.perform(patch(APP_PUSH_SETTING_URL)
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("NOTIFICATION400_1"))
                    .andExpect(jsonPath("$.result").doesNotExist());
        }
    }

    private void clearDatabase() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.update("DELETE FROM notification_delivery_log");
        jdbcTemplate.update("DELETE FROM member_push_token");
        jdbcTemplate.update("DELETE FROM member_notification_setting");
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

    private void insertMemberNotificationSetting(
            Long memberId,
            boolean pushEnabled,
            boolean intakePushEnabled
    ) {
        jdbcTemplate.update("""
                        INSERT INTO member_notification_setting (
                            member_id,
                            push_enabled,
                            intake_push_enabled,
                            created_at,
                            updated_at
                        )
                        VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                memberId,
                pushEnabled,
                intakePushEnabled
        );
    }

    private int countMemberNotificationSettings() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM member_notification_setting", Integer.class);
        return count == null ? 0 : count;
    }

    private Boolean findPushEnabled(Long memberId) {
        return jdbcTemplate.queryForObject(
                "SELECT push_enabled FROM member_notification_setting WHERE member_id = ?",
                Boolean.class,
                memberId
        );
    }

    private Boolean findIntakePushEnabled(Long memberId) {
        return jdbcTemplate.queryForObject(
                "SELECT intake_push_enabled FROM member_notification_setting WHERE member_id = ?",
                Boolean.class,
                memberId
        );
    }

    private String bearerToken(String token) {
        return "Bearer " + token;
    }
}
