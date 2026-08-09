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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
    private static final String INTAKE_NOTIFICATION_SETTINGS_URL = "/api/v1/notification-settings/intake";
    private static final String ACTIVE_PRODUCT_NOTIFICATION_SETTING_URL_PREFIX =
            "/api/v1/notification-settings/intake/active-products";
    private static final String PUSH_TOKEN_URL = "/api/v1/push-tokens";
    private static final long MEMBER_ID = 1L;
    private static final long ONBOARDING_INCOMPLETE_MEMBER_ID = 2L;
    private static final long OTHER_MEMBER_ID = 3L;

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

        accessToken = jwtProvider.createAccessToken(MEMBER_ID, "USER", "test-session");
        onboardingIncompleteAccessToken = jwtProvider.createAccessToken(ONBOARDING_INCOMPLETE_MEMBER_ID, "USER", "test-session");
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
    @DisplayName("설정 행이 없으면 기본값 false를 반환하고 설정 행을 생성하지 않는다")
    void getAppPushSetting_withoutSetting_returnsDefaultFalseWithoutCreatingSetting() throws Exception {
        int beforeSettingCount = countMemberNotificationSettings();

        mockMvc.perform(get(APP_PUSH_SETTING_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.result.pushEnabled").value(false));

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
    @DisplayName("인증 없이 복용 알림 설정을 통합 조회하면 401을 반환한다")
    void getIntakeNotificationSettings_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get(INTAKE_NOTIFICATION_SETTINGS_URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("온보딩을 완료하지 않은 회원은 복용 알림 설정을 통합 조회할 수 없다")
    void getIntakeNotificationSettings_withoutCompletedOnboarding_returnsForbidden() throws Exception {
        mockMvc.perform(get(INTAKE_NOTIFICATION_SETTINGS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(onboardingIncompleteAccessToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("NOTIFICATION403_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("설정 행과 섭취 중 영양제가 없으면 기본값과 빈 목록을 반환하고 설정 행을 생성하지 않는다")
    void getIntakeNotificationSettings_withoutSettingAndActiveProducts_returnsDefaultWithoutCreatingSetting()
            throws Exception {
        int beforeSettingCount = countMemberNotificationSettings();

        mockMvc.perform(get(INTAKE_NOTIFICATION_SETTINGS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.result.pushEnabled").value(false))
                .andExpect(jsonPath("$.result.intakePushEnabled").value(true))
                .andExpect(jsonPath("$.result.activeProductCount").value(0))
                .andExpect(jsonPath("$.result.activeProducts").isArray())
                .andExpect(jsonPath("$.result.activeProducts.length()").value(0));

        assertEquals(beforeSettingCount, countMemberNotificationSettings());
    }

    @Test
    @DisplayName("설정 행이 있으면 저장된 앱 푸시와 복용 전체 알림 값을 반환한다")
    void getIntakeNotificationSettings_withExistingSetting_returnsSavedSettings() throws Exception {
        insertMemberNotificationSetting(MEMBER_ID, false, false);

        mockMvc.perform(get(INTAKE_NOTIFICATION_SETTINGS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.result.pushEnabled").value(false))
                .andExpect(jsonPath("$.result.intakePushEnabled").value(false))
                .andExpect(jsonPath("$.result.activeProductCount").value(0))
                .andExpect(jsonPath("$.result.activeProducts.length()").value(0));
    }

    @Test
    @DisplayName("활성 섭취 중 영양제의 개별 알림 설정을 정렬해 반환하고 비활성 대상은 제외한다")
    void getIntakeNotificationSettings_withActiveProducts_returnsSortedActiveProductSettings() throws Exception {
        insertMember(OTHER_MEMBER_ID, "다른회원", "2026-07-01 00:00:00");
        insertMemberNotificationSetting(MEMBER_ID, true, false);

        insertProduct(112L, "뉴트리코어 유기농 비타민D 1000IU", null);
        insertProduct(124L, "헬로바이오 맥스 비타민C 3000", null);
        insertProduct(130L, "중단된 제품", null);
        insertProduct(131L, "삭제된 캐비닛 제품", null);
        insertProduct(132L, "삭제된 상품", "2026-07-02 00:00:00");
        insertProduct(133L, "다른 회원 제품", null);

        insertMemberProduct(15L, MEMBER_ID, 112L, null);
        insertMemberProduct(16L, MEMBER_ID, 124L, null);
        insertMemberProduct(17L, MEMBER_ID, 130L, null);
        insertMemberProduct(18L, MEMBER_ID, 131L, "2026-07-02 00:00:00");
        insertMemberProduct(19L, MEMBER_ID, 132L, null);
        insertMemberProduct(20L, OTHER_MEMBER_ID, 133L, null);

        insertActiveProduct(22L, MEMBER_ID, 15L, null, true, "08:30", "2026-07-01 10:00:00");
        insertActiveProduct(20L, MEMBER_ID, 16L, null, false, "21:00", "2026-07-02 10:00:00");
        insertActiveProduct(23L, MEMBER_ID, 17L, "2026-07-10", true, "09:00", "2026-07-03 10:00:00");
        insertActiveProduct(24L, MEMBER_ID, 18L, null, true, "10:00", "2026-07-04 10:00:00");
        insertActiveProduct(25L, MEMBER_ID, 19L, null, true, "11:00", "2026-07-05 10:00:00");
        insertActiveProduct(26L, OTHER_MEMBER_ID, 20L, null, true, "12:00", "2026-07-06 10:00:00");

        mockMvc.perform(get(INTAKE_NOTIFICATION_SETTINGS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.result.pushEnabled").value(true))
                .andExpect(jsonPath("$.result.intakePushEnabled").value(false))
                .andExpect(jsonPath("$.result.activeProductCount").value(2))
                .andExpect(jsonPath("$.result.activeProducts.length()").value(2))
                .andExpect(jsonPath("$.result.activeProducts[0].activeProductId").value(22))
                .andExpect(jsonPath("$.result.activeProducts[0].memberProductId").value(15))
                .andExpect(jsonPath("$.result.activeProducts[0].productId").value(112))
                .andExpect(jsonPath("$.result.activeProducts[0].productName")
                        .value("뉴트리코어 유기농 비타민D 1000IU"))
                .andExpect(jsonPath("$.result.activeProducts[0].notificationEnabled").value(true))
                .andExpect(jsonPath("$.result.activeProducts[0].intakeTime").value("08:30"))
                .andExpect(jsonPath("$.result.activeProducts[1].activeProductId").value(20))
                .andExpect(jsonPath("$.result.activeProducts[1].memberProductId").value(16))
                .andExpect(jsonPath("$.result.activeProducts[1].productId").value(124))
                .andExpect(jsonPath("$.result.activeProducts[1].productName")
                        .value("헬로바이오 맥스 비타민C 3000"))
                .andExpect(jsonPath("$.result.activeProducts[1].notificationEnabled").value(false))
                .andExpect(jsonPath("$.result.activeProducts[1].intakeTime").value("21:00"));
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
    @DisplayName("인증 없이 복용 전체 알림 설정을 변경하면 401을 반환한다")
    void updateIntakePushSetting_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(patch(INTAKE_NOTIFICATION_SETTINGS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "intakePushEnabled": false
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("온보딩을 완료하지 않은 회원은 복용 전체 알림 설정을 변경할 수 없다")
    void updateIntakePushSetting_withoutCompletedOnboarding_returnsForbidden() throws Exception {
        mockMvc.perform(patch(INTAKE_NOTIFICATION_SETTINGS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(onboardingIncompleteAccessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "intakePushEnabled": false
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("NOTIFICATION403_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("설정 행이 없으면 생성한 뒤 복용 전체 알림 설정을 변경한다")
    void updateIntakePushSetting_withoutSetting_createsSettingAndReturnsUpdatedValue() throws Exception {
        int beforeSettingCount = countMemberNotificationSettings();

        mockMvc.perform(patch(INTAKE_NOTIFICATION_SETTINGS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "intakePushEnabled": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.result.pushEnabled").value(false))
                .andExpect(jsonPath("$.result.intakePushEnabled").value(false));

        assertEquals(beforeSettingCount + 1, countMemberNotificationSettings());
        assertEquals(Boolean.FALSE, findPushEnabled(MEMBER_ID));
        assertEquals(Boolean.FALSE, findIntakePushEnabled(MEMBER_ID));
    }

    @Test
    @DisplayName("기존 설정 행이 있으면 복용 전체 알림 설정만 true로 변경한다")
    void updateIntakePushSetting_withExistingSetting_updatesIntakePushEnabledToTrueOnly() throws Exception {
        insertMemberNotificationSetting(MEMBER_ID, false, false);

        mockMvc.perform(patch(INTAKE_NOTIFICATION_SETTINGS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "intakePushEnabled": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.result.pushEnabled").value(false))
                .andExpect(jsonPath("$.result.intakePushEnabled").value(true));

        assertEquals(1, countMemberNotificationSettings());
        assertEquals(Boolean.FALSE, findPushEnabled(MEMBER_ID));
        assertEquals(Boolean.TRUE, findIntakePushEnabled(MEMBER_ID));
    }

    @Test
    @DisplayName("기존 설정 행이 있으면 복용 전체 알림 설정만 false로 변경한다")
    void updateIntakePushSetting_withExistingSetting_updatesIntakePushEnabledToFalseOnly() throws Exception {
        insertMemberNotificationSetting(MEMBER_ID, true, true);

        mockMvc.perform(patch(INTAKE_NOTIFICATION_SETTINGS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "intakePushEnabled": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.result.pushEnabled").value(true))
                .andExpect(jsonPath("$.result.intakePushEnabled").value(false));

        assertEquals(1, countMemberNotificationSettings());
        assertEquals(Boolean.TRUE, findPushEnabled(MEMBER_ID));
        assertEquals(Boolean.FALSE, findIntakePushEnabled(MEMBER_ID));
    }

    @Test
    @DisplayName("복용 전체 알림 설정 변경 요청 본문이 없으면 400을 반환한다")
    void updateIntakePushSetting_withoutBody_returnsBadRequest() throws Exception {
        mockMvc.perform(patch(INTAKE_NOTIFICATION_SETTINGS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("NOTIFICATION400_2"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("복용 전체 알림 설정 변경 요청의 intakePushEnabled가 Boolean이 아니면 400을 반환한다")
    void updateIntakePushSetting_withInvalidIntakePushEnabled_returnsBadRequest() throws Exception {
        List<String> invalidBodies = List.of(
                "{}",
                "null",
                """
                        {
                          "intakePushEnabled": null
                        }
                        """,
                """
                        {
                          "intakePushEnabled": "false"
                        }
                        """,
                """
                        {
                          "intakePushEnabled": 1
                        }
                        """
        );

        for (String invalidBody : invalidBodies) {
            mockMvc.perform(patch(INTAKE_NOTIFICATION_SETTINGS_URL)
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("NOTIFICATION400_2"))
                    .andExpect(jsonPath("$.result").doesNotExist());
        }
    }

    @Test
    @DisplayName("인증 없이 개별 복용 알림 설정을 변경하면 401을 반환한다")
    void updateActiveProductNotificationSetting_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(patch(activeProductNotificationSettingUrl(70L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "notificationEnabled": false
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("온보딩을 완료하지 않은 회원은 개별 복용 알림 설정을 변경할 수 없다")
    void updateActiveProductNotificationSetting_withoutCompletedOnboarding_returnsForbidden() throws Exception {
        mockMvc.perform(patch(activeProductNotificationSettingUrl(70L))
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(onboardingIncompleteAccessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "notificationEnabled": false
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("NOTIFICATION403_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("활성 섭취 중 영양제의 개별 알림을 false로 변경한다")
    void updateActiveProductNotificationSetting_withActiveProduct_updatesNotificationEnabledToFalse()
            throws Exception {
        insertProduct(140L, "개별 알림 변경 제품", null);
        insertMemberProduct(40L, MEMBER_ID, 140L, null);
        insertActiveProduct(70L, MEMBER_ID, 40L, null, true, "08:30", "2026-07-01 10:00:00");

        mockMvc.perform(patch(activeProductNotificationSettingUrl(70L))
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "notificationEnabled": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.result.activeProductId").value(70))
                .andExpect(jsonPath("$.result.notificationEnabled").value(false));

        assertEquals(Boolean.FALSE, findActiveProductNotificationEnabled(70L));
    }

    @Test
    @DisplayName("개별 알림 설정을 기존 값과 같은 상태로 요청해도 성공한다")
    void updateActiveProductNotificationSetting_withSameNotificationEnabled_returnsSuccess() throws Exception {
        insertProduct(141L, "이미 꺼진 개별 알림 제품", null);
        insertMemberProduct(41L, MEMBER_ID, 141L, null);
        insertActiveProduct(71L, MEMBER_ID, 41L, null, false, "08:30", "2026-07-01 10:00:00");

        mockMvc.perform(patch(activeProductNotificationSettingUrl(71L))
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "notificationEnabled": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.result.activeProductId").value(71))
                .andExpect(jsonPath("$.result.notificationEnabled").value(false));

        assertEquals(Boolean.FALSE, findActiveProductNotificationEnabled(71L));
    }

    @Test
    @DisplayName("개별 알림 대상 ID가 1 미만이면 400을 반환한다")
    void updateActiveProductNotificationSetting_withInvalidActiveProductId_returnsBadRequest()
            throws Exception {
        List<String> invalidActiveProductIds = List.of("0", "-1");

        for (String invalidActiveProductId : invalidActiveProductIds) {
            mockMvc.perform(patch(activeProductNotificationSettingUrl(invalidActiveProductId))
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "notificationEnabled": false
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("NOTIFICATION400_5"))
                    .andExpect(jsonPath("$.result").doesNotExist());
        }
    }

    @Test
    @DisplayName("개별 알림 대상 ID가 숫자 형식이 아니면 400을 반환한다")
    void updateActiveProductNotificationSetting_withNonNumericActiveProductId_returnsBadRequest()
            throws Exception {
        mockMvc.perform(patch(activeProductNotificationSettingUrl("abc"))
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "notificationEnabled": false
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON400_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("개별 알림 설정 변경 요청 본문이 없으면 400을 반환한다")
    void updateActiveProductNotificationSetting_withoutBody_returnsBadRequest() throws Exception {
        mockMvc.perform(patch(activeProductNotificationSettingUrl(70L))
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("NOTIFICATION400_6"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("개별 알림 설정 변경 요청의 notificationEnabled가 Boolean이 아니면 400을 반환한다")
    void updateActiveProductNotificationSetting_withInvalidNotificationEnabled_returnsBadRequest()
            throws Exception {
        List<String> invalidBodies = List.of(
                "{}",
                "null",
                """
                        {
                          "notificationEnabled": null
                        }
                        """,
                """
                        {
                          "notificationEnabled": "false"
                        }
                        """,
                """
                        {
                          "notificationEnabled": 1
                        }
                        """
        );

        for (String invalidBody : invalidBodies) {
            mockMvc.perform(patch(activeProductNotificationSettingUrl(70L))
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("NOTIFICATION400_6"))
                    .andExpect(jsonPath("$.result").doesNotExist());
        }
    }

    @Test
    @DisplayName("개별 알림 변경 대상이 현재 회원의 활성 섭취 중 상품이 아니면 404를 반환한다")
    void updateActiveProductNotificationSetting_withUnavailableActiveProduct_returnsNotFound()
            throws Exception {
        insertMember(OTHER_MEMBER_ID, "다른회원", "2026-07-01 00:00:00");

        insertProduct(150L, "다른 회원 제품", null);
        insertProduct(151L, "중단된 제품", null);
        insertProduct(152L, "삭제된 캐비닛 제품", null);
        insertProduct(153L, "삭제된 상품", "2026-07-02 00:00:00");

        insertMemberProduct(50L, OTHER_MEMBER_ID, 150L, null);
        insertMemberProduct(51L, MEMBER_ID, 151L, null);
        insertMemberProduct(52L, MEMBER_ID, 152L, "2026-07-02 00:00:00");
        insertMemberProduct(53L, MEMBER_ID, 153L, null);

        insertActiveProduct(80L, OTHER_MEMBER_ID, 50L, null, true, "08:00", "2026-07-01 10:00:00");
        insertActiveProduct(81L, MEMBER_ID, 51L, "2026-07-10", true, "09:00", "2026-07-02 10:00:00");
        insertActiveProduct(82L, MEMBER_ID, 52L, null, true, "10:00", "2026-07-03 10:00:00");
        insertActiveProduct(83L, MEMBER_ID, 53L, null, true, "11:00", "2026-07-04 10:00:00");

        for (long activeProductId : new long[]{999L, 80L, 81L, 82L, 83L}) {
            mockMvc.perform(patch(activeProductNotificationSettingUrl(activeProductId))
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "notificationEnabled": false
                                    }
                                    """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("NOTIFICATION404_2"))
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

    @Test
    @DisplayName("인증 없이 푸시 토큰을 비활성화하면 401을 반환한다")
    void deactivatePushToken_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(delete(PUSH_TOKEN_URL + "/21"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("온보딩 미완료 회원도 본인 푸시 토큰을 비활성화할 수 있다")
    void deactivatePushToken_withoutCompletedOnboarding_deactivatesToken() throws Exception {
        insertMemberPushToken(
                31L,
                ONBOARDING_INCOMPLETE_MEMBER_ID,
                "WEB",
                "incomplete_deactivate_token",
                true,
                "2026-07-01 00:00:00"
        );

        mockMvc.perform(delete(PUSH_TOKEN_URL + "/31")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(onboardingIncompleteAccessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.result.pushTokenId").value(31))
                .andExpect(jsonPath("$.result.active").value(false));

        assertEquals(Boolean.FALSE, findPushTokenActive("incomplete_deactivate_token"));
    }

    @Test
    @DisplayName("활성 푸시 토큰을 비활성화하면 토큰 행을 삭제하지 않고 active를 false로 변경한다")
    void deactivatePushToken_withActiveToken_deactivatesTokenWithoutDeletingRow() throws Exception {
        insertMemberPushToken(32L, MEMBER_ID, "WEB", "active_deactivate_token", true, "2026-07-01 00:00:00");
        int beforeTokenCount = countMemberPushTokens();

        mockMvc.perform(delete(PUSH_TOKEN_URL + "/32")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.result.pushTokenId").value(32))
                .andExpect(jsonPath("$.result.active").value(false))
                .andExpect(jsonPath("$.result.platform").doesNotExist())
                .andExpect(jsonPath("$.result.lastSeenAt").doesNotExist())
                .andExpect(jsonPath("$.result.token").doesNotExist());

        assertEquals(beforeTokenCount, countMemberPushTokens());
        assertEquals(Boolean.FALSE, findPushTokenActive("active_deactivate_token"));
    }

    @Test
    @DisplayName("이미 비활성인 푸시 토큰을 다시 비활성화해도 성공한다")
    void deactivatePushToken_withInactiveToken_returnsSuccess() throws Exception {
        insertMemberPushToken(33L, MEMBER_ID, "WEB", "already_inactive_token", false, "2026-07-01 00:00:00");

        mockMvc.perform(delete(PUSH_TOKEN_URL + "/33")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.result.pushTokenId").value(33))
                .andExpect(jsonPath("$.result.active").value(false));

        assertEquals(Boolean.FALSE, findPushTokenActive("already_inactive_token"));
    }

    @Test
    @DisplayName("푸시 토큰 ID가 1 미만이면 400을 반환한다")
    void deactivatePushToken_withInvalidPushTokenId_returnsBadRequest() throws Exception {
        List<String> invalidPushTokenIds = List.of("0", "-1");

        for (String invalidPushTokenId : invalidPushTokenIds) {
            mockMvc.perform(delete(PUSH_TOKEN_URL + "/" + invalidPushTokenId)
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("NOTIFICATION400_4"))
                    .andExpect(jsonPath("$.result").doesNotExist());
        }
    }

    @Test
    @DisplayName("푸시 토큰 ID가 숫자 형식이 아니면 400을 반환한다")
    void deactivatePushToken_withNonNumericPushTokenId_returnsBadRequest() throws Exception {
        mockMvc.perform(delete(PUSH_TOKEN_URL + "/abc")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON400_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("존재하지 않는 푸시 토큰이면 404를 반환한다")
    void deactivatePushToken_withUnknownPushToken_returnsNotFound() throws Exception {
        mockMvc.perform(delete(PUSH_TOKEN_URL + "/34")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("NOTIFICATION404_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("다른 회원의 푸시 토큰이면 404를 반환하고 토큰 상태를 변경하지 않는다")
    void deactivatePushToken_withOtherMemberPushToken_returnsNotFoundWithoutChangingToken() throws Exception {
        insertMemberPushToken(
                35L,
                ONBOARDING_INCOMPLETE_MEMBER_ID,
                "WEB",
                "other_member_deactivate_token",
                true,
                "2026-07-01 00:00:00"
        );

        mockMvc.perform(delete(PUSH_TOKEN_URL + "/35")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("NOTIFICATION404_1"))
                .andExpect(jsonPath("$.result").doesNotExist());

        assertEquals(Boolean.TRUE, findPushTokenActive("other_member_deactivate_token"));
    }

    private void clearDatabase() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.update("DELETE FROM notification_delivery_log");
        jdbcTemplate.update("DELETE FROM member_push_token");
        jdbcTemplate.update("DELETE FROM member_notification_setting");
        jdbcTemplate.update("DELETE FROM member_active_product_schedule_history");
        jdbcTemplate.update("DELETE FROM member_active_product");
        jdbcTemplate.update("DELETE FROM member_product");
        jdbcTemplate.update("DELETE FROM product");
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

    private void insertProduct(Long id, String name, String deletedAt) {
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
                        VALUES (?, ?, '테스트브랜드', '테스트 설명', 'https://example.com/products',
                                true, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                id,
                name,
                deletedAt
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

    private void insertActiveProduct(
            Long id,
            Long memberId,
            Long memberProductId,
            String stoppedOn,
            boolean notificationEnabled,
            String intakeTime,
            String createdAt
    ) {
        jdbcTemplate.update("""
                        INSERT INTO member_active_product (
                            id,
                            member_id,
                            member_product_id,
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
                        VALUES (?, ?, ?, '2026-07-01', ?, ?, 'EVERY_DAY', 1, '2026-07-01',
                                ?, ?, CURRENT_TIMESTAMP)
                        """,
                id,
                memberId,
                memberProductId,
                stoppedOn,
                intakeTime,
                notificationEnabled,
                createdAt
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

    private Boolean findActiveProductNotificationEnabled(Long activeProductId) {
        return jdbcTemplate.queryForObject(
                "SELECT notification_enabled FROM member_active_product WHERE id = ?",
                Boolean.class,
                activeProductId
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

    private String activeProductNotificationSettingUrl(Object activeProductId) {
        return ACTIVE_PRODUCT_NOTIFICATION_SETTING_URL_PREFIX + "/" + activeProductId;
    }
}
