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

import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationControllerTest {

    private static final String APP_PUSH_SETTING_URL = "/api/v1/notification-settings/me";
    private static final String PUSH_TOKEN_URL = "/api/v1/push-tokens";
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

    @Test
    @DisplayName("인증 없이 푸시 토큰을 등록하면 401을 반환한다")
    void registerPushToken_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(post(PUSH_TOKEN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "platform": "WEB",
                                  "token": "fcm_registration_token_sample"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("온보딩 미완료 회원도 푸시 토큰을 등록할 수 있다")
    void registerPushToken_withoutCompletedOnboarding_registersToken() throws Exception {
        mockMvc.perform(post(PUSH_TOKEN_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(onboardingIncompleteAccessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "platform": "WEB",
                                  "token": "incomplete_member_token"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.result.platform").value("WEB"))
                .andExpect(jsonPath("$.result.active").value(true));

        assertEquals(1, countMemberPushTokens());
        assertEquals(ONBOARDING_INCOMPLETE_MEMBER_ID, findPushTokenMemberId("incomplete_member_token"));
    }

    @Test
    @DisplayName("신규 푸시 토큰이면 회원 푸시 토큰 행을 생성한다")
    void registerPushToken_withNewToken_createsPushToken() throws Exception {
        int beforeTokenCount = countMemberPushTokens();

        mockMvc.perform(post(PUSH_TOKEN_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "platform": "WEB",
                                  "token": "new_fcm_token"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.result.pushTokenId").isNumber())
                .andExpect(jsonPath("$.result.platform").value("WEB"))
                .andExpect(jsonPath("$.result.active").value(true))
                .andExpect(jsonPath("$.result.lastSeenAt").exists())
                .andExpect(jsonPath("$.result.token").doesNotExist());

        assertEquals(beforeTokenCount + 1, countMemberPushTokens());
        assertEquals(MEMBER_ID, findPushTokenMemberId("new_fcm_token"));
        assertEquals("WEB", findPushTokenPlatform("new_fcm_token"));
        assertEquals(Boolean.TRUE, findPushTokenActive("new_fcm_token"));
        assertNotNull(findPushTokenLastSeenAt("new_fcm_token"));
    }

    @Test
    @DisplayName("같은 회원이 같은 푸시 토큰을 재등록하면 기존 행을 갱신한다")
    void registerPushToken_withSameMemberAndSameToken_updatesExistingPushToken() throws Exception {
        insertMemberPushToken(21L, MEMBER_ID, "WEB", "same_member_token", false, "2026-07-01 00:00:00");
        Timestamp oldLastSeenAt = Timestamp.valueOf("2026-07-01 00:00:00");

        mockMvc.perform(post(PUSH_TOKEN_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "platform": "WEB",
                                  "token": "same_member_token"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.pushTokenId").value(21))
                .andExpect(jsonPath("$.result.active").value(true));

        assertEquals(1, countMemberPushTokens());
        assertEquals(MEMBER_ID, findPushTokenMemberId("same_member_token"));
        assertEquals(Boolean.TRUE, findPushTokenActive("same_member_token"));
        assertNotEquals(oldLastSeenAt, findPushTokenLastSeenAt("same_member_token"));
    }

    @Test
    @DisplayName("다른 회원의 같은 푸시 토큰을 등록하면 현재 로그인 회원에게 귀속한다")
    void registerPushToken_withOtherMemberToken_transfersOwnership() throws Exception {
        insertMemberPushToken(
                22L,
                ONBOARDING_INCOMPLETE_MEMBER_ID,
                "WEB",
                "other_member_token",
                true,
                "2026-07-01 00:00:00"
        );

        mockMvc.perform(post(PUSH_TOKEN_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "platform": "WEB",
                                  "token": "other_member_token"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.pushTokenId").value(22))
                .andExpect(jsonPath("$.result.active").value(true));

        assertEquals(1, countMemberPushTokens());
        assertEquals(MEMBER_ID, findPushTokenMemberId("other_member_token"));
    }

    @Test
    @DisplayName("비활성 푸시 토큰을 재등록하면 다시 활성화한다")
    void registerPushToken_withInactiveToken_reactivatesPushToken() throws Exception {
        insertMemberPushToken(23L, MEMBER_ID, "WEB", "inactive_token", false, "2026-07-01 00:00:00");

        mockMvc.perform(post(PUSH_TOKEN_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "platform": "WEB",
                                  "token": "inactive_token"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.pushTokenId").value(23))
                .andExpect(jsonPath("$.result.active").value(true));

        assertEquals(Boolean.TRUE, findPushTokenActive("inactive_token"));
    }

    @Test
    @DisplayName("푸시 토큰 등록 요청 본문이 없으면 400을 반환한다")
    void registerPushToken_withoutBody_returnsBadRequest() throws Exception {
        mockMvc.perform(post(PUSH_TOKEN_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("NOTIFICATION400_3"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("푸시 토큰 등록 요청 값이 올바르지 않으면 400을 반환한다")
    void registerPushToken_withInvalidRequest_returnsBadRequest() throws Exception {
        List<String> invalidBodies = List.of(
                "{}",
                """
                        {
                          "platform": null,
                          "token": "fcm_token"
                        }
                        """,
                """
                        {
                          "platform": "WEB",
                          "token": null
                        }
                        """,
                """
                        {
                          "platform": 1,
                          "token": "fcm_token"
                        }
                        """,
                """
                        {
                          "platform": "WEB",
                          "token": 1
                        }
                        """,
                """
                        {
                          "platform": "IOS",
                          "token": "fcm_token"
                        }
                        """,
                """
                        {
                          "platform": " WEB",
                          "token": "fcm_token"
                        }
                        """,
                """
                        {
                          "platform": "WEB",
                          "token": ""
                        }
                        """,
                """
                        {
                          "platform": "WEB",
                          "token": "   "
                        }
                        """,
                """
                        {
                          "platform": "WEB",
                          "token": "%s"
                        }
                        """.formatted("a".repeat(513))
        );

        for (String invalidBody : invalidBodies) {
            mockMvc.perform(post(PUSH_TOKEN_URL)
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("NOTIFICATION400_3"))
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

    private void insertMemberPushToken(
            Long id,
            Long memberId,
            String platform,
            String token,
            boolean active,
            String lastSeenAt
    ) {
        jdbcTemplate.update("""
                        INSERT INTO member_push_token (
                            id,
                            member_id,
                            platform,
                            token,
                            active,
                            last_seen_at,
                            created_at,
                            updated_at
                        )
                        VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                id,
                memberId,
                platform,
                token,
                active,
                lastSeenAt
        );
    }

    private int countMemberNotificationSettings() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM member_notification_setting", Integer.class);
        return count == null ? 0 : count;
    }

    private int countMemberPushTokens() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM member_push_token", Integer.class);
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

    private Long findPushTokenMemberId(String token) {
        return jdbcTemplate.queryForObject(
                "SELECT member_id FROM member_push_token WHERE token = ?",
                Long.class,
                token
        );
    }

    private String findPushTokenPlatform(String token) {
        return jdbcTemplate.queryForObject(
                "SELECT platform FROM member_push_token WHERE token = ?",
                String.class,
                token
        );
    }

    private Boolean findPushTokenActive(String token) {
        return jdbcTemplate.queryForObject(
                "SELECT active FROM member_push_token WHERE token = ?",
                Boolean.class,
                token
        );
    }

    private Timestamp findPushTokenLastSeenAt(String token) {
        return jdbcTemplate.queryForObject(
                "SELECT last_seen_at FROM member_push_token WHERE token = ?",
                Timestamp.class,
                token
        );
    }

    private String bearerToken(String token) {
        return "Bearer " + token;
    }
}
