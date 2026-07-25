package com.ipillgood.server.domain.notification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class NotificationDeliveryServiceTest {

    private static final LocalDateTime INTAKE_SCHEDULED_AT = LocalDateTime.of(2026, 7, 24, 8, 30);
    private static final LocalDateTime CONDITION_SUNDAY_NOON = LocalDateTime.of(2026, 7, 26, 12, 0);

    @Autowired
    private NotificationDeliveryService notificationDeliveryService;

    @Autowired
    private FakePushNotificationClient pushNotificationClient;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        clearDatabase();
        pushNotificationClient.reset();
    }

    @Test
    @DisplayName("같은 시각의 복용 대상 영양제를 병합하고 활성 토큰별로 발송한다")
    void deliverIntakeNotifications_mergesSameTimeProductsAndSendsPerActiveToken() {
        insertMember(1L, "알림회원", "2026-07-01 00:00:00");
        insertMemberNotificationSetting(1L, true, true);
        insertMemberPushToken(10L, 1L, "token-a", true);
        insertMemberPushToken(11L, 1L, "token-b", true);
        insertActiveProductWithProduct(100L, 200L, 300L, 1L, "루틴A", true, "08:30", "EVERY_DAY", 1,
                "2026-07-01", "2026-07-01 08:00:00");
        insertActiveProductWithProduct(101L, 201L, 301L, 1L, "루틴B", true, "08:30", "EVERY_DAY", 1,
                "2026-07-01", "2026-07-01 08:01:00");
        insertActiveProductWithProduct(102L, 202L, 302L, 1L, "루틴C", true, "08:30", "EVERY_DAY", 1,
                "2026-07-01", "2026-07-01 08:02:00");

        notificationDeliveryService.deliverIntakeNotifications(INTAKE_SCHEDULED_AT);

        assertEquals(2, pushNotificationClient.sentPushes().size());
        String expectedBody = "8시 30분, 건강 루틴 지킬 시간!💊 루틴A, 루틴B, 루틴C 오늘도 챙겨봐요🍃";
        assertEquals(List.of("token-a", "token-b"), pushNotificationClient.sentTokens());
        assertEquals(expectedBody, pushNotificationClient.sentPushes().getFirst().payload().body());
        assertEquals("INTAKE", pushNotificationClient.sentPushes().getFirst().payload().notificationType().name());
        assertEquals("/home", pushNotificationClient.sentPushes().getFirst().payload().targetRoute());
        assertEquals(2, countDeliveryLogs());
        assertEquals("SENT", findDeliveryLogStatus(10L));
        assertEquals("SENT", findDeliveryLogStatus(11L));
        assertEquals(expectedBody, findDeliveryLogBody(10L));
    }

    @Test
    @DisplayName("복용 알림은 설정, 개별 알림, 주기, 시간을 모두 만족하는 대상만 발송한다")
    void deliverIntakeNotifications_respectsSettingsAndScheduleFilters() {
        insertIntakeCandidateMember(1L, "push-off-token", false, true, true, "08:30", "EVERY_DAY", 1,
                "2026-07-01");
        insertIntakeCandidateMember(2L, "intake-off-token", true, false, true, "08:30", "EVERY_DAY", 1,
                "2026-07-01");
        insertIntakeCandidateMember(3L, "product-off-token", true, true, false, "08:30", "EVERY_DAY", 1,
                "2026-07-01");
        insertIntakeCandidateMember(4L, "not-scheduled-token", true, true, true, "08:30", "EVERY_2_DAYS", 2,
                "2026-07-23");
        insertIntakeCandidateMember(5L, "time-mismatch-token", true, true, true, "09:00", "EVERY_DAY", 1,
                "2026-07-01");
        insertMember(6L, "기본설정회원", "2026-07-01 00:00:00");
        insertMemberPushToken(60L, 6L, "default-setting-token", true);
        insertActiveProductWithProduct(600L, 700L, 800L, 6L, "기본설정상품", true, "08:30", "EVERY_DAY", 1,
                "2026-07-01", "2026-07-01 08:00:00");

        notificationDeliveryService.deliverIntakeNotifications(INTAKE_SCHEDULED_AT);

        assertEquals(List.of("default-setting-token"), pushNotificationClient.sentTokens());
        assertEquals(1, countDeliveryLogs());
        assertEquals("SENT", findDeliveryLogStatus(60L));
    }

    @Test
    @DisplayName("복용 알림 본문이 500자를 넘으면 500자 이하로 줄여 발송하고 저장한다")
    void deliverIntakeNotifications_whenBodyExceeds500_truncatesBody() {
        insertMember(1L, "긴본문회원", "2026-07-01 00:00:00");
        insertMemberPushToken(10L, 1L, "long-body-token", true);
        String longProductName = "상품".repeat(80);
        insertActiveProductWithProduct(100L, 200L, 300L, 1L, longProductName + "A", true, "08:30", "EVERY_DAY", 1,
                "2026-07-01", "2026-07-01 08:00:00");
        insertActiveProductWithProduct(101L, 201L, 301L, 1L, longProductName + "B", true, "08:30", "EVERY_DAY", 1,
                "2026-07-01", "2026-07-01 08:01:00");
        insertActiveProductWithProduct(102L, 202L, 302L, 1L, longProductName + "C", true, "08:30", "EVERY_DAY", 1,
                "2026-07-01", "2026-07-01 08:02:00");

        notificationDeliveryService.deliverIntakeNotifications(INTAKE_SCHEDULED_AT);

        String sentBody = pushNotificationClient.sentPushes().getFirst().payload().body();
        assertTrue(sentBody.length() <= 500);
        assertTrue(findDeliveryLogBody(10L).length() <= 500);
    }

    @Test
    @DisplayName("동일 토큰/유형/예정시각 로그가 있으면 중복 발송하지 않는다")
    void deliverIntakeNotifications_withExistingDeliveryLog_skipsDuplicateSend() {
        insertMember(1L, "중복회원", "2026-07-01 00:00:00");
        insertMemberPushToken(10L, 1L, "duplicate-token", true);
        insertActiveProductWithProduct(100L, 200L, 300L, 1L, "중복상품", true, "08:30", "EVERY_DAY", 1,
                "2026-07-01", "2026-07-01 08:00:00");
        insertDeliveryLog(900L, 1L, 10L, "INTAKE", null, "기존 본문", "/home", INTAKE_SCHEDULED_AT, "SENT", 0);

        notificationDeliveryService.deliverIntakeNotifications(INTAKE_SCHEDULED_AT);

        assertEquals(0, pushNotificationClient.sentPushes().size());
        assertEquals(1, countDeliveryLogs());
    }

    @Test
    @DisplayName("일시 실패 후 재시도에 성공하면 SENT와 retry_count 1로 저장한다")
    void deliverNotification_whenRetrySucceeds_marksSentWithRetryCount() {
        insertSingleIntakeTarget("retry-success-token");
        pushNotificationClient.enqueueResults(
                "retry-success-token",
                PushSendResult.retryableFailure("UNAVAILABLE"),
                PushSendResult.success()
        );

        notificationDeliveryService.deliverIntakeNotifications(INTAKE_SCHEDULED_AT);

        assertEquals(2, pushNotificationClient.sentPushes().size());
        assertEquals("SENT", findDeliveryLogStatus(10L));
        assertEquals(1, findDeliveryLogRetryCount(10L));
        assertNotNull(findDeliveryLogSentAt(10L));
    }

    @Test
    @DisplayName("재시도까지 실패하면 RETRY_FAILED와 retry_count 1로 저장한다")
    void deliverNotification_whenRetryFails_marksRetryFailed() {
        insertSingleIntakeTarget("retry-failed-token");
        pushNotificationClient.enqueueResults(
                "retry-failed-token",
                PushSendResult.retryableFailure("UNAVAILABLE"),
                PushSendResult.retryableFailure("INTERNAL")
        );

        notificationDeliveryService.deliverIntakeNotifications(INTAKE_SCHEDULED_AT);

        assertEquals(2, pushNotificationClient.sentPushes().size());
        assertEquals("RETRY_FAILED", findDeliveryLogStatus(10L));
        assertEquals(1, findDeliveryLogRetryCount(10L));
        assertEquals("INTERNAL", findDeliveryLogFailureReason(10L));
    }

    @Test
    @DisplayName("무효 토큰 응답이면 재시도하지 않고 토큰 비활성화와 FAILED 로그를 저장한다")
    void deliverNotification_withInvalidToken_deactivatesTokenAndMarksFailed() {
        insertSingleIntakeTarget("invalid-token");
        pushNotificationClient.enqueueResults("invalid-token", PushSendResult.invalidToken("UNREGISTERED"));

        notificationDeliveryService.deliverIntakeNotifications(INTAKE_SCHEDULED_AT);

        assertEquals(1, pushNotificationClient.sentPushes().size());
        assertFalse(findPushTokenActive(10L));
        assertEquals("FAILED", findDeliveryLogStatus(10L));
        assertEquals(0, findDeliveryLogRetryCount(10L));
        assertEquals("UNREGISTERED", findDeliveryLogFailureReason(10L));
    }

    @Test
    @DisplayName("컨디션 체크 알림은 일요일 12시에 대상 회원의 활성 토큰으로 발송한다")
    void deliverConditionCheckNotifications_onSundayNoon_sendsEligibleTokens() {
        insertMember(1L, "컨디션회원", "2026-07-01 00:00:00");
        insertMemberNotificationSetting(1L, true, false);
        insertMemberPushToken(10L, 1L, "condition-token", true);
        insertMember(2L, "푸시오프회원", "2026-07-01 00:00:00");
        insertMemberNotificationSetting(2L, false, true);
        insertMemberPushToken(20L, 2L, "condition-push-off-token", true);
        insertMember(3L, "온보딩미완료", null);
        insertMemberPushToken(30L, 3L, "condition-onboarding-token", true);
        insertMember(4L, "체크완료회원", "2026-07-01 00:00:00");
        insertMemberPushToken(40L, 4L, "condition-completed-token", true);
        insertConditionWeeklyRecord(400L, 4L, "2026-07-20", "2026-07-26");

        notificationDeliveryService.deliverConditionCheckNotifications(CONDITION_SUNDAY_NOON);

        assertEquals(List.of("condition-token"), pushNotificationClient.sentTokens());
        PushNotificationPayload payload = pushNotificationClient.sentPushes().getFirst().payload();
        assertEquals("CONDITION_CHECK", payload.notificationType().name());
        assertEquals("이번 주 컨디션 체크", payload.title());
        assertEquals("이번 주 컨디션을 기록할 시간이에요.", payload.body());
        assertEquals("/condition", payload.targetRoute());
        assertEquals("SENT", findDeliveryLogStatus(10L));
    }

    @Test
    @DisplayName("컨디션 체크 알림은 일요일 12시 또는 21시가 아니면 발송하지 않는다")
    void deliverConditionCheckNotifications_whenNotDueTime_doesNotSend() {
        insertMember(1L, "컨디션회원", "2026-07-01 00:00:00");
        insertMemberPushToken(10L, 1L, "condition-token", true);

        notificationDeliveryService.deliverConditionCheckNotifications(LocalDateTime.of(2026, 7, 25, 12, 0));
        notificationDeliveryService.deliverConditionCheckNotifications(LocalDateTime.of(2026, 7, 26, 13, 0));

        assertEquals(0, pushNotificationClient.sentPushes().size());
        assertEquals(0, countDeliveryLogs());
    }

    private void insertSingleIntakeTarget(String token) {
        insertMember(1L, "단일회원", "2026-07-01 00:00:00");
        insertMemberPushToken(10L, 1L, token, true);
        insertActiveProductWithProduct(100L, 200L, 300L, 1L, "단일상품", true, "08:30", "EVERY_DAY", 1,
                "2026-07-01", "2026-07-01 08:00:00");
    }

    private void insertIntakeCandidateMember(
            Long memberId,
            String token,
            boolean pushEnabled,
            boolean intakePushEnabled,
            boolean productNotificationEnabled,
            String intakeTime,
            String frequency,
            int frequencyIntervalDays,
            String scheduleAnchorOn
    ) {
        insertMember(memberId, "회원" + memberId, "2026-07-01 00:00:00");
        insertMemberNotificationSetting(memberId, pushEnabled, intakePushEnabled);
        insertMemberPushToken(memberId * 10, memberId, token, true);
        insertActiveProductWithProduct(
                memberId * 100,
                memberId * 100 + 1,
                memberId * 100 + 2,
                memberId,
                "상품" + memberId,
                productNotificationEnabled,
                intakeTime,
                frequency,
                frequencyIntervalDays,
                scheduleAnchorOn,
                "2026-07-01 08:00:00"
        );
    }

    private void insertActiveProductWithProduct(
            Long activeProductId,
            Long memberProductId,
            Long productId,
            Long memberId,
            String productName,
            boolean notificationEnabled,
            String intakeTime,
            String frequency,
            int frequencyIntervalDays,
            String scheduleAnchorOn,
            String createdAt
    ) {
        insertProduct(productId, productName, null);
        insertMemberProduct(memberProductId, memberId, productId, null);
        insertActiveProduct(
                activeProductId,
                memberId,
                memberProductId,
                "2026-07-01",
                null,
                intakeTime,
                frequency,
                frequencyIntervalDays,
                scheduleAnchorOn,
                notificationEnabled,
                createdAt
        );
    }

    private void clearDatabase() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.update("DELETE FROM notification_delivery_log");
        jdbcTemplate.update("DELETE FROM member_push_token");
        jdbcTemplate.update("DELETE FROM member_notification_setting");
        jdbcTemplate.update("DELETE FROM condition_weekly_record");
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
                "du" + id,
                "deliveryuser" + id + "@example.com",
                onboardingCompletedAt
        );
    }

    private void insertMemberNotificationSetting(Long memberId, boolean pushEnabled, boolean intakePushEnabled) {
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
            String startedOn,
            String stoppedOn,
            String intakeTime,
            String frequency,
            int frequencyIntervalDays,
            String scheduleAnchorOn,
            boolean notificationEnabled,
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
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                        """,
                id,
                memberId,
                memberProductId,
                startedOn,
                stoppedOn,
                intakeTime,
                frequency,
                frequencyIntervalDays,
                scheduleAnchorOn,
                notificationEnabled,
                createdAt
        );
    }

    private void insertMemberPushToken(Long id, Long memberId, String token, boolean active) {
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
                        VALUES (?, ?, 'WEB', ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                id,
                memberId,
                token,
                active
        );
    }

    private void insertDeliveryLog(
            Long id,
            Long memberId,
            Long pushTokenId,
            String notificationType,
            String title,
            String body,
            String targetRoute,
            LocalDateTime scheduledAt,
            String status,
            int retryCount
    ) {
        jdbcTemplate.update("""
                        INSERT INTO notification_delivery_log (
                            id,
                            member_id,
                            member_push_token_id,
                            notification_type,
                            title,
                            body,
                            target_route,
                            scheduled_at,
                            sent_at,
                            status,
                            retry_count,
                            failure_reason,
                            created_at,
                            updated_at
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, NULL, ?, ?, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                id,
                memberId,
                pushTokenId,
                notificationType,
                title,
                body,
                targetRoute,
                scheduledAt,
                status,
                retryCount
        );
    }

    private void insertConditionWeeklyRecord(Long id, Long memberId, String weekStartOn, String weekEndOn) {
        jdbcTemplate.update("""
                        INSERT INTO condition_weekly_record (
                            id,
                            member_id,
                            week_start_on,
                            week_end_on,
                            checked_on,
                            vitality_score,
                            sleep_hours,
                            sleep_minutes,
                            sleep_score,
                            intake_days_count,
                            intake_score,
                            condition_score,
                            created_at,
                            updated_at
                        )
                        VALUES (?, ?, ?, ?, ?, 4, 7, 30, 4, 5, 4, 4.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                id,
                memberId,
                weekStartOn,
                weekEndOn,
                weekEndOn
        );
    }

    private int countDeliveryLogs() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM notification_delivery_log", Integer.class);
        return count == null ? 0 : count;
    }

    private String findDeliveryLogStatus(Long pushTokenId) {
        return jdbcTemplate.queryForObject(
                "SELECT status FROM notification_delivery_log WHERE member_push_token_id = ?",
                String.class,
                pushTokenId
        );
    }

    private String findDeliveryLogBody(Long pushTokenId) {
        return jdbcTemplate.queryForObject(
                "SELECT body FROM notification_delivery_log WHERE member_push_token_id = ?",
                String.class,
                pushTokenId
        );
    }

    private int findDeliveryLogRetryCount(Long pushTokenId) {
        Short retryCount = jdbcTemplate.queryForObject(
                "SELECT retry_count FROM notification_delivery_log WHERE member_push_token_id = ?",
                Short.class,
                pushTokenId
        );
        return retryCount == null ? 0 : retryCount;
    }

    private LocalDateTime findDeliveryLogSentAt(Long pushTokenId) {
        return jdbcTemplate.queryForObject(
                "SELECT sent_at FROM notification_delivery_log WHERE member_push_token_id = ?",
                LocalDateTime.class,
                pushTokenId
        );
    }

    private String findDeliveryLogFailureReason(Long pushTokenId) {
        return jdbcTemplate.queryForObject(
                "SELECT failure_reason FROM notification_delivery_log WHERE member_push_token_id = ?",
                String.class,
                pushTokenId
        );
    }

    private boolean findPushTokenActive(Long pushTokenId) {
        Boolean active = jdbcTemplate.queryForObject(
                "SELECT active FROM member_push_token WHERE id = ?",
                Boolean.class,
                pushTokenId
        );
        return Boolean.TRUE.equals(active);
    }

    @TestConfiguration
    static class PushNotificationClientTestConfig {

        @Bean
        @Primary
        FakePushNotificationClient fakePushNotificationClient() {
            return new FakePushNotificationClient();
        }
    }

    static class FakePushNotificationClient implements PushNotificationClient {

        private final List<SentPush> sentPushes = new ArrayList<>();
        private final Map<String, ArrayDeque<PushSendResult>> resultsByToken = new HashMap<>();

        @Override
        public PushSendResult send(String token, PushNotificationPayload payload) {
            sentPushes.add(new SentPush(token, payload));
            ArrayDeque<PushSendResult> results = resultsByToken.get(token);
            if (results == null || results.isEmpty()) {
                return PushSendResult.success();
            }
            return results.removeFirst();
        }

        void enqueueResults(String token, PushSendResult... results) {
            resultsByToken.put(token, new ArrayDeque<>(Arrays.asList(results)));
        }

        void reset() {
            sentPushes.clear();
            resultsByToken.clear();
        }

        List<SentPush> sentPushes() {
            return sentPushes;
        }

        List<String> sentTokens() {
            return sentPushes.stream()
                    .map(SentPush::token)
                    .toList();
        }
    }

    record SentPush(
            String token,
            PushNotificationPayload payload
    ) {
    }
}
