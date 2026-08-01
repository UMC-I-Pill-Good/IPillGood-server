package com.ipillgood.server.domain.intake.controller;

import com.ipillgood.server.global.security.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IntakeControllerTest {

    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final String TODAY_STATUS_URL = "/api/v1/intake/today";
    private static final String CALENDAR_URL = "/api/v1/intake/calendar";
    private static final String DAILY_TAKEN_PRODUCTS_URL_PREFIX = "/api/v1/intake/days";
    private static final String STREAK_URL = "/api/v1/intake/streak";
    private static final String TODAY_POPUP_SHOWN_URL = "/api/v1/intake/today/popup-shown";
    private static final String TODAY_RECORDS_URL = "/api/v1/intake/today/records";
    private static final String ACTIVE_PRODUCTS_URL = "/api/v1/intake/active-products";
    private static final String COMPATIBILITY_CHECKS_URL = "/api/v1/intake/compatibility-checks";
    private static final String CABINET_PRODUCTS_URL = "/api/v1/cabinet/products";
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

    @BeforeEach
    void setUp() {
        clearDatabase();

        insertMember(MEMBER_ID, "필굿", "2026-07-01 00:00:00");
        insertMember(OTHER_MEMBER_ID, "다른회원", "2026-07-01 00:00:00");
        insertMember(EMPTY_MEMBER_ID, "빈회원", "2026-07-01 00:00:00");
        insertMember(ONBOARDING_INCOMPLETE_MEMBER_ID, "미완료", null);

        insertIngredient(1L, "비타민 D", "ingredients/1.png");
        insertIngredient(2L, "비타민 C", "ingredients/2.png");
        insertIngredient(3L, "아연", "ingredients/3.png");
        insertIngredient(4L, "철", "ingredients/4.png");
        insertIngredient(5L, "마그네슘", "ingredients/5.png");
        insertIngredient(6L, "프로바이오틱스", "ingredients/6.png");

        insertProduct(100L, "비타민 D 제품", "테스트브랜드", null);
        insertProduct(101L, "멀티비타민 제품", "테스트브랜드", null);
        insertProduct(102L, "중단된 제품", "테스트브랜드", null);
        insertProduct(103L, "삭제된 캐비닛 제품", "테스트브랜드", null);
        insertProduct(104L, "삭제된 상품", "테스트브랜드", "2026-07-01 00:00:00");
        insertProduct(105L, "다른 회원 제품", "테스트브랜드", null);
        insertProduct(106L, "먼저 등록한 제품", "테스트브랜드", null);
        insertProduct(107L, "철 마그네슘 제품", "테스트브랜드", null);
        insertProduct(108L, "프로바이오틱스 제품", "테스트브랜드", null);
        insertProduct(109L, "빈회원 철 제품", "테스트브랜드", null);

        insertProductIngredient(1L, 100L, 1L);
        insertProductIngredient(2L, 101L, 1L);
        insertProductIngredient(3L, 101L, 2L);
        insertProductIngredient(4L, 102L, 2L);
        insertProductIngredient(5L, 103L, 2L);
        insertProductIngredient(6L, 104L, 2L);
        insertProductIngredient(7L, 105L, 3L);
        insertProductIngredient(8L, 106L, 3L);
        insertProductIngredient(9L, 107L, 4L);
        insertProductIngredient(10L, 107L, 5L);
        insertProductIngredient(11L, 108L, 6L);
        insertProductIngredient(12L, 109L, 4L);

        insertIngredientCombination(1L, 1L, 4L, "CAUTION", "동시 복용 시 흡수에 영향을 줄 수 있습니다.");
        insertIngredientCombination(2L, 5L, 3L, "CAUTION", "함께 복용하는 것이 권장되지 않습니다.");
        insertIngredientCombination(3L, 2L, 4L, "GOOD", "함께 섭취하면 좋습니다.");

        insertMemberProduct(1L, MEMBER_ID, 100L, "2026-07-01 10:00:00", null);
        insertMemberProduct(2L, MEMBER_ID, 101L, "2026-07-01 10:00:00", null);
        insertMemberProduct(3L, MEMBER_ID, 102L, "2026-07-01 10:00:00", null);
        insertMemberProduct(4L, MEMBER_ID, 103L, "2026-07-01 10:00:00", "2026-07-02 00:00:00");
        insertMemberProduct(5L, MEMBER_ID, 104L, "2026-07-01 10:00:00", null);
        insertMemberProduct(6L, OTHER_MEMBER_ID, 105L, "2026-07-01 10:00:00", null);
        insertMemberProduct(7L, MEMBER_ID, 106L, "2026-07-01 10:00:00", null);
        insertMemberProduct(8L, MEMBER_ID, 107L, "2026-07-01 10:00:00", null);
        insertMemberProduct(9L, MEMBER_ID, 108L, "2026-07-01 10:00:00", null);
        insertMemberProduct(16L, EMPTY_MEMBER_ID, 109L, "2026-07-01 10:00:00", null);

        insertMemberActiveProduct(10L, 1L, MEMBER_ID, null, "2026-07-02 09:00:00");
        insertMemberActiveProduct(11L, 2L, MEMBER_ID, null, "2026-07-02 09:00:00");
        insertMemberActiveProduct(12L, 3L, MEMBER_ID, "2026-07-03", "2026-07-01 09:00:00");
        insertMemberActiveProduct(13L, 4L, MEMBER_ID, null, "2026-07-01 08:00:00");
        insertMemberActiveProduct(14L, 5L, MEMBER_ID, null, "2026-07-01 08:00:00");
        insertMemberActiveProduct(15L, 6L, OTHER_MEMBER_ID, null, "2026-07-01 08:00:00");
        insertMemberActiveProduct(20L, 7L, MEMBER_ID, null, "2026-06-30 09:00:00");

        accessToken = jwtProvider.createAccessToken(MEMBER_ID, "USER", "test-session");
        emptyMemberAccessToken = jwtProvider.createAccessToken(EMPTY_MEMBER_ID, "USER", "test-session");
        onboardingIncompleteAccessToken = jwtProvider.createAccessToken(ONBOARDING_INCOMPLETE_MEMBER_ID, "USER",
                "test-session");
    }

    @Test
    @DisplayName("인증 없이 오늘 복용 상태를 조회하면 401을 반환한다")
    void getTodayIntakeStatus_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get(TODAY_STATUS_URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("온보딩을 완료하지 않은 회원은 오늘 복용 상태를 조회할 수 없다")
    void getTodayIntakeStatus_withoutCompletedOnboarding_returnsForbidden() throws Exception {
        mockMvc.perform(get(TODAY_STATUS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(onboardingIncompleteAccessToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE403_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("오늘 복용 예정 영양제가 없으면 빈 오늘 상태를 반환한다")
    void getTodayIntakeStatus_withNoScheduledProducts_returnsEmptyStatus() throws Exception {
        LocalDate currentDate = currentDate();
        int beforeIntakeDayCount = countIntakeDays();
        int beforeIntakeRecordCount = countIntakeRecords();

        mockMvc.perform(get(TODAY_STATUS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(emptyMemberAccessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.message").value("요청이 성공적으로 처리되었습니다."))
                .andExpect(jsonPath("$.result.currentDate").value(currentDate.toString()))
                .andExpect(jsonPath("$.result.scheduledCount").value(0))
                .andExpect(jsonPath("$.result.takenCount").value(0))
                .andExpect(jsonPath("$.result.allCompleted").value(false))
                .andExpect(jsonPath("$.result.missedNoticeVisible").value(false))
                .andExpect(jsonPath("$.result.autoPopupShown").value(false))
                .andExpect(jsonPath("$.result.autoPopupRequired").value(false))
                .andExpect(jsonPath("$.result.scheduledProducts.length()").value(0));

        assertEquals(beforeIntakeDayCount, countIntakeDays());
        assertEquals(beforeIntakeRecordCount, countIntakeRecords());
    }

    @Test
    @DisplayName("오늘 기록이 없으면 복용 예정 영양제를 모두 미섭취 상태로 반환하고 기록을 생성하지 않는다")
    void getTodayIntakeStatus_withoutRecords_returnsScheduledProductsAsUntaken() throws Exception {
        LocalDate currentDate = currentDate();
        int beforeIntakeDayCount = countIntakeDays();
        int beforeIntakeRecordCount = countIntakeRecords();

        mockMvc.perform(get(TODAY_STATUS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.currentDate").value(currentDate.toString()))
                .andExpect(jsonPath("$.result.scheduledCount").value(3))
                .andExpect(jsonPath("$.result.takenCount").value(0))
                .andExpect(jsonPath("$.result.allCompleted").value(false))
                .andExpect(jsonPath("$.result.missedNoticeVisible").value(true))
                .andExpect(jsonPath("$.result.autoPopupShown").value(false))
                .andExpect(jsonPath("$.result.autoPopupRequired").value(true))
                .andExpect(jsonPath("$.result.scheduledProducts.length()").value(3))
                .andExpect(jsonPath("$.result.scheduledProducts[*].activeProductId", contains(20, 10, 11)))
                .andExpect(jsonPath("$.result.scheduledProducts[*].memberProductId", contains(7, 1, 2)))
                .andExpect(jsonPath("$.result.scheduledProducts[*].productId", contains(106, 100, 101)))
                .andExpect(jsonPath("$.result.scheduledProducts[*].taken", contains(false, false, false)))
                .andExpect(jsonPath("$.result.scheduledProducts[0].takenAt").value(nullValue()))
                .andExpect(jsonPath("$.result.scheduledProducts[1].takenAt").value(nullValue()))
                .andExpect(jsonPath("$.result.scheduledProducts[2].takenAt").value(nullValue()));

        assertEquals(beforeIntakeDayCount, countIntakeDays());
        assertEquals(beforeIntakeRecordCount, countIntakeRecords());
    }

    @Test
    @DisplayName("일부 섭취 기록이 있으면 오늘 완료 수와 영양제별 섭취 상태를 반영한다")
    void getTodayIntakeStatus_withPartialRecords_returnsTakenState() throws Exception {
        LocalDate currentDate = currentDate();
        insertTodayIntakeDay(1L, MEMBER_ID, currentDate, null);
        insertTodayIntakeRecord(1L, 1L, 20L, 106L, true, currentDate + " 07:30:00");

        mockMvc.perform(get(TODAY_STATUS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.currentDate").value(currentDate.toString()))
                .andExpect(jsonPath("$.result.scheduledCount").value(3))
                .andExpect(jsonPath("$.result.takenCount").value(1))
                .andExpect(jsonPath("$.result.allCompleted").value(false))
                .andExpect(jsonPath("$.result.missedNoticeVisible").value(true))
                .andExpect(jsonPath("$.result.autoPopupShown").value(false))
                .andExpect(jsonPath("$.result.autoPopupRequired").value(true))
                .andExpect(jsonPath("$.result.scheduledProducts[*].activeProductId", contains(20, 10, 11)))
                .andExpect(jsonPath("$.result.scheduledProducts[*].taken", contains(true, false, false)))
                .andExpect(jsonPath("$.result.scheduledProducts[0].takenAt")
                        .value(currentDate + "T07:30:00"))
                .andExpect(jsonPath("$.result.scheduledProducts[1].takenAt").value(nullValue()))
                .andExpect(jsonPath("$.result.scheduledProducts[2].takenAt").value(nullValue()));
    }

    @Test
    @DisplayName("오늘 예정 영양제를 모두 섭취했으면 전체 완료 상태로 반환한다")
    void getTodayIntakeStatus_withAllCompletedRecords_returnsAllCompleted() throws Exception {
        LocalDate currentDate = currentDate();
        insertTodayIntakeDay(1L, MEMBER_ID, currentDate, null);
        insertTodayIntakeRecord(1L, 1L, 20L, 106L, true, currentDate + " 07:30:00");
        insertTodayIntakeRecord(2L, 1L, 10L, 100L, true, currentDate + " 08:30:00");
        insertTodayIntakeRecord(3L, 1L, 11L, 101L, true, currentDate + " 09:30:00");

        mockMvc.perform(get(TODAY_STATUS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.scheduledCount").value(3))
                .andExpect(jsonPath("$.result.takenCount").value(3))
                .andExpect(jsonPath("$.result.allCompleted").value(true))
                .andExpect(jsonPath("$.result.missedNoticeVisible").value(false))
                .andExpect(jsonPath("$.result.autoPopupShown").value(false))
                .andExpect(jsonPath("$.result.autoPopupRequired").value(false))
                .andExpect(jsonPath("$.result.scheduledProducts[*].taken", contains(true, true, true)));
    }

    @Test
    @DisplayName("오늘 자동 팝업 노출 기록이 있으면 자동 팝업 필요 여부를 false로 반환한다")
    void getTodayIntakeStatus_withAutoPopupShown_returnsPopupShownState() throws Exception {
        LocalDate currentDate = currentDate();
        insertTodayIntakeDay(1L, MEMBER_ID, currentDate, currentDate + " 10:00:00");

        mockMvc.perform(get(TODAY_STATUS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.scheduledCount").value(3))
                .andExpect(jsonPath("$.result.takenCount").value(0))
                .andExpect(jsonPath("$.result.allCompleted").value(false))
                .andExpect(jsonPath("$.result.missedNoticeVisible").value(true))
                .andExpect(jsonPath("$.result.autoPopupShown").value(true))
                .andExpect(jsonPath("$.result.autoPopupRequired").value(false));
    }

    @Test
    @DisplayName("주기 복용 영양제는 오늘 주기에 맞는 상품만 예정 목록에 포함한다")
    void getTodayIntakeStatus_withPeriodicProducts_returnsOnlyScheduledProducts() throws Exception {
        LocalDate currentDate = currentDate();
        changeActiveProductFrequency(11L, "EVERY_2_DAYS", 2, currentDate.minusDays(1));

        mockMvc.perform(get(TODAY_STATUS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.currentDate").value(currentDate.toString()))
                .andExpect(jsonPath("$.result.scheduledCount").value(2))
                .andExpect(jsonPath("$.result.takenCount").value(0))
                .andExpect(jsonPath("$.result.allCompleted").value(false))
                .andExpect(jsonPath("$.result.scheduledProducts.length()").value(2))
                .andExpect(jsonPath("$.result.scheduledProducts[*].activeProductId", contains(20, 10)))
                .andExpect(jsonPath("$.result.scheduledProducts[*].activeProductId", not(hasItem(11))));
    }

    @Test
    @DisplayName("인증 없이 복용 캘린더를 조회하면 401을 반환한다")
    void getIntakeCalendar_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get(CALENDAR_URL)
                        .param("year", "2026")
                        .param("month", "7"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("온보딩을 완료하지 않은 회원은 복용 캘린더를 조회할 수 없다")
    void getIntakeCalendar_withoutCompletedOnboarding_returnsForbidden() throws Exception {
        mockMvc.perform(get(CALENDAR_URL)
                        .param("year", "2026")
                        .param("month", "7")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(onboardingIncompleteAccessToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE403_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "?year=2026",
            "?month=7",
            "?year=&month=7",
            "?year=abc&month=7",
            "?year=0&month=7",
            "?year=2026&month=0",
            "?year=2026&month=13",
            "?year=2026&month=abc"
    })
    @DisplayName("복용 캘린더 조회 기간이 올바르지 않으면 400을 반환한다")
    void getIntakeCalendar_withInvalidPeriod_returnsBadRequest(String queryString) throws Exception {
        mockMvc.perform(get(CALENDAR_URL + queryString)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE400_5"))
                .andExpect(jsonPath("$.message").value("복용 캘린더 조회 기간이 올바르지 않습니다."))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("복용 캘린더는 조회 월의 전체 날짜를 반환하고 기록을 생성하지 않는다")
    void getIntakeCalendar_returnsAllDaysAndDoesNotCreateRecords() throws Exception {
        YearMonth targetMonth = YearMonth.from(currentDate().minusMonths(1));
        LocalDate firstDate = targetMonth.atDay(1);
        LocalDate lastDate = targetMonth.atEndOfMonth();
        int beforeIntakeDayCount = countIntakeDays();
        int beforeIntakeRecordCount = countIntakeRecords();

        mockMvc.perform(get(CALENDAR_URL)
                        .param("year", String.valueOf(targetMonth.getYear()))
                        .param("month", String.valueOf(targetMonth.getMonthValue()))
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(emptyMemberAccessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.message").value("요청이 성공적으로 처리되었습니다."))
                .andExpect(jsonPath("$.result.year").value(targetMonth.getYear()))
                .andExpect(jsonPath("$.result.month").value(targetMonth.getMonthValue()))
                .andExpect(jsonPath("$.result.days.length()").value(targetMonth.lengthOfMonth()))
                .andExpect(jsonPath("$.result.days[0].date").value(firstDate.toString()))
                .andExpect(jsonPath("$.result.days[0].dayOfMonth").value(1))
                .andExpect(jsonPath("$.result.days[0].streakStatus").value("EXCLUDED"))
                .andExpect(jsonPath("$.result.days[%d].date".formatted(targetMonth.lengthOfMonth() - 1))
                        .value(lastDate.toString()))
                .andExpect(jsonPath("$.result.days[%d].dayOfMonth".formatted(targetMonth.lengthOfMonth() - 1))
                        .value(targetMonth.lengthOfMonth()))
                .andExpect(jsonPath("$.result.days[%d].streakStatus".formatted(targetMonth.lengthOfMonth() - 1))
                        .value("EXCLUDED"));

        assertEquals(beforeIntakeDayCount, countIntakeDays());
        assertEquals(beforeIntakeRecordCount, countIntakeRecords());
    }

    @Test
    @DisplayName("복용 캘린더는 주기 이력 기준으로 날짜별 연속 섭취 상태를 계산한다")
    void getIntakeCalendar_withScheduleHistory_returnsDailyStreakStatuses() throws Exception {
        LocalDate currentDate = currentDate();
        LocalDate completedOn = currentDate.minusDays(3);
        LocalDate scheduleChangedOn = currentDate.minusDays(2);
        LocalDate maintainedOn = currentDate.minusDays(1);
        LocalDate upcomingOn = currentDate.plusDays(1);

        insertMemberActiveProduct(30L, 16L, EMPTY_MEMBER_ID, null, "2026-07-01 09:00:00");
        changeActiveProductFrequencyPreservingHistory(
                30L,
                "EVERY_2_DAYS",
                2,
                scheduleChangedOn
        );
        insertCalendarIntakeDay(
                101L,
                EMPTY_MEMBER_ID,
                completedOn,
                true,
                completedOn + " 08:00:00"
        );
        insertTodayIntakeRecord(101L, 101L, 30L, 109L, true, completedOn + " 08:00:00");

        performCalendarRequest(YearMonth.from(completedOn), emptyMemberAccessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.days[%d].date".formatted(completedOn.getDayOfMonth() - 1))
                        .value(completedOn.toString()))
                .andExpect(jsonPath("$.result.days[%d].allCompleted".formatted(completedOn.getDayOfMonth() - 1))
                        .value(true))
                .andExpect(jsonPath("$.result.days[%d].streakStatus".formatted(completedOn.getDayOfMonth() - 1))
                        .value("COMPLETED"))
                .andExpect(jsonPath("$.result.days[%d].streakIncluded".formatted(completedOn.getDayOfMonth() - 1))
                        .value(true))
                .andExpect(jsonPath("$.result.days[%d].selectable".formatted(completedOn.getDayOfMonth() - 1))
                        .value(true))
                .andExpect(jsonPath("$.result.days[%d].takenCount".formatted(completedOn.getDayOfMonth() - 1))
                        .value(1))
                .andExpect(jsonPath("$.result.days[%d].completedAt".formatted(completedOn.getDayOfMonth() - 1))
                        .value(completedOn + "T08:00:00"));

        performCalendarRequest(YearMonth.from(scheduleChangedOn), emptyMemberAccessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.days[%d].streakStatus".formatted(scheduleChangedOn.getDayOfMonth() - 1))
                        .value("BROKEN"))
                .andExpect(jsonPath("$.result.days[%d].streakIncluded".formatted(scheduleChangedOn.getDayOfMonth() - 1))
                        .value(false));

        performCalendarRequest(YearMonth.from(maintainedOn), emptyMemberAccessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.days[%d].streakStatus".formatted(maintainedOn.getDayOfMonth() - 1))
                        .value("MAINTAINED"))
                .andExpect(jsonPath("$.result.days[%d].streakIncluded".formatted(maintainedOn.getDayOfMonth() - 1))
                        .value(true));

        performCalendarRequest(YearMonth.from(currentDate), emptyMemberAccessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.days[%d].streakStatus".formatted(currentDate.getDayOfMonth() - 1))
                        .value("PENDING"))
                .andExpect(jsonPath("$.result.days[%d].streakIncluded".formatted(currentDate.getDayOfMonth() - 1))
                        .value(false));

        performCalendarRequest(YearMonth.from(upcomingOn), emptyMemberAccessToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.days[%d].streakStatus".formatted(upcomingOn.getDayOfMonth() - 1))
                        .value("UPCOMING"))
                .andExpect(jsonPath("$.result.days[%d].streakIncluded".formatted(upcomingOn.getDayOfMonth() - 1))
                        .value(false));
    }

    @Test
    @DisplayName("인증 없이 연속 섭취일을 조회하면 401을 반환한다")
    void getIntakeStreak_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get(STREAK_URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("온보딩을 완료하지 않은 회원은 연속 섭취일을 조회할 수 없다")
    void getIntakeStreak_withoutCompletedOnboarding_returnsForbidden() throws Exception {
        mockMvc.perform(get(STREAK_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(onboardingIncompleteAccessToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE403_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("활성 섭취 중 영양제가 없으면 연속 섭취일을 초기 상태로 반환하고 기록을 생성하지 않는다")
    void getIntakeStreak_withNoActiveProducts_returnsExcludedAndDoesNotCreateRecords() throws Exception {
        LocalDate currentDate = currentDate();
        int beforeIntakeDayCount = countIntakeDays();
        int beforeIntakeRecordCount = countIntakeRecords();

        mockMvc.perform(get(STREAK_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(emptyMemberAccessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.message").value("요청이 성공적으로 처리되었습니다."))
                .andExpect(jsonPath("$.result.currentDate").value(currentDate.toString()))
                .andExpect(jsonPath("$.result.currentDateStreakStatus").value("EXCLUDED"))
                .andExpect(jsonPath("$.result.streakDays").value(0))
                .andExpect(jsonPath("$.result.mascotStage").value("SEED"))
                .andExpect(jsonPath("$.result.mascotStageLabel").value("씨앗"))
                .andExpect(jsonPath("$.result.activeProductCount").value(0))
                .andExpect(jsonPath("$.result.lastRoutineDate").value(nullValue()))
                .andExpect(jsonPath("$.result.nextStageThresholdDays").value(7));

        assertEquals(beforeIntakeDayCount, countIntakeDays());
        assertEquals(beforeIntakeRecordCount, countIntakeRecords());
    }

    @Test
    @DisplayName("오늘 예정 영양제를 모두 완료했으면 오늘을 연속 섭취일에 포함한다")
    void getIntakeStreak_withTodayCompleted_includesToday() throws Exception {
        LocalDate currentDate = currentDate();
        insertMemberActiveProduct(30L, 16L, EMPTY_MEMBER_ID, null, "2026-07-01 09:00:00");
        insertCompletedRoutineDays(EMPTY_MEMBER_ID, 30L, 109L, currentDate, 1, 301L, 401L);

        mockMvc.perform(get(STREAK_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(emptyMemberAccessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.currentDate").value(currentDate.toString()))
                .andExpect(jsonPath("$.result.currentDateStreakStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.result.streakDays").value(1))
                .andExpect(jsonPath("$.result.mascotStage").value("SEED"))
                .andExpect(jsonPath("$.result.activeProductCount").value(1))
                .andExpect(jsonPath("$.result.lastRoutineDate").value(currentDate.toString()))
                .andExpect(jsonPath("$.result.nextStageThresholdDays").value(7));
    }

    @Test
    @DisplayName("오늘 예정 영양제를 아직 완료하지 않았으면 어제까지의 연속 섭취일을 반환한다")
    void getIntakeStreak_withTodayPending_excludesTodayAndKeepsPreviousStreak() throws Exception {
        LocalDate currentDate = currentDate();
        LocalDate yesterday = currentDate.minusDays(1);
        insertMemberActiveProduct(30L, 16L, EMPTY_MEMBER_ID, null, "2026-07-01 09:00:00");
        insertCompletedRoutineDays(EMPTY_MEMBER_ID, 30L, 109L, yesterday, 2, 301L, 401L);

        mockMvc.perform(get(STREAK_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(emptyMemberAccessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.currentDateStreakStatus").value("PENDING"))
                .andExpect(jsonPath("$.result.streakDays").value(2))
                .andExpect(jsonPath("$.result.mascotStage").value("SEED"))
                .andExpect(jsonPath("$.result.activeProductCount").value(1))
                .andExpect(jsonPath("$.result.lastRoutineDate").value(yesterday.toString()))
                .andExpect(jsonPath("$.result.nextStageThresholdDays").value(7));
    }

    @Test
    @DisplayName("오늘 예정 영양제가 없으면 오늘을 연속 섭취 유지일에 포함한다")
    void getIntakeStreak_withNoScheduledProductsToday_includesMaintainedToday() throws Exception {
        LocalDate currentDate = currentDate();
        LocalDate scheduledOn = currentDate.minusDays(1);
        insertMemberActiveProduct(30L, 16L, EMPTY_MEMBER_ID, null, "2026-07-01 09:00:00");
        changeActiveProductFrequency(30L, "EVERY_2_DAYS", 2, scheduledOn);
        insertCompletedRoutineDays(EMPTY_MEMBER_ID, 30L, 109L, scheduledOn, 1, 301L, 401L);

        mockMvc.perform(get(STREAK_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(emptyMemberAccessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.currentDateStreakStatus").value("MAINTAINED"))
                .andExpect(jsonPath("$.result.streakDays").value(2))
                .andExpect(jsonPath("$.result.mascotStage").value("SEED"))
                .andExpect(jsonPath("$.result.activeProductCount").value(1))
                .andExpect(jsonPath("$.result.lastRoutineDate").value(currentDate.toString()))
                .andExpect(jsonPath("$.result.nextStageThresholdDays").value(7));
    }

    @Test
    @DisplayName("과거 예정일 미완료가 있으면 그 날짜에서 연속 섭취일이 끊긴다")
    void getIntakeStreak_withPastBrokenDate_stopsAtBrokenDate() throws Exception {
        LocalDate currentDate = currentDate();
        LocalDate oldCompletedOn = currentDate.minusDays(3);
        insertMemberActiveProduct(30L, 16L, EMPTY_MEMBER_ID, null, "2026-07-01 09:00:00");
        insertCompletedRoutineDays(EMPTY_MEMBER_ID, 30L, 109L, currentDate, 2, 301L, 401L);
        insertCalendarIntakeDay(303L, EMPTY_MEMBER_ID, oldCompletedOn, true, oldCompletedOn + " 08:00:00");
        insertTodayIntakeRecord(403L, 303L, 30L, 109L, true, oldCompletedOn + " 08:00:00");

        mockMvc.perform(get(STREAK_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(emptyMemberAccessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.currentDateStreakStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.result.streakDays").value(2))
                .andExpect(jsonPath("$.result.activeProductCount").value(1))
                .andExpect(jsonPath("$.result.lastRoutineDate").value(currentDate.toString()));
    }

    @Test
    @DisplayName("연속 섭취일은 복용 주기 변경 이력 기준으로 과거 예정일을 판단한다")
    void getIntakeStreak_withScheduleHistory_usesHistoricalSchedule() throws Exception {
        LocalDate currentDate = currentDate();
        LocalDate scheduleChangedOn = currentDate.minusDays(1);
        insertMemberActiveProduct(30L, 16L, EMPTY_MEMBER_ID, null, "2026-07-01 09:00:00");
        changeActiveProductFrequencyPreservingHistory(
                30L,
                "EVERY_2_DAYS",
                2,
                scheduleChangedOn
        );
        insertCompletedRoutineDays(EMPTY_MEMBER_ID, 30L, 109L, scheduleChangedOn, 1, 301L, 401L);

        mockMvc.perform(get(STREAK_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(emptyMemberAccessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.currentDateStreakStatus").value("MAINTAINED"))
                .andExpect(jsonPath("$.result.streakDays").value(2))
                .andExpect(jsonPath("$.result.activeProductCount").value(1))
                .andExpect(jsonPath("$.result.lastRoutineDate").value(currentDate.toString()));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "0,SEED,씨앗,7",
            "6,SEED,씨앗,7",
            "7,SPROUT,새싹,15",
            "14,SPROUT,새싹,15",
            "15,FLOWER,꽃,30",
            "29,FLOWER,꽃,30",
            "30,FRUIT,열매,31",
            "31,TREE,나무,NULL"
    }, nullValues = "NULL")
    @DisplayName("연속 섭취일 경계값에 맞는 마스코트 성장 단계를 반환한다")
    void getIntakeStreak_returnsMascotStageByStreakDays(
            int streakDays,
            String expectedMascotStage,
            String expectedMascotStageLabel,
            Integer expectedNextStageThresholdDays
    ) throws Exception {
        LocalDate currentDate = currentDate();
        if (streakDays > 0) {
            LocalDate startedOn = currentDate.minusDays(streakDays - 1L);
            insertMemberActiveProduct(30L, 16L, EMPTY_MEMBER_ID, null, "2026-07-01 09:00:00");
            moveActiveProductStartAndCurrentSchedule(30L, startedOn);
            insertCompletedRoutineDays(EMPTY_MEMBER_ID, 30L, 109L, currentDate, streakDays, 301L, 401L);
        }

        var resultActions = mockMvc.perform(get(STREAK_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(emptyMemberAccessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.currentDate").value(currentDate.toString()))
                .andExpect(jsonPath("$.result.streakDays").value(streakDays))
                .andExpect(jsonPath("$.result.mascotStage").value(expectedMascotStage))
                .andExpect(jsonPath("$.result.mascotStageLabel").value(expectedMascotStageLabel));

        if (expectedNextStageThresholdDays == null) {
            resultActions.andExpect(jsonPath("$.result.nextStageThresholdDays").value(nullValue()));
            return;
        }
        resultActions.andExpect(jsonPath("$.result.nextStageThresholdDays")
                .value(expectedNextStageThresholdDays));
    }

    @Test
    @DisplayName("인증 없이 날짜별 섭취 완료 목록을 조회하면 401을 반환한다")
    void getDailyTakenProducts_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get(DAILY_TAKEN_PRODUCTS_URL_PREFIX + "/2026-07-02"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("온보딩을 완료하지 않은 회원은 날짜별 섭취 완료 목록을 조회할 수 없다")
    void getDailyTakenProducts_withoutCompletedOnboarding_returnsForbidden() throws Exception {
        mockMvc.perform(get(DAILY_TAKEN_PRODUCTS_URL_PREFIX + "/2026-07-02")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(onboardingIncompleteAccessToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE403_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "not-a-date",
            "2026-7-02",
            "2026-02-30",
            "0000-01-01"
    })
    @DisplayName("날짜 형식이 올바르지 않으면 날짜별 섭취 완료 목록 조회 시 400을 반환한다")
    void getDailyTakenProducts_withInvalidDate_returnsBadRequest(String date) throws Exception {
        mockMvc.perform(get(DAILY_TAKEN_PRODUCTS_URL_PREFIX + "/" + date)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE400_6"))
                .andExpect(jsonPath("$.message").value("날짜 요청이 올바르지 않습니다."))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("오늘 이후 날짜로 날짜별 섭취 완료 목록을 조회하면 400을 반환한다")
    void getDailyTakenProducts_withFutureDate_returnsBadRequest() throws Exception {
        LocalDate futureDate = currentDate().plusDays(1);

        mockMvc.perform(get(DAILY_TAKEN_PRODUCTS_URL_PREFIX + "/" + futureDate)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE400_6"))
                .andExpect(jsonPath("$.message").value("날짜 요청이 올바르지 않습니다."))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("섭취 완료 기록이 없는 날짜는 빈 목록을 반환하고 기록을 생성하지 않는다")
    void getDailyTakenProducts_withoutRecords_returnsEmptyListAndDoesNotCreateRecords() throws Exception {
        LocalDate targetDate = currentDate().minusDays(1);
        int beforeIntakeDayCount = countIntakeDays();
        int beforeIntakeRecordCount = countIntakeRecords();

        mockMvc.perform(get(DAILY_TAKEN_PRODUCTS_URL_PREFIX + "/" + targetDate)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(emptyMemberAccessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.message").value("요청이 성공적으로 처리되었습니다."))
                .andExpect(jsonPath("$.result.date").value(targetDate.toString()))
                .andExpect(jsonPath("$.result.takenCount").value(0))
                .andExpect(jsonPath("$.result.products.length()").value(0));

        assertEquals(beforeIntakeDayCount, countIntakeDays());
        assertEquals(beforeIntakeRecordCount, countIntakeRecords());
    }

    @Test
    @DisplayName("날짜별 섭취 완료 목록은 해당 회원과 날짜의 완료 기록만 섭취 일시와 상품 ID 순서로 반환한다")
    void getDailyTakenProducts_withRecords_returnsTakenProductsOnlyInOrder() throws Exception {
        LocalDate targetDate = LocalDate.of(2026, 7, 2);
        LocalDate otherDate = targetDate.minusDays(1);

        insertCalendarIntakeDay(201L, MEMBER_ID, targetDate, false, null);
        insertCalendarIntakeDay(202L, OTHER_MEMBER_ID, targetDate, true, targetDate + " 07:00:00");
        insertCalendarIntakeDay(203L, MEMBER_ID, otherDate, true, otherDate + " 06:00:00");
        insertTodayIntakeRecord(201L, 201L, 10L, 100L, true, targetDate + " 09:00:00");
        insertTodayIntakeRecord(202L, 201L, 20L, 106L, true, targetDate + " 08:00:00");
        insertTodayIntakeRecord(203L, 201L, 11L, 101L, true, targetDate + " 09:00:00");
        insertTodayIntakeRecord(204L, 201L, 12L, 102L, true, targetDate + " 10:00:00");
        insertTodayIntakeRecord(205L, 201L, 13L, 103L, false, null);
        insertTodayIntakeRecord(206L, 202L, 15L, 105L, true, targetDate + " 07:00:00");
        insertTodayIntakeRecord(207L, 203L, 20L, 106L, true, otherDate + " 06:00:00");

        mockMvc.perform(get(DAILY_TAKEN_PRODUCTS_URL_PREFIX + "/" + targetDate)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.message").value("요청이 성공적으로 처리되었습니다."))
                .andExpect(jsonPath("$.result.date").value(targetDate.toString()))
                .andExpect(jsonPath("$.result.takenCount").value(4))
                .andExpect(jsonPath("$.result.products.length()").value(4))
                .andExpect(jsonPath("$.result.products[*].activeProductId", contains(20, 10, 11, 12)))
                .andExpect(jsonPath("$.result.products[*].productId", contains(106, 100, 101, 102)))
                .andExpect(jsonPath("$.result.products[*].productName", contains(
                        "먼저 등록한 제품",
                        "비타민 D 제품",
                        "멀티비타민 제품",
                        "중단된 제품"
                )))
                .andExpect(jsonPath("$.result.products[*].takenAt", contains(
                        targetDate + "T08:00:00",
                        targetDate + "T09:00:00",
                        targetDate + "T09:00:00",
                        targetDate + "T10:00:00"
                )));
    }

    @Test
    @DisplayName("인증 없이 오늘 복용 팝업 노출 기록을 요청하면 401을 반환한다")
    void recordTodayPopupShown_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(patch(TODAY_POPUP_SHOWN_URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("온보딩을 완료하지 않은 회원은 오늘 복용 팝업 노출 기록을 요청할 수 없다")
    void recordTodayPopupShown_withoutCompletedOnboarding_returnsForbidden() throws Exception {
        mockMvc.perform(patch(TODAY_POPUP_SHOWN_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(onboardingIncompleteAccessToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE403_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("오늘 복용 예정 영양제가 없으면 팝업 노출 기록을 생성하지 않고 409를 반환한다")
    void recordTodayPopupShown_withNoScheduledProducts_returnsConflictAndDoesNotCreateIntakeDay() throws Exception {
        LocalDate currentDate = currentDate();
        int beforeIntakeDayCount = countIntakeDays();

        mockMvc.perform(patch(TODAY_POPUP_SHOWN_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(emptyMemberAccessToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE409_2"))
                .andExpect(jsonPath("$.message").value("오늘 복용 팝업 노출 대상이 아닙니다."))
                .andExpect(jsonPath("$.result").doesNotExist());

        assertEquals(beforeIntakeDayCount, countIntakeDays());
        assertEquals(0, countIntakeDays(EMPTY_MEMBER_ID, currentDate));
    }

    @Test
    @DisplayName("오늘 예정 영양제가 있고 기록 묶음이 없으면 intake_day를 생성하고 팝업 노출 일시를 저장한다")
    void recordTodayPopupShown_withoutIntakeDay_createsIntakeDayAndRecordsShownAt() throws Exception {
        LocalDate currentDate = currentDate();
        int beforeIntakeRecordCount = countIntakeRecords();

        mockMvc.perform(patch(TODAY_POPUP_SHOWN_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.message").value("요청이 성공적으로 처리되었습니다."))
                .andExpect(jsonPath("$.result.currentDate").value(currentDate.toString()))
                .andExpect(jsonPath("$.result.autoPopupShown").value(true))
                .andExpect(jsonPath("$.result.autoPopupShownAt")
                        .value(matchesPattern(currentDate + "T.+")));

        LocalDateTime autoPopupShownAt = findAutoPopupShownAt(MEMBER_ID, currentDate);
        assertNotNull(autoPopupShownAt);
        assertEquals(currentDate, autoPopupShownAt.toLocalDate());
        assertEquals(false, findAllCompleted(MEMBER_ID, currentDate));
        assertEquals(beforeIntakeRecordCount, countIntakeRecords());

        mockMvc.perform(get(TODAY_STATUS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.autoPopupShown").value(true))
                .andExpect(jsonPath("$.result.autoPopupRequired").value(false));
    }

    @Test
    @DisplayName("기존 오늘 기록 묶음이 있으면 같은 row에 팝업 노출 일시만 저장한다")
    void recordTodayPopupShown_withExistingIntakeDay_recordsShownAtOnly() throws Exception {
        LocalDate currentDate = currentDate();
        insertTodayIntakeDay(1L, MEMBER_ID, currentDate, null);
        int beforeIntakeDayCount = countIntakeDays();
        int beforeIntakeRecordCount = countIntakeRecords();

        mockMvc.perform(patch(TODAY_POPUP_SHOWN_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.currentDate").value(currentDate.toString()))
                .andExpect(jsonPath("$.result.autoPopupShown").value(true))
                .andExpect(jsonPath("$.result.autoPopupShownAt")
                        .value(matchesPattern(currentDate + "T.+")));

        LocalDateTime autoPopupShownAt = findAutoPopupShownAt(MEMBER_ID, currentDate);
        assertNotNull(autoPopupShownAt);
        assertEquals(currentDate, autoPopupShownAt.toLocalDate());
        assertEquals(beforeIntakeDayCount, countIntakeDays());
        assertEquals(beforeIntakeRecordCount, countIntakeRecords());
    }

    @Test
    @DisplayName("이미 오늘 팝업 노출 기록이 있으면 기존 일시를 유지하고 성공 응답한다")
    void recordTodayPopupShown_withAlreadyShownAt_keepsExistingShownAt() throws Exception {
        LocalDate currentDate = currentDate();
        LocalDateTime existingShownAt = LocalDateTime.parse(currentDate + "T10:00:00");
        insertTodayIntakeDay(1L, MEMBER_ID, currentDate, currentDate + " 10:00:00");
        insertTodayIntakeRecord(1L, 1L, 20L, 106L, true, currentDate + " 07:30:00");
        insertTodayIntakeRecord(2L, 1L, 10L, 100L, true, currentDate + " 08:30:00");
        insertTodayIntakeRecord(3L, 1L, 11L, 101L, true, currentDate + " 09:30:00");

        mockMvc.perform(patch(TODAY_POPUP_SHOWN_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.currentDate").value(currentDate.toString()))
                .andExpect(jsonPath("$.result.autoPopupShown").value(true))
                .andExpect(jsonPath("$.result.autoPopupShownAt").value(currentDate + "T10:00:00"));

        assertEquals(existingShownAt, findAutoPopupShownAt(MEMBER_ID, currentDate));
    }

    @Test
    @DisplayName("오늘 예정 영양제를 모두 섭취 완료했으면 팝업 노출 기록 없이 409를 반환한다")
    void recordTodayPopupShown_withAllCompletedRecords_returnsConflictAndKeepsIntakeDay() throws Exception {
        LocalDate currentDate = currentDate();
        insertTodayIntakeDay(1L, MEMBER_ID, currentDate, null);
        insertTodayIntakeRecord(1L, 1L, 20L, 106L, true, currentDate + " 07:30:00");
        insertTodayIntakeRecord(2L, 1L, 10L, 100L, true, currentDate + " 08:30:00");
        insertTodayIntakeRecord(3L, 1L, 11L, 101L, true, currentDate + " 09:30:00");
        int beforeIntakeDayCount = countIntakeDays();
        int beforeIntakeRecordCount = countIntakeRecords();

        mockMvc.perform(patch(TODAY_POPUP_SHOWN_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE409_2"))
                .andExpect(jsonPath("$.message").value("오늘 복용 팝업 노출 대상이 아닙니다."))
                .andExpect(jsonPath("$.result").doesNotExist());

        assertNull(findAutoPopupShownAt(MEMBER_ID, currentDate));
        assertEquals(beforeIntakeDayCount, countIntakeDays());
        assertEquals(beforeIntakeRecordCount, countIntakeRecords());
    }

    @Test
    @DisplayName("인증 없이 오늘 복용 체크 저장을 요청하면 401을 반환한다")
    void saveTodayIntakeRecords_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(put(TODAY_RECORDS_URL)
                        .contentType("application/json")
                        .content("""
                                {
                                  "takenActiveProductIds": [20]
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("온보딩을 완료하지 않은 회원은 오늘 복용 체크를 저장할 수 없다")
    void saveTodayIntakeRecords_withoutCompletedOnboarding_returnsForbidden() throws Exception {
        mockMvc.perform(put(TODAY_RECORDS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(onboardingIncompleteAccessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "takenActiveProductIds": [20]
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE403_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("오늘 복용 체크 저장 요청 본문이 없으면 400을 반환한다")
    void saveTodayIntakeRecords_withoutBody_returnsBadRequest() throws Exception {
        mockMvc.perform(put(TODAY_RECORDS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE400_4"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}",
            "{\"takenActiveProductIds\":null}",
            "{\"takenActiveProductIds\":[10,10]}",
            "{\"takenActiveProductIds\":[0]}",
            "{\"takenActiveProductIds\":[null]}"
    })
    @DisplayName("오늘 복용 체크 저장 요청 값이 올바르지 않으면 400을 반환한다")
    void saveTodayIntakeRecords_withInvalidRequest_returnsBadRequest(String requestBody) throws Exception {
        mockMvc.perform(put(TODAY_RECORDS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE400_4"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("오늘 복용 예정 영양제가 없으면 기록을 생성하지 않고 400을 반환한다")
    void saveTodayIntakeRecords_withNoScheduledProducts_returnsBadRequestAndDoesNotCreateRecords()
            throws Exception {
        LocalDate currentDate = currentDate();
        int beforeIntakeDayCount = countIntakeDays();
        int beforeIntakeRecordCount = countIntakeRecords();

        mockMvc.perform(put(TODAY_RECORDS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(emptyMemberAccessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "takenActiveProductIds": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE400_4"))
                .andExpect(jsonPath("$.result").doesNotExist());

        assertEquals(beforeIntakeDayCount, countIntakeDays());
        assertEquals(beforeIntakeRecordCount, countIntakeRecords());
        assertEquals(0, countIntakeDays(EMPTY_MEMBER_ID, currentDate));
    }

    @ParameterizedTest
    @ValueSource(longs = {999L, 12L, 13L, 14L, 15L})
    @DisplayName("요청 ID가 현재 회원의 활성 섭취 중 상품이 아니면 404를 반환한다")
    void saveTodayIntakeRecords_withUnavailableActiveProduct_returnsNotFound(long activeProductId)
            throws Exception {
        mockMvc.perform(put(TODAY_RECORDS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "takenActiveProductIds": [%d]
                                }
                                """.formatted(activeProductId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE404_2"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("활성 섭취 중 상품이지만 오늘 예정이 아닌 ID가 포함되면 400을 반환한다")
    void saveTodayIntakeRecords_withNotScheduledActiveProduct_returnsBadRequest() throws Exception {
        LocalDate currentDate = currentDate();
        changeActiveProductFrequency(11L, "EVERY_2_DAYS", 2, currentDate.minusDays(1));
        int beforeIntakeDayCount = countIntakeDays();
        int beforeIntakeRecordCount = countIntakeRecords();

        mockMvc.perform(put(TODAY_RECORDS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "takenActiveProductIds": [11]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE400_4"))
                .andExpect(jsonPath("$.result").doesNotExist());

        assertEquals(beforeIntakeDayCount, countIntakeDays());
        assertEquals(beforeIntakeRecordCount, countIntakeRecords());
    }

    @Test
    @DisplayName("최초 일부 체크 저장 시 오늘 기록 묶음과 예정 전체 기록을 생성한다")
    void saveTodayIntakeRecords_withPartialTakenIds_createsTodayRecords() throws Exception {
        LocalDate currentDate = currentDate();
        int beforeIntakeDayCount = countIntakeDays();
        int beforeIntakeRecordCount = countIntakeRecords();

        mockMvc.perform(put(TODAY_RECORDS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "takenActiveProductIds": [20]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.message").value("요청이 성공적으로 처리되었습니다."))
                .andExpect(jsonPath("$.result.currentDate").value(currentDate.toString()))
                .andExpect(jsonPath("$.result.scheduledCount").value(3))
                .andExpect(jsonPath("$.result.takenCount").value(1))
                .andExpect(jsonPath("$.result.allCompleted").value(false))
                .andExpect(jsonPath("$.result.completedAt").value(nullValue()))
                .andExpect(jsonPath("$.result.missedNoticeVisible").value(true))
                .andExpect(jsonPath("$.result.records.length()").value(3))
                .andExpect(jsonPath("$.result.records[*].activeProductId", contains(20, 10, 11)))
                .andExpect(jsonPath("$.result.records[*].productId", contains(106, 100, 101)))
                .andExpect(jsonPath("$.result.records[*].scheduled", contains(true, true, true)))
                .andExpect(jsonPath("$.result.records[*].taken", contains(true, false, false)))
                .andExpect(jsonPath("$.result.records[0].takenAt").value(matchesPattern(currentDate + "T.+")))
                .andExpect(jsonPath("$.result.records[1].takenAt").value(nullValue()))
                .andExpect(jsonPath("$.result.records[2].takenAt").value(nullValue()));

        assertEquals(beforeIntakeDayCount + 1, countIntakeDays());
        assertEquals(beforeIntakeRecordCount + 3, countIntakeRecords());
        assertEquals(3, countIntakeRecords(MEMBER_ID, currentDate));
        assertEquals(false, findAllCompleted(MEMBER_ID, currentDate));
        assertNull(findCompletedAt(MEMBER_ID, currentDate));
        assertEquals(true, findTodayRecordTaken(MEMBER_ID, currentDate, 20L));
        assertEquals(false, findTodayRecordTaken(MEMBER_ID, currentDate, 10L));
        assertEquals(false, findTodayRecordTaken(MEMBER_ID, currentDate, 11L));
    }

    @Test
    @DisplayName("빈 배열로 저장하면 오늘 예정 전체를 미섭취 상태로 저장한다")
    void saveTodayIntakeRecords_withEmptyTakenIds_savesAllRecordsAsUntaken() throws Exception {
        LocalDate currentDate = currentDate();

        mockMvc.perform(put(TODAY_RECORDS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "takenActiveProductIds": []
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.scheduledCount").value(3))
                .andExpect(jsonPath("$.result.takenCount").value(0))
                .andExpect(jsonPath("$.result.allCompleted").value(false))
                .andExpect(jsonPath("$.result.missedNoticeVisible").value(true))
                .andExpect(jsonPath("$.result.records[*].taken", contains(false, false, false)));

        assertEquals(3, countIntakeRecords(MEMBER_ID, currentDate));
        assertEquals(false, findTodayRecordTaken(MEMBER_ID, currentDate, 20L));
        assertEquals(false, findTodayRecordTaken(MEMBER_ID, currentDate, 10L));
        assertEquals(false, findTodayRecordTaken(MEMBER_ID, currentDate, 11L));
        assertNull(findTodayRecordTakenAt(MEMBER_ID, currentDate, 20L));
    }

    @Test
    @DisplayName("추가 체크 저장 시 기존 섭취 완료 일시는 유지하고 새 체크 항목만 현재 일시로 저장한다")
    void saveTodayIntakeRecords_withAdditionalTakenIds_keepsExistingTakenAt() throws Exception {
        LocalDate currentDate = currentDate();
        LocalDateTime existingTakenAt = LocalDateTime.parse(currentDate + "T07:30:00");
        insertTodayIntakeDay(1L, MEMBER_ID, currentDate, null);
        insertTodayIntakeRecord(1L, 1L, 20L, 106L, true, currentDate + " 07:30:00");
        insertTodayIntakeRecord(2L, 1L, 10L, 100L, false, null);
        insertTodayIntakeRecord(3L, 1L, 11L, 101L, false, null);

        mockMvc.perform(put(TODAY_RECORDS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "takenActiveProductIds": [20, 10]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.scheduledCount").value(3))
                .andExpect(jsonPath("$.result.takenCount").value(2))
                .andExpect(jsonPath("$.result.allCompleted").value(false))
                .andExpect(jsonPath("$.result.records[*].taken", contains(true, true, false)))
                .andExpect(jsonPath("$.result.records[0].takenAt").value(currentDate + "T07:30:00"))
                .andExpect(jsonPath("$.result.records[1].takenAt").value(matchesPattern(currentDate + "T.+")))
                .andExpect(jsonPath("$.result.records[2].takenAt").value(nullValue()));

        assertEquals(existingTakenAt, findTodayRecordTakenAt(MEMBER_ID, currentDate, 20L));
        LocalDateTime newTakenAt = findTodayRecordTakenAt(MEMBER_ID, currentDate, 10L);
        assertNotNull(newTakenAt);
        assertEquals(currentDate, newTakenAt.toLocalDate());
        assertNull(findTodayRecordTakenAt(MEMBER_ID, currentDate, 11L));
    }

    @Test
    @DisplayName("오늘 예정 영양제를 모두 체크하면 전체 완료 상태와 완료 일시를 저장한다")
    void saveTodayIntakeRecords_withAllTakenIds_marksDayAsCompleted() throws Exception {
        LocalDate currentDate = currentDate();
        insertTodayIntakeDay(1L, MEMBER_ID, currentDate, null);
        insertTodayIntakeRecord(1L, 1L, 20L, 106L, true, currentDate + " 07:30:00");

        mockMvc.perform(put(TODAY_RECORDS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "takenActiveProductIds": [20, 10, 11]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.scheduledCount").value(3))
                .andExpect(jsonPath("$.result.takenCount").value(3))
                .andExpect(jsonPath("$.result.allCompleted").value(true))
                .andExpect(jsonPath("$.result.completedAt").value(matchesPattern(currentDate + "T.+")))
                .andExpect(jsonPath("$.result.missedNoticeVisible").value(false))
                .andExpect(jsonPath("$.result.records[*].taken", contains(true, true, true)));

        assertEquals(true, findAllCompleted(MEMBER_ID, currentDate));
        LocalDateTime completedAt = findCompletedAt(MEMBER_ID, currentDate);
        assertNotNull(completedAt);
        assertEquals(currentDate, completedAt.toLocalDate());
        assertEquals(3, countIntakeRecords(MEMBER_ID, currentDate));
    }

    @Test
    @DisplayName("전체 완료 후 체크 해제 저장 시 미완료 상태와 null 완료 일시로 변경한다")
    void saveTodayIntakeRecords_afterAllCompletedWithUncheckedProduct_marksDayAsIncomplete() throws Exception {
        LocalDate currentDate = currentDate();

        mockMvc.perform(put(TODAY_RECORDS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "takenActiveProductIds": [20, 10, 11]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.allCompleted").value(true));

        assertNotNull(findCompletedAt(MEMBER_ID, currentDate));

        mockMvc.perform(put(TODAY_RECORDS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "takenActiveProductIds": [20, 10]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.scheduledCount").value(3))
                .andExpect(jsonPath("$.result.takenCount").value(2))
                .andExpect(jsonPath("$.result.allCompleted").value(false))
                .andExpect(jsonPath("$.result.completedAt").value(nullValue()))
                .andExpect(jsonPath("$.result.missedNoticeVisible").value(true))
                .andExpect(jsonPath("$.result.records[*].taken", contains(true, true, false)))
                .andExpect(jsonPath("$.result.records[2].takenAt").value(nullValue()));

        assertEquals(false, findAllCompleted(MEMBER_ID, currentDate));
        assertNull(findCompletedAt(MEMBER_ID, currentDate));
        assertEquals(false, findTodayRecordTaken(MEMBER_ID, currentDate, 11L));
        assertNull(findTodayRecordTakenAt(MEMBER_ID, currentDate, 11L));
    }

    @Test
    @DisplayName("오늘 복용 체크 저장은 기존 자동 팝업 노출 일시를 유지한다")
    void saveTodayIntakeRecords_withExistingAutoPopupShownAt_keepsShownAt() throws Exception {
        LocalDate currentDate = currentDate();
        LocalDateTime existingShownAt = LocalDateTime.parse(currentDate + "T10:00:00");
        insertTodayIntakeDay(1L, MEMBER_ID, currentDate, currentDate + " 10:00:00");

        mockMvc.perform(put(TODAY_RECORDS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "takenActiveProductIds": [20]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.takenCount").value(1))
                .andExpect(jsonPath("$.result.allCompleted").value(false));

        assertEquals(existingShownAt, findAutoPopupShownAt(MEMBER_ID, currentDate));
    }

    @Test
    @DisplayName("인증 없이 섭취 중 영양제 목록을 조회하면 401을 반환한다")
    void getActiveProducts_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get(ACTIVE_PRODUCTS_URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("온보딩을 완료하지 않은 회원은 섭취 중 영양제 목록을 조회할 수 없다")
    void getActiveProducts_withoutCompletedOnboarding_returnsForbidden() throws Exception {
        mockMvc.perform(get(ACTIVE_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(onboardingIncompleteAccessToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE403_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("활성 섭취 중 영양제 목록을 등록 순서와 ID 순서로 조회한다")
    void getActiveProducts_returnsOnlyAvailableActiveProductsOrderedByCreatedAtAndId() throws Exception {
        mockMvc.perform(get(ACTIVE_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.message").value("요청이 성공적으로 처리되었습니다."))
                .andExpect(jsonPath("$.result.totalCount").value(3))
                .andExpect(jsonPath("$.result.activeProducts.length()").value(3))
                .andExpect(jsonPath("$.result.activeProducts[*].activeProductId", contains(20, 10, 11)))
                .andExpect(jsonPath("$.result.activeProducts[*].memberProductId", contains(7, 1, 2)))
                .andExpect(jsonPath("$.result.activeProducts[*].productId", contains(106, 100, 101)))
                .andExpect(jsonPath("$.result.activeProducts[0].productName").value("먼저 등록한 제품"))
                .andExpect(jsonPath("$.result.activeProducts[0].thumbnailImageUrl")
                        .value("https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/ingredients/3.png"))
                .andExpect(jsonPath("$.result.activeProducts[1].productName").value("비타민 D 제품"))
                .andExpect(jsonPath("$.result.activeProducts[1].thumbnailImageUrl")
                        .value("https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/ingredients/1.png"))
                .andExpect(jsonPath("$.result.activeProducts[2].productName").value("멀티비타민 제품"))
                .andExpect(jsonPath("$.result.activeProducts[2].thumbnailImageUrl")
                        .value("https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/ingredients/other2.png"));
    }

    @Test
    @DisplayName("섭취 중 영양제가 없으면 빈 목록을 반환한다")
    void getActiveProducts_withNoActiveProducts_returnsEmptyList() throws Exception {
        mockMvc.perform(get(ACTIVE_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(emptyMemberAccessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.totalCount").value(0))
                .andExpect(jsonPath("$.result.activeProducts.length()").value(0));
    }

    @Test
    @DisplayName("인증 없이 섭취 중 영양제 등록을 요청하면 401을 반환한다")
    void registerActiveProduct_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(post(ACTIVE_PRODUCTS_URL)
                        .contentType("application/json")
                        .content("""
                                {
                                  "memberProductId": 8,
                                  "intakeTime": "08:30",
                                  "frequency": "EVERY_DAY"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("온보딩을 완료하지 않은 회원은 섭취 중 영양제를 등록할 수 없다")
    void registerActiveProduct_withoutCompletedOnboarding_returnsForbidden() throws Exception {
        mockMvc.perform(post(ACTIVE_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(onboardingIncompleteAccessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "memberProductId": 8,
                                  "intakeTime": "08:30",
                                  "frequency": "EVERY_DAY"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE403_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("섭취 중 영양제 등록 요청 본문이 없으면 400을 반환한다")
    void registerActiveProduct_withoutBody_returnsBadRequest() throws Exception {
        mockMvc.perform(post(ACTIVE_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE400_2"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("섭취 중 영양제 등록 요청의 memberProductId가 없으면 400을 반환한다")
    void registerActiveProduct_withoutMemberProductId_returnsBadRequest() throws Exception {
        mockMvc.perform(post(ACTIVE_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "intakeTime": "08:30",
                                  "frequency": "EVERY_DAY"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE400_2"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("섭취 중 영양제 등록 요청의 memberProductId가 1 미만이면 400을 반환한다")
    void registerActiveProduct_withInvalidMemberProductId_returnsBadRequest() throws Exception {
        mockMvc.perform(post(ACTIVE_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "memberProductId": 0,
                                  "intakeTime": "08:30",
                                  "frequency": "EVERY_DAY"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE400_2"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @ParameterizedTest
    @ValueSource(strings = {"08:30:00", "8:30", "24:00"})
    @DisplayName("섭취 중 영양제 등록 요청의 intakeTime 형식이 올바르지 않으면 400을 반환한다")
    void registerActiveProduct_withInvalidIntakeTime_returnsBadRequest(String intakeTime) throws Exception {
        mockMvc.perform(post(ACTIVE_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "memberProductId": 8,
                                  "intakeTime": "%s",
                                  "frequency": "EVERY_DAY"
                                }
                                """.formatted(intakeTime)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE400_2"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("섭취 중 영양제 등록 요청의 frequency가 올바르지 않으면 400을 반환한다")
    void registerActiveProduct_withInvalidFrequency_returnsBadRequest() throws Exception {
        mockMvc.perform(post(ACTIVE_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "memberProductId": 8,
                                  "intakeTime": "08:30",
                                  "frequency": "DAILY"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE400_2"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @ParameterizedTest
    @ValueSource(longs = {6L, 4L, 5L, 999L})
    @DisplayName("섭취 중 영양제 등록 대상이 현재 회원의 활성 보유 상품이 아니면 404를 반환한다")
    void registerActiveProduct_withUnavailableTarget_returnsNotFound(long memberProductId) throws Exception {
        mockMvc.perform(post(ACTIVE_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "memberProductId": %d,
                                  "intakeTime": "08:30",
                                  "frequency": "EVERY_DAY"
                                }
                                """.formatted(memberProductId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE404_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("이미 섭취 중인 캐비닛 상품이면 등록 시 409를 반환한다")
    void registerActiveProduct_withAlreadyActiveTarget_returnsConflict() throws Exception {
        LocalDate currentDate = currentDate();
        insertMemberProduct(50L, MEMBER_ID, 100L, "2026-07-01 10:00:00", currentDate + " 12:00:00");
        insertMemberActiveProduct(40L, 50L, MEMBER_ID, currentDate.toString(), "2026-07-01 09:00:00");

        mockMvc.perform(post(ACTIVE_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "memberProductId": 1,
                                  "intakeTime": "08:30",
                                  "frequency": "EVERY_DAY"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE409_1"))
                .andExpect(jsonPath("$.message").value("이미 섭취 중인 영양제입니다."))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("오늘 홈에서 섭취 중 제거된 같은 캐비닛 상품은 다시 등록할 수 없다")
    void registerActiveProduct_withTodayStoppedSameMemberProduct_returnsConflict() throws Exception {
        LocalDate currentDate = currentDate();

        mockMvc.perform(delete(activeProductUrl(10L))
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk());

        assertEquals(currentDate, findStoppedOn(10L));

        mockMvc.perform(post(ACTIVE_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "memberProductId": 1,
                                  "intakeTime": "08:30",
                                  "frequency": "EVERY_DAY"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE409_3"))
                .andExpect(jsonPath("$.message").value("오늘 삭제한 영양제는 내일부터 다시 추가할 수 있습니다."))
                .andExpect(jsonPath("$.result").doesNotExist());

        assertEquals(0, countActiveProductsByMemberProductId(1L));
    }

    @Test
    @DisplayName("오늘 캐비닛 삭제 후 같은 상품을 새 캐비닛 상품으로 추가해도 다시 등록할 수 없다")
    void registerActiveProduct_afterCabinetDeleteAndReAddSameProduct_returnsConflict() throws Exception {
        LocalDate currentDate = currentDate();

        mockMvc.perform(delete(CABINET_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "memberProductIds": [1]
                                }
                                """))
                .andExpect(status().isOk());

        assertEquals(currentDate, findStoppedOn(10L));
        insertMemberProduct(50L, MEMBER_ID, 100L, currentDate + " 12:00:00", null);

        mockMvc.perform(post(ACTIVE_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "memberProductId": 50,
                                  "intakeTime": "08:30",
                                  "frequency": "EVERY_DAY"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE409_3"))
                .andExpect(jsonPath("$.message").value("오늘 삭제한 영양제는 내일부터 다시 추가할 수 있습니다."))
                .andExpect(jsonPath("$.result").doesNotExist());

        assertEquals(0, countActiveProductsByMemberProductId(50L));
    }

    @Test
    @DisplayName("어제 이전에 중단한 동일 상품은 오늘 다시 등록하고 새 활성 섭취 상품을 생성한다")
    void registerActiveProduct_withPreviouslyStoppedSameProduct_registersNewActiveProduct() throws Exception {
        LocalDate currentDate = currentDate();
        LocalDate previousDate = currentDate.minusDays(1);
        insertMemberActiveProduct(40L, 9L, MEMBER_ID, previousDate.toString(), "2026-07-01 09:00:00");
        int beforeTotalCount = countMemberActiveProductsByMemberProductId(9L);

        mockMvc.perform(post(ACTIVE_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "memberProductId": 9,
                                  "intakeTime": "08:30",
                                  "frequency": "EVERY_DAY"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS201_1"))
                .andExpect(jsonPath("$.result.memberProductId").value(9))
                .andExpect(jsonPath("$.result.productId").value(108));

        assertEquals(beforeTotalCount + 1, countMemberActiveProductsByMemberProductId(9L));
        assertEquals(1, countActiveProductsByMemberProductId(9L));
    }

    @Test
    @DisplayName("병용 금기 충돌이 있는 캐비닛 상품도 섭취 중 영양제로 등록한다")
    void registerActiveProduct_withCompatibilityConflicts_registersActiveProduct() throws Exception {
        LocalDate currentDate = currentDate();

        mockMvc.perform(post(ACTIVE_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "memberProductId": 8,
                                  "intakeTime": "08:30",
                                  "frequency": "EVERY_2_DAYS"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS201_1"))
                .andExpect(jsonPath("$.message").value("리소스가 성공적으로 생성되었습니다."))
                .andExpect(jsonPath("$.result.activeProductId").exists())
                .andExpect(jsonPath("$.result.memberProductId").value(8))
                .andExpect(jsonPath("$.result.productId").value(107))
                .andExpect(jsonPath("$.result.productName").value("철 마그네슘 제품"))
                .andExpect(jsonPath("$.result.thumbnailImageUrl")
                        .value("https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/ingredients/other4.png"))
                .andExpect(jsonPath("$.result.notificationEnabled").value(true))
                .andExpect(jsonPath("$.result.intakeTime").value("08:30"))
                .andExpect(jsonPath("$.result.frequency").value("EVERY_2_DAYS"))
                .andExpect(jsonPath("$.result.frequencyLabel").value("2일에 한 번"));

        Integer activeProductCount = jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM member_active_product
                        WHERE member_product_id = ?
                          AND member_id = ?
                          AND stopped_on IS NULL
                        """,
                Integer.class,
                8L,
                MEMBER_ID
        );
        assertEquals(1, activeProductCount);

        Long activeProductId = jdbcTemplate.queryForObject("""
                        SELECT id
                        FROM member_active_product
                        WHERE member_product_id = ?
                          AND member_id = ?
                          AND stopped_on IS NULL
                        """,
                Long.class,
                8L,
                MEMBER_ID
        );
        assertNotNull(activeProductId);

        LocalDate startedOn = jdbcTemplate.queryForObject("""
                        SELECT started_on
                        FROM member_active_product
                        WHERE id = ?
                        """,
                LocalDate.class,
                activeProductId
        );
        assertEquals(currentDate, startedOn);

        Integer scheduleHistoryCount = jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM member_active_product_schedule_history
                        WHERE member_active_product_id = ?
                          AND frequency = 'EVERY_2_DAYS'
                          AND frequency_interval_days = 2
                          AND schedule_anchor_on = ?
                          AND effective_from = ?
                          AND effective_to IS NULL
                        """,
                Integer.class,
                activeProductId,
                currentDate,
                currentDate
        );
        assertEquals(1, scheduleHistoryCount);
    }

    @Test
    @DisplayName("기존 오늘 예정 영양제를 모두 완료한 뒤 새 영양제를 등록하면 오늘 완료 상태를 미완료로 재계산한다")
    void registerActiveProduct_afterAllScheduledProductsCompleted_marksTodayAsIncomplete() throws Exception {
        LocalDate currentDate = currentDate();
        changeActiveProductFrequency(11L, "EVERY_2_DAYS", 2, currentDate.minusDays(1));
        insertCalendarIntakeDay(1L, MEMBER_ID, currentDate, true, currentDate + " 10:00:00");
        insertTodayIntakeRecord(1L, 1L, 20L, 106L, true, currentDate + " 07:30:00");
        insertTodayIntakeRecord(2L, 1L, 10L, 100L, true, currentDate + " 08:30:00");

        mockMvc.perform(post(ACTIVE_PRODUCTS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "memberProductId": 8,
                                  "intakeTime": "08:30",
                                  "frequency": "EVERY_DAY"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.memberProductId").value(8))
                .andExpect(jsonPath("$.result.productId").value(107));

        assertEquals(false, findAllCompleted(MEMBER_ID, currentDate));
        assertNull(findCompletedAt(MEMBER_ID, currentDate));
        assertEquals(2, countIntakeRecords(MEMBER_ID, currentDate));
    }

    @Test
    @DisplayName("인증 없이 섭취 중 영양제 설정 변경을 요청하면 401을 반환한다")
    void updateActiveProductSettings_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(patch(activeProductUrl(10L))
                        .contentType("application/json")
                        .content("""
                                {
                                  "notificationEnabled": false
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("온보딩을 완료하지 않은 회원은 섭취 중 영양제 설정을 변경할 수 없다")
    void updateActiveProductSettings_withoutCompletedOnboarding_returnsForbidden() throws Exception {
        mockMvc.perform(patch(activeProductUrl(10L))
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(onboardingIncompleteAccessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "notificationEnabled": false
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE403_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("복용 시간과 개별 알림만 변경하면 주기 기준일과 이력은 유지한다")
    void updateActiveProductSettings_withTimeAndNotification_updatesOnlyRequestedSettings() throws Exception {
        LocalDate currentDate = currentDate();
        int intakeDayCount = toIntakeDayCount(LocalDate.of(2026, 7, 1), currentDate);

        mockMvc.perform(patch(activeProductUrl(10L))
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "intakeTime": "21:00",
                                  "notificationEnabled": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.message").value("요청이 성공적으로 처리되었습니다."))
                .andExpect(jsonPath("$.result.activeProductId").value(10))
                .andExpect(jsonPath("$.result.memberProductId").value(1))
                .andExpect(jsonPath("$.result.productId").value(100))
                .andExpect(jsonPath("$.result.brand").value("테스트브랜드"))
                .andExpect(jsonPath("$.result.productName").value("비타민 D 제품"))
                .andExpect(jsonPath("$.result.thumbnailImageUrl")
                        .value("https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/ingredients/1.png"))
                .andExpect(jsonPath("$.result.startedOn").value("2026-07-01"))
                .andExpect(jsonPath("$.result.intakeDayCount").value(intakeDayCount))
                .andExpect(jsonPath("$.result.notificationEnabled").value(false))
                .andExpect(jsonPath("$.result.intakeTime").value("21:00"))
                .andExpect(jsonPath("$.result.frequency").value("EVERY_DAY"))
                .andExpect(jsonPath("$.result.frequencyLabel").value("매일"))
                .andExpect(jsonPath("$.result.frequencyIntervalDays").value(1))
                .andExpect(jsonPath("$.result.scheduleAnchorOn").value("2026-07-01"));

        assertEquals(LocalTime.of(21, 0), findIntakeTime(10L));
        assertEquals(false, findNotificationEnabled(10L));
        assertEquals("EVERY_DAY", findActiveProductFrequency(10L));
        assertEquals((short) 1, findFrequencyIntervalDays(10L));
        assertEquals(LocalDate.of(2026, 7, 1), findScheduleAnchorOn(10L));
        assertEquals(1, countScheduleHistories(10L));
        assertEquals(1, countActiveScheduleHistories(10L));
    }

    @Test
    @DisplayName("복수 성분 섭취 중 영양제 설정 변경은 productId 기준 기타 썸네일을 반환한다")
    void updateActiveProductSettings_withMultiIngredientProduct_returnsProductIdBasedThumbnail() throws Exception {
        LocalDate currentDate = currentDate();
        int intakeDayCount = toIntakeDayCount(LocalDate.of(2026, 7, 1), currentDate);

        mockMvc.perform(patch(activeProductUrl(11L))
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "notificationEnabled": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.activeProductId").value(11))
                .andExpect(jsonPath("$.result.memberProductId").value(2))
                .andExpect(jsonPath("$.result.productId").value(101))
                .andExpect(jsonPath("$.result.brand").value("테스트브랜드"))
                .andExpect(jsonPath("$.result.productName").value("멀티비타민 제품"))
                .andExpect(jsonPath("$.result.thumbnailImageUrl")
                        .value("https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com/ingredients/other2.png"))
                .andExpect(jsonPath("$.result.startedOn").value("2026-07-01"))
                .andExpect(jsonPath("$.result.intakeDayCount").value(intakeDayCount))
                .andExpect(jsonPath("$.result.notificationEnabled").value(false))
                .andExpect(jsonPath("$.result.intakeTime").value("09:00"))
                .andExpect(jsonPath("$.result.frequency").value("EVERY_DAY"))
                .andExpect(jsonPath("$.result.frequencyLabel").value("매일"))
                .andExpect(jsonPath("$.result.frequencyIntervalDays").value(1))
                .andExpect(jsonPath("$.result.scheduleAnchorOn").value("2026-07-01"));

        assertEquals(false, findNotificationEnabled(11L));
    }

    @Test
    @DisplayName("복용 주기를 변경하면 기준일을 오늘로 갱신하고 새 주기 이력을 생성한다")
    void updateActiveProductSettings_withDifferentFrequency_updatesAnchorAndCreatesHistory() throws Exception {
        LocalDate currentDate = currentDate();

        mockMvc.perform(patch(activeProductUrl(10L))
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "frequency": "EVERY_2_DAYS"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.activeProductId").value(10))
                .andExpect(jsonPath("$.result.frequency").value("EVERY_2_DAYS"))
                .andExpect(jsonPath("$.result.frequencyLabel").value("2일에 한 번"))
                .andExpect(jsonPath("$.result.frequencyIntervalDays").value(2))
                .andExpect(jsonPath("$.result.scheduleAnchorOn").value(currentDate.toString()));

        assertEquals("EVERY_2_DAYS", findActiveProductFrequency(10L));
        assertEquals((short) 2, findFrequencyIntervalDays(10L));
        assertEquals(currentDate, findScheduleAnchorOn(10L));
        assertEquals(2, countScheduleHistories(10L));
        assertEquals(1, countClosedScheduleHistories(10L, "EVERY_DAY", currentDate));
        assertEquals(1, countActiveScheduleHistories(10L, "EVERY_2_DAYS"));
    }

    @Test
    @DisplayName("복용 주기 변경으로 오늘 예정 영양제가 늘어나면 오늘 완료 상태를 미완료로 재계산한다")
    void updateActiveProductSettings_whenTodayScheduledStatusChanges_marksTodayAsIncomplete()
            throws Exception {
        LocalDate currentDate = currentDate();
        changeActiveProductFrequency(10L, "EVERY_2_DAYS", 2, currentDate.minusDays(1));
        changeActiveProductFrequency(11L, "EVERY_2_DAYS", 2, currentDate.minusDays(1));
        insertCalendarIntakeDay(1L, MEMBER_ID, currentDate, true, currentDate + " 10:00:00");
        insertTodayIntakeRecord(1L, 1L, 20L, 106L, true, currentDate + " 07:30:00");

        mockMvc.perform(patch(activeProductUrl(10L))
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "frequency": "EVERY_3_DAYS"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.frequency").value("EVERY_3_DAYS"))
                .andExpect(jsonPath("$.result.scheduleAnchorOn").value(currentDate.toString()));

        assertEquals(false, findAllCompleted(MEMBER_ID, currentDate));
        assertNull(findCompletedAt(MEMBER_ID, currentDate));
        assertEquals(1, countIntakeRecords(MEMBER_ID, currentDate));
    }

    @Test
    @DisplayName("같은 날짜에 복용 주기를 다시 변경하면 오늘 시작 이력을 새 값으로 갱신한다")
    void updateActiveProductSettings_withSecondFrequencyChangeOnSameDay_updatesCurrentHistory() throws Exception {
        LocalDate currentDate = currentDate();

        mockMvc.perform(patch(activeProductUrl(10L))
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "frequency": "EVERY_2_DAYS"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(patch(activeProductUrl(10L))
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "frequency": "WEEKLY"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.frequency").value("WEEKLY"))
                .andExpect(jsonPath("$.result.frequencyLabel").value("일주일에 한 번"))
                .andExpect(jsonPath("$.result.frequencyIntervalDays").value(7))
                .andExpect(jsonPath("$.result.scheduleAnchorOn").value(currentDate.toString()));

        assertEquals("WEEKLY", findActiveProductFrequency(10L));
        assertEquals((short) 7, findFrequencyIntervalDays(10L));
        assertEquals(currentDate, findScheduleAnchorOn(10L));
        assertEquals(2, countScheduleHistories(10L));
        assertEquals(1, countClosedScheduleHistories(10L, "EVERY_DAY", currentDate));
        assertEquals(1, countActiveScheduleHistories(10L, "WEEKLY"));
        assertEquals(0, countActiveScheduleHistories(10L, "EVERY_2_DAYS"));
    }

    @Test
    @DisplayName("기존 복용 주기와 같은 주기를 요청하면 기준일과 주기 이력을 유지한다")
    void updateActiveProductSettings_withSameFrequency_keepsAnchorAndHistory() throws Exception {
        mockMvc.perform(patch(activeProductUrl(10L))
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "frequency": "EVERY_DAY"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.frequency").value("EVERY_DAY"))
                .andExpect(jsonPath("$.result.frequencyIntervalDays").value(1))
                .andExpect(jsonPath("$.result.scheduleAnchorOn").value("2026-07-01"));

        assertEquals("EVERY_DAY", findActiveProductFrequency(10L));
        assertEquals(LocalDate.of(2026, 7, 1), findScheduleAnchorOn(10L));
        assertEquals(1, countScheduleHistories(10L));
        assertEquals(1, countActiveScheduleHistories(10L, "EVERY_DAY"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "abc"})
    @DisplayName("activeProductId가 올바르지 않으면 400을 반환한다")
    void updateActiveProductSettings_withInvalidActiveProductId_returnsBadRequest(String activeProductId)
            throws Exception {
        mockMvc.perform(patch(activeProductUrl(activeProductId))
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "notificationEnabled": false
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE400_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("섭취 중 영양제 설정 변경 요청 본문이 없으면 400을 반환한다")
    void updateActiveProductSettings_withoutBody_returnsBadRequest() throws Exception {
        mockMvc.perform(patch(activeProductUrl(10L))
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE400_3"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}",
            "{\"intakeTime\":null,\"frequency\":null,\"notificationEnabled\":null}"
    })
    @DisplayName("설정 변경 요청에 변경 대상 필드가 없으면 400을 반환한다")
    void updateActiveProductSettings_withoutUpdateFields_returnsBadRequest(String requestBody) throws Exception {
        mockMvc.perform(patch(activeProductUrl(10L))
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE400_3"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @ParameterizedTest
    @ValueSource(strings = {"08:30:00", "8:30", "24:00"})
    @DisplayName("설정 변경 요청의 intakeTime 형식이 올바르지 않으면 400을 반환한다")
    void updateActiveProductSettings_withInvalidIntakeTime_returnsBadRequest(String intakeTime) throws Exception {
        mockMvc.perform(patch(activeProductUrl(10L))
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "intakeTime": "%s"
                                }
                                """.formatted(intakeTime)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE400_3"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("설정 변경 요청의 frequency가 올바르지 않으면 400을 반환한다")
    void updateActiveProductSettings_withInvalidFrequency_returnsBadRequest() throws Exception {
        mockMvc.perform(patch(activeProductUrl(10L))
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "frequency": "DAILY"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE400_3"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("설정 변경 대상이 현재 회원의 활성 섭취 중 상품이 아니면 404를 반환한다")
    void updateActiveProductSettings_withUnavailableActiveProduct_returnsNotFound() throws Exception {
        for (long activeProductId : new long[]{999L, 12L, 13L, 14L, 15L}) {
            mockMvc.perform(patch(activeProductUrl(activeProductId))
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                            .contentType("application/json")
                            .content("""
                                    {
                                      "notificationEnabled": false
                                    }
                                    """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("INTAKE404_2"))
                    .andExpect(jsonPath("$.result").doesNotExist());
        }
    }

    @Test
    @DisplayName("섭취 중 영양제를 제거하고 주기 이력을 닫는다")
    void removeActiveProduct_withActiveProduct_returnsOk() throws Exception {
        LocalDate currentDate = currentDate();
        insertIntakeDay(1L, MEMBER_ID, "2026-07-20");
        insertIntakeRecord(1L, 1L, 10L, 100L);
        int beforeIntakeDayCount = countIntakeDays();
        int beforeIntakeRecordCount = countIntakeRecords();

        mockMvc.perform(delete(activeProductUrl(10L))
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.message").value("요청이 성공적으로 처리되었습니다."))
                .andExpect(jsonPath("$.result.activeProductId").value(10))
                .andExpect(jsonPath("$.result.memberProductId").value(1))
                .andExpect(jsonPath("$.result.productId").value(100))
                .andExpect(jsonPath("$.result.productName").value("비타민 D 제품"))
                .andExpect(jsonPath("$.result.stoppedOn").value(currentDate.toString()));

        assertEquals(currentDate, findStoppedOn(10L));
        assertEquals(0, countActiveScheduleHistories(10L));
        assertEquals(1, countClosedScheduleHistories(10L, "EVERY_DAY", currentDate));
        assertEquals(0, countDeletedMemberProduct(1L));
        assertEquals(beforeIntakeDayCount, countIntakeDays());
        assertEquals(beforeIntakeRecordCount, countIntakeRecords());
    }

    @Test
    @DisplayName("일부 완료 후 미완료 영양제를 제거하면 남은 오늘 예정 기준으로 완료 상태를 true로 재계산한다")
    void removeActiveProduct_afterOnlyRemainingProductTaken_marksTodayAsCompleted() throws Exception {
        LocalDate currentDate = currentDate();
        changeActiveProductFrequency(11L, "EVERY_2_DAYS", 2, currentDate.minusDays(1));
        insertCalendarIntakeDay(1L, MEMBER_ID, currentDate, false, null);
        insertTodayIntakeRecord(1L, 1L, 20L, 106L, true, currentDate + " 07:30:00");
        insertTodayIntakeRecord(2L, 1L, 10L, 100L, false, null);

        mockMvc.perform(delete(activeProductUrl(10L))
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true));

        assertEquals(currentDate, findStoppedOn(10L));
        assertEquals(true, findAllCompleted(MEMBER_ID, currentDate));
        LocalDateTime completedAt = findCompletedAt(MEMBER_ID, currentDate);
        assertNotNull(completedAt);
        assertEquals(currentDate, completedAt.toLocalDate());
        assertEquals(2, countIntakeRecords(MEMBER_ID, currentDate));
        assertEquals(false, findTodayRecordTaken(MEMBER_ID, currentDate, 10L));
    }

    @Test
    @DisplayName("오늘 예정 영양제를 모두 제거하면 완료 상태를 false와 null 완료 일시로 재계산한다")
    void removeActiveProduct_whenNoScheduledProductsRemain_marksTodayAsIncomplete() throws Exception {
        LocalDate currentDate = currentDate();
        changeActiveProductFrequency(20L, "EVERY_2_DAYS", 2, currentDate.minusDays(1));
        changeActiveProductFrequency(11L, "EVERY_2_DAYS", 2, currentDate.minusDays(1));
        insertCalendarIntakeDay(1L, MEMBER_ID, currentDate, true, currentDate + " 10:00:00");
        insertTodayIntakeRecord(1L, 1L, 10L, 100L, true, currentDate + " 08:30:00");

        mockMvc.perform(delete(activeProductUrl(10L))
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true));

        assertEquals(currentDate, findStoppedOn(10L));
        assertEquals(false, findAllCompleted(MEMBER_ID, currentDate));
        assertNull(findCompletedAt(MEMBER_ID, currentDate));
        assertEquals(1, countIntakeRecords(MEMBER_ID, currentDate));
        assertEquals(true, findTodayRecordTaken(MEMBER_ID, currentDate, 10L));
    }

    @Test
    @DisplayName("인증 없이 섭취 중 영양제 제거를 요청하면 401을 반환한다")
    void removeActiveProduct_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(delete(activeProductUrl(10L)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("온보딩을 완료하지 않은 회원은 섭취 중 영양제를 제거할 수 없다")
    void removeActiveProduct_withoutCompletedOnboarding_returnsForbidden() throws Exception {
        mockMvc.perform(delete(activeProductUrl(10L))
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(onboardingIncompleteAccessToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE403_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "abc"})
    @DisplayName("제거할 activeProductId가 올바르지 않으면 400을 반환한다")
    void removeActiveProduct_withInvalidActiveProductId_returnsBadRequest(String activeProductId) throws Exception {
        mockMvc.perform(delete(activeProductUrl(activeProductId))
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE400_1"))
                .andExpect(jsonPath("$.result").doesNotExist());

        assertEquals(0, countStoppedActiveProduct(10L));
        assertEquals(1, countActiveScheduleHistories(10L));
    }

    @Test
    @DisplayName("제거 대상이 현재 회원의 활성 섭취 중 상품이 아니면 404를 반환한다")
    void removeActiveProduct_withUnavailableActiveProduct_returnsNotFound() throws Exception {
        for (long activeProductId : new long[]{999L, 12L, 13L, 14L, 15L}) {
            mockMvc.perform(delete(activeProductUrl(activeProductId))
                            .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("INTAKE404_2"))
                    .andExpect(jsonPath("$.result").doesNotExist());
        }

        assertEquals(0, countStoppedActiveProduct(10L));
        assertEquals(1, countActiveScheduleHistories(10L));
    }

    @Test
    @DisplayName("인증 없이 병용 금기 확인을 요청하면 401을 반환한다")
    void checkCompatibility_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(post(COMPATIBILITY_CHECKS_URL)
                        .contentType("application/json")
                        .content("""
                                {
                                  "memberProductId": 8
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("온보딩을 완료하지 않은 회원은 병용 금기 확인을 요청할 수 없다")
    void checkCompatibility_withoutCompletedOnboarding_returnsForbidden() throws Exception {
        mockMvc.perform(post(COMPATIBILITY_CHECKS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(onboardingIncompleteAccessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "memberProductId": 8
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE403_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("병용 금기 확인 요청 본문이 없으면 400을 반환한다")
    void checkCompatibility_withoutBody_returnsBadRequest() throws Exception {
        mockMvc.perform(post(COMPATIBILITY_CHECKS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE400_2"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("memberProductId가 없으면 400을 반환한다")
    void checkCompatibility_withoutMemberProductId_returnsBadRequest() throws Exception {
        mockMvc.perform(post(COMPATIBILITY_CHECKS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE400_2"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("memberProductId가 1 미만이면 400을 반환한다")
    void checkCompatibility_withInvalidMemberProductId_returnsBadRequest() throws Exception {
        mockMvc.perform(post(COMPATIBILITY_CHECKS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "memberProductId": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE400_2"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("등록 대상 캐비닛 상품이 현재 회원의 활성 보유 상품이 아니면 404를 반환한다")
    void checkCompatibility_withUnavailableTarget_returnsNotFound() throws Exception {
        mockMvc.perform(post(COMPATIBILITY_CHECKS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "memberProductId": 6
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE404_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("등록 대상 캐비닛 상품이 삭제되었으면 404를 반환한다")
    void checkCompatibility_withDeletedCabinetProduct_returnsNotFound() throws Exception {
        mockMvc.perform(post(COMPATIBILITY_CHECKS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "memberProductId": 4
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE404_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("등록 대상 상품이 삭제되었으면 404를 반환한다")
    void checkCompatibility_withDeletedProduct_returnsNotFound() throws Exception {
        mockMvc.perform(post(COMPATIBILITY_CHECKS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "memberProductId": 5
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE404_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("등록 대상 캐비닛 상품이 없으면 404를 반환한다")
    void checkCompatibility_withUnknownTarget_returnsNotFound() throws Exception {
        mockMvc.perform(post(COMPATIBILITY_CHECKS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "memberProductId": 999
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE404_1"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("이미 섭취 중인 캐비닛 상품이면 409를 반환한다")
    void checkCompatibility_withAlreadyActiveTarget_returnsConflict() throws Exception {
        LocalDate currentDate = currentDate();
        insertMemberProduct(50L, MEMBER_ID, 100L, "2026-07-01 10:00:00", currentDate + " 12:00:00");
        insertMemberActiveProduct(40L, 50L, MEMBER_ID, currentDate.toString(), "2026-07-01 09:00:00");

        mockMvc.perform(post(COMPATIBILITY_CHECKS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "memberProductId": 1
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE409_1"))
                .andExpect(jsonPath("$.message").value("이미 섭취 중인 영양제입니다."))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("오늘 중단한 동일 상품이면 병용 금기 확인도 409를 반환한다")
    void checkCompatibility_withTodayStoppedSameProduct_returnsConflict() throws Exception {
        LocalDate currentDate = currentDate();
        insertMemberActiveProduct(40L, 9L, MEMBER_ID, currentDate.toString(), "2026-07-01 09:00:00");

        mockMvc.perform(post(COMPATIBILITY_CHECKS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "memberProductId": 9
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE409_3"))
                .andExpect(jsonPath("$.message").value("오늘 삭제한 영양제는 내일부터 다시 추가할 수 있습니다."))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("당일 재등록 제한 대상이면 병용 금기 충돌보다 409가 우선한다")
    void checkCompatibility_withTodayStoppedSameProductAndConflicts_returnsConflictBeforeConflicts()
            throws Exception {
        LocalDate currentDate = currentDate();
        insertMemberActiveProduct(40L, 8L, MEMBER_ID, currentDate.toString(), "2026-07-01 09:00:00");

        mockMvc.perform(post(COMPATIBILITY_CHECKS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "memberProductId": 8
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("INTAKE409_3"))
                .andExpect(jsonPath("$.message").value("오늘 삭제한 영양제는 내일부터 다시 추가할 수 있습니다."))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("활성 섭취 중 상품과 등록 대상 상품의 주의/금기 성분 조합을 조회한다")
    void checkCompatibility_withConflicts_returnsWarningsOnly() throws Exception {
        mockMvc.perform(post(COMPATIBILITY_CHECKS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "memberProductId": 8
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS200_1"))
                .andExpect(jsonPath("$.message").value("요청이 성공적으로 처리되었습니다."))
                .andExpect(jsonPath("$.result.hasConflicts").value(true))
                .andExpect(jsonPath("$.result.conflicts.length()").value(2))
                .andExpect(jsonPath("$.result.conflicts[*].combinationType",
                        contains("CAUTION", "CAUTION")))
                .andExpect(jsonPath("$.result.conflicts[*].combinationType", not(hasItem("GOOD"))))
                .andExpect(jsonPath("$.result.conflicts[0].currentIngredientId").value(1))
                .andExpect(jsonPath("$.result.conflicts[0].currentIngredientName").value("비타민 D"))
                .andExpect(jsonPath("$.result.conflicts[0].targetIngredientId").value(4))
                .andExpect(jsonPath("$.result.conflicts[0].targetIngredientName").value("철"))
                .andExpect(jsonPath("$.result.conflicts[0].reason")
                        .value("동시 복용 시 흡수에 영향을 줄 수 있습니다."))
                .andExpect(jsonPath("$.result.conflicts[1].currentIngredientId").value(3))
                .andExpect(jsonPath("$.result.conflicts[1].currentIngredientName").value("아연"))
                .andExpect(jsonPath("$.result.conflicts[1].targetIngredientId").value(5))
                .andExpect(jsonPath("$.result.conflicts[1].targetIngredientName").value("마그네슘"))
                .andExpect(jsonPath("$.result.conflicts[1].reason")
                        .value("함께 복용하는 것이 권장되지 않습니다."));
    }

    @Test
    @DisplayName("병용 금기 조합이 없으면 빈 목록을 반환한다")
    void checkCompatibility_withNoMatchingConflicts_returnsEmptyConflicts() throws Exception {
        mockMvc.perform(post(COMPATIBILITY_CHECKS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "memberProductId": 9
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.hasConflicts").value(false))
                .andExpect(jsonPath("$.result.conflicts.length()").value(0));
    }

    @Test
    @DisplayName("현재 활성 섭취 중 상품이 없으면 빈 목록을 반환한다")
    void checkCompatibility_withNoActiveProducts_returnsEmptyConflicts() throws Exception {
        mockMvc.perform(post(COMPATIBILITY_CHECKS_URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(emptyMemberAccessToken))
                        .contentType("application/json")
                        .content("""
                                {
                                  "memberProductId": 16
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.hasConflicts").value(false))
                .andExpect(jsonPath("$.result.conflicts.length()").value(0));
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

    private void insertMemberActiveProduct(
            Long id,
            Long memberProductId,
            Long memberId,
            String stoppedOn,
            String createdAt
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
                        VALUES (?, ?, ?, '2026-07-01', ?, '09:00:00', 'EVERY_DAY', 1,
                                '2026-07-01', true, NULL, ?, ?)
                        """,
                id,
                memberProductId,
                memberId,
                stoppedOn,
                createdAt,
                createdAt
        );
        insertMemberActiveProductScheduleHistory(
                id,
                "EVERY_DAY",
                1,
                "2026-07-01",
                "2026-07-01",
                stoppedOn
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

    private void changeActiveProductFrequency(
            Long activeProductId,
            String frequency,
            int frequencyIntervalDays,
            LocalDate scheduleAnchorOn
    ) {
        jdbcTemplate.update("""
                        UPDATE member_active_product
                        SET frequency = ?,
                            frequency_interval_days = ?,
                            schedule_anchor_on = ?,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                frequency,
                frequencyIntervalDays,
                scheduleAnchorOn,
                activeProductId
        );
        jdbcTemplate.update("""
                        UPDATE member_active_product_schedule_history
                        SET frequency = ?,
                            frequency_interval_days = ?,
                            schedule_anchor_on = ?,
                            effective_from = ?,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE member_active_product_id = ?
                          AND effective_to IS NULL
                        """,
                frequency,
                frequencyIntervalDays,
                scheduleAnchorOn,
                scheduleAnchorOn,
                activeProductId
        );
    }

    private void changeActiveProductFrequencyPreservingHistory(
            Long activeProductId,
            String frequency,
            int frequencyIntervalDays,
            LocalDate scheduleAnchorOn
    ) {
        jdbcTemplate.update("""
                        UPDATE member_active_product
                        SET frequency = ?,
                            frequency_interval_days = ?,
                            schedule_anchor_on = ?,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                frequency,
                frequencyIntervalDays,
                scheduleAnchorOn,
                activeProductId
        );
        jdbcTemplate.update("""
                        UPDATE member_active_product_schedule_history
                        SET effective_to = ?,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE member_active_product_id = ?
                          AND effective_to IS NULL
                        """,
                scheduleAnchorOn,
                activeProductId
        );
        insertMemberActiveProductScheduleHistory(
                activeProductId,
                frequency,
                frequencyIntervalDays,
                scheduleAnchorOn.toString(),
                scheduleAnchorOn.toString(),
                null
        );
    }

    private void moveActiveProductStartAndCurrentSchedule(Long activeProductId, LocalDate startedOn) {
        jdbcTemplate.update("""
                        UPDATE member_active_product
                        SET started_on = ?,
                            schedule_anchor_on = ?,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                startedOn,
                startedOn,
                activeProductId
        );
        jdbcTemplate.update("""
                        UPDATE member_active_product_schedule_history
                        SET schedule_anchor_on = ?,
                            effective_from = ?,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE member_active_product_id = ?
                          AND effective_to IS NULL
                        """,
                startedOn,
                startedOn,
                activeProductId
        );
    }

    private void insertCompletedRoutineDays(
            Long memberId,
            Long activeProductId,
            Long productId,
            LocalDate endDate,
            int days,
            Long firstIntakeDayId,
            Long firstRecordId
    ) {
        for (int offset = 0; offset < days; offset++) {
            LocalDate intakeOn = endDate.minusDays(offset);
            long intakeDayId = firstIntakeDayId + offset;
            insertCalendarIntakeDay(
                    intakeDayId,
                    memberId,
                    intakeOn,
                    true,
                    intakeOn + " 08:00:00"
            );
            insertTodayIntakeRecord(
                    firstRecordId + offset,
                    intakeDayId,
                    activeProductId,
                    productId,
                    true,
                    intakeOn + " 08:00:00"
            );
        }
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

    private void insertTodayIntakeDay(
            Long id,
            Long memberId,
            LocalDate intakeOn,
            String autoPopupShownAt
    ) {
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
                        VALUES (?, ?, ?, ?, false, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                id,
                memberId,
                intakeOn,
                autoPopupShownAt
        );
    }

    private void insertCalendarIntakeDay(
            Long id,
            Long memberId,
            LocalDate intakeOn,
            boolean allCompleted,
            String completedAt
    ) {
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
                        VALUES (?, ?, ?, NULL, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                id,
                memberId,
                intakeOn,
                allCompleted,
                completedAt
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

    private void insertTodayIntakeRecord(
            Long id,
            Long intakeDayId,
            Long memberActiveProductId,
            Long productId,
            boolean taken,
            String takenAt
    ) {
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
                        VALUES (?, ?, ?, ?, true, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                id,
                intakeDayId,
                memberActiveProductId,
                productId,
                taken,
                takenAt
        );
    }

    private int toIntakeDayCount(LocalDate startedOn, LocalDate currentDate) {
        long dayCount = ChronoUnit.DAYS.between(startedOn, currentDate) + 1;
        return (int) Math.max(dayCount, 1);
    }

    private LocalDate currentDate() {
        return LocalDate.now(SERVICE_ZONE_ID);
    }

    private ResultActions performCalendarRequest(YearMonth targetMonth, String token) throws Exception {
        return mockMvc.perform(get(CALENDAR_URL)
                .param("year", String.valueOf(targetMonth.getYear()))
                .param("month", String.valueOf(targetMonth.getMonthValue()))
                .header(HttpHeaders.AUTHORIZATION, bearerToken(token)));
    }

    private LocalDateTime findAutoPopupShownAt(Long memberId, LocalDate intakeOn) {
        return jdbcTemplate.queryForObject(
                "SELECT auto_popup_shown_at FROM intake_day WHERE member_id = ? AND intake_on = ?",
                LocalDateTime.class,
                memberId,
                intakeOn
        );
    }

    private Boolean findAllCompleted(Long memberId, LocalDate intakeOn) {
        return jdbcTemplate.queryForObject(
                "SELECT all_completed FROM intake_day WHERE member_id = ? AND intake_on = ?",
                Boolean.class,
                memberId,
                intakeOn
        );
    }

    private LocalDateTime findCompletedAt(Long memberId, LocalDate intakeOn) {
        return jdbcTemplate.queryForObject(
                "SELECT completed_at FROM intake_day WHERE member_id = ? AND intake_on = ?",
                LocalDateTime.class,
                memberId,
                intakeOn
        );
    }

    private Boolean findTodayRecordTaken(Long memberId, LocalDate intakeOn, Long activeProductId) {
        return jdbcTemplate.queryForObject("""
                        SELECT ir.taken
                        FROM intake_record ir
                        JOIN intake_day iday ON iday.id = ir.intake_day_id
                        WHERE iday.member_id = ?
                          AND iday.intake_on = ?
                          AND ir.member_active_product_id = ?
                        """,
                Boolean.class,
                memberId,
                intakeOn,
                activeProductId
        );
    }

    private LocalDateTime findTodayRecordTakenAt(Long memberId, LocalDate intakeOn, Long activeProductId) {
        return jdbcTemplate.queryForObject("""
                        SELECT ir.taken_at
                        FROM intake_record ir
                        JOIN intake_day iday ON iday.id = ir.intake_day_id
                        WHERE iday.member_id = ?
                          AND iday.intake_on = ?
                          AND ir.member_active_product_id = ?
                        """,
                LocalDateTime.class,
                memberId,
                intakeOn,
                activeProductId
        );
    }

    private LocalTime findIntakeTime(Long activeProductId) {
        return jdbcTemplate.queryForObject(
                "SELECT intake_time FROM member_active_product WHERE id = ?",
                LocalTime.class,
                activeProductId
        );
    }

    private Boolean findNotificationEnabled(Long activeProductId) {
        return jdbcTemplate.queryForObject(
                "SELECT notification_enabled FROM member_active_product WHERE id = ?",
                Boolean.class,
                activeProductId
        );
    }

    private String findActiveProductFrequency(Long activeProductId) {
        return jdbcTemplate.queryForObject(
                "SELECT frequency FROM member_active_product WHERE id = ?",
                String.class,
                activeProductId
        );
    }

    private Short findFrequencyIntervalDays(Long activeProductId) {
        return jdbcTemplate.queryForObject(
                "SELECT frequency_interval_days FROM member_active_product WHERE id = ?",
                Short.class,
                activeProductId
        );
    }

    private LocalDate findScheduleAnchorOn(Long activeProductId) {
        return jdbcTemplate.queryForObject(
                "SELECT schedule_anchor_on FROM member_active_product WHERE id = ?",
                LocalDate.class,
                activeProductId
        );
    }

    private LocalDate findStoppedOn(Long activeProductId) {
        return jdbcTemplate.queryForObject(
                "SELECT stopped_on FROM member_active_product WHERE id = ?",
                LocalDate.class,
                activeProductId
        );
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

    private int countMemberActiveProductsByMemberProductId(Long memberProductId) {
        Integer count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM member_active_product
                        WHERE member_product_id = ?
                          AND member_id = ?
                        """,
                Integer.class,
                memberProductId,
                MEMBER_ID
        );
        return count == null ? 0 : count;
    }

    private int countActiveProductsByMemberProductId(Long memberProductId) {
        Integer count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM member_active_product
                        WHERE member_product_id = ?
                          AND member_id = ?
                          AND stopped_on IS NULL
                        """,
                Integer.class,
                memberProductId,
                MEMBER_ID
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

    private int countIntakeDays() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM intake_day", Integer.class);
        return count == null ? 0 : count;
    }

    private int countIntakeDays(Long memberId, LocalDate intakeOn) {
        Integer count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM intake_day
                        WHERE member_id = ?
                          AND intake_on = ?
                        """,
                Integer.class,
                memberId,
                intakeOn
        );
        return count == null ? 0 : count;
    }

    private int countIntakeRecords() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM intake_record", Integer.class);
        return count == null ? 0 : count;
    }

    private int countIntakeRecords(Long memberId, LocalDate intakeOn) {
        Integer count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM intake_record ir
                        JOIN intake_day iday ON iday.id = ir.intake_day_id
                        WHERE iday.member_id = ?
                          AND iday.intake_on = ?
                        """,
                Integer.class,
                memberId,
                intakeOn
        );
        return count == null ? 0 : count;
    }

    private int countScheduleHistories(Long activeProductId) {
        Integer count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM member_active_product_schedule_history
                        WHERE member_active_product_id = ?
                        """,
                Integer.class,
                activeProductId
        );
        return count == null ? 0 : count;
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

    private int countActiveScheduleHistories(Long activeProductId, String frequency) {
        Integer count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM member_active_product_schedule_history
                        WHERE member_active_product_id = ?
                          AND frequency = ?
                          AND effective_to IS NULL
                        """,
                Integer.class,
                activeProductId,
                frequency
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

    private String activeProductUrl(Object activeProductId) {
        return ACTIVE_PRODUCTS_URL + "/" + activeProductId;
    }

    private String bearerToken(String token) {
        return "Bearer " + token;
    }
}
