package com.ipillgood.server.domain.intake.service;

import com.ipillgood.server.domain.cabinet.entity.MemberProduct;
import com.ipillgood.server.domain.cabinet.repository.MemberProductRepository;
import com.ipillgood.server.domain.ingredient.entity.enums.CombinationType;
import com.ipillgood.server.domain.intake.code.IntakeErrorCode;
import com.ipillgood.server.domain.intake.converter.IntakeConverter;
import com.ipillgood.server.domain.intake.dto.IntakeRequest;
import com.ipillgood.server.domain.intake.dto.IntakeResponse;
import com.ipillgood.server.domain.intake.entity.IntakeDay;
import com.ipillgood.server.domain.intake.entity.IntakeRecord;
import com.ipillgood.server.domain.intake.entity.MemberActiveProduct;
import com.ipillgood.server.domain.intake.entity.MemberActiveProductScheduleHistory;
import com.ipillgood.server.domain.intake.entity.enums.IntakeFrequency;
import com.ipillgood.server.domain.intake.exception.IntakeException;
import com.ipillgood.server.domain.intake.repository.ActiveProductRow;
import com.ipillgood.server.domain.intake.repository.ActiveProductSettingsRow;
import com.ipillgood.server.domain.intake.repository.CompatibilityConflictRow;
import com.ipillgood.server.domain.intake.repository.IntakeDayRepository;
import com.ipillgood.server.domain.intake.repository.IntakeRecordRepository;
import com.ipillgood.server.domain.intake.repository.MemberActiveProductRepository;
import com.ipillgood.server.domain.intake.repository.MemberActiveProductScheduleHistoryRepository;
import com.ipillgood.server.domain.intake.repository.TodayIntakeRecordRow;
import com.ipillgood.server.domain.intake.repository.TodayScheduledProductRow;
import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.member.repository.MemberRepository;
import com.ipillgood.server.global.apiPayload.code.GeneralErrorCode;
import com.ipillgood.server.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IntakeService {

    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final List<CombinationType> WARNING_COMBINATION_TYPES = List.of(
            CombinationType.CAUTION,
            CombinationType.CONTRAINDICATION
    );
    private static final Pattern INTAKE_TIME_PATTERN = Pattern.compile("^(?:[01]\\d|2[0-3]):[0-5]\\d$");
    private static final DateTimeFormatter INTAKE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final MemberRepository memberRepository;
    private final MemberProductRepository memberProductRepository;
    private final MemberActiveProductRepository memberActiveProductRepository;
    private final MemberActiveProductScheduleHistoryRepository memberActiveProductScheduleHistoryRepository;
    private final IntakeDayRepository intakeDayRepository;
    private final IntakeRecordRepository intakeRecordRepository;
    private final ActiveProductStopService activeProductStopService;

    @Value("${app.storage.public-base-url:https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com}")
    private String storagePublicBaseUrl;

    public IntakeResponse.ActiveProducts getActiveProducts(Long memberId) {
        Member member = getMember(memberId);
        validateOnboardingCompleted(member);

        List<ActiveProductRow> activeProducts = memberActiveProductRepository.findActiveProductRows(memberId);
        return IntakeConverter.toActiveProducts(activeProducts, storagePublicBaseUrl);
    }

    public IntakeResponse.TodayIntakeStatus getTodayIntakeStatus(Long memberId) {
        Member member = getMember(memberId);
        validateOnboardingCompleted(member);

        LocalDate currentDate = currentDate();
        List<TodayScheduledProductRow> scheduledRows = findTodayScheduledRows(memberId, currentDate);

        IntakeDay intakeDay = intakeDayRepository.findByMemberIdAndIntakeOn(memberId, currentDate)
                .orElse(null);
        boolean autoPopupShown = intakeDay != null && intakeDay.getAutoPopupShownAt() != null;
        Map<Long, TodayIntakeRecordRow> recordsByActiveProductId =
                findTodayRecordsByActiveProductId(intakeDay, scheduledRows);

        return IntakeConverter.toTodayIntakeStatus(
                currentDate,
                scheduledRows,
                recordsByActiveProductId,
                autoPopupShown
        );
    }

    @Transactional
    public IntakeResponse.TodayPopupShown recordTodayPopupShown(Long memberId) {
        Member member = getMember(memberId);
        validateOnboardingCompleted(member);

        LocalDate currentDate = currentDate();
        IntakeDay intakeDay = intakeDayRepository.findByMemberIdAndIntakeOn(memberId, currentDate)
                .orElse(null);

        if (intakeDay != null && intakeDay.getAutoPopupShownAt() != null) {
            return IntakeConverter.toTodayPopupShown(currentDate, intakeDay.getAutoPopupShownAt());
        }

        List<TodayScheduledProductRow> scheduledRows = findTodayScheduledRows(memberId, currentDate);
        Map<Long, TodayIntakeRecordRow> recordsByActiveProductId =
                findTodayRecordsByActiveProductId(intakeDay, scheduledRows);
        validateTodayPopupTarget(scheduledRows, recordsByActiveProductId);

        if (intakeDay == null) {
            intakeDay = intakeDayRepository.save(IntakeDay.create(member, currentDate));
        }

        LocalDateTime autoPopupShownAt = intakeDay.markAutoPopupShown(currentDateTime());
        return IntakeConverter.toTodayPopupShown(currentDate, autoPopupShownAt);
    }

    @Transactional
    public IntakeResponse.SaveTodayIntakeRecords saveTodayIntakeRecords(
            Long memberId,
            IntakeRequest.SaveTodayIntakeRecords request
    ) {
        Member member = getMember(memberId);
        validateOnboardingCompleted(member);

        Set<Long> takenActiveProductIds = validateSaveTodayIntakeRecordsRequest(request);
        LocalDate currentDate = currentDate();
        List<TodayScheduledProductRow> scheduledRows = findTodayScheduledRows(memberId, currentDate);
        if (scheduledRows.isEmpty()) {
            throw new IntakeException(IntakeErrorCode.TODAY_RECORD_REQUEST_INVALID);
        }

        Map<Long, TodayScheduledProductRow> scheduledRowsByActiveProductId = scheduledRows.stream()
                .collect(Collectors.toMap(
                        TodayScheduledProductRow::activeProductId,
                        Function.identity(),
                        (first, ignored) -> first
                ));
        validateTodayRecordTargets(memberId, takenActiveProductIds, scheduledRowsByActiveProductId);

        IntakeDay intakeDay = intakeDayRepository.findByMemberIdAndIntakeOn(memberId, currentDate)
                .orElseGet(() -> intakeDayRepository.save(IntakeDay.create(member, currentDate)));
        Map<Long, MemberActiveProduct> activeProductsById =
                findActiveTodayRecordTargetsById(memberId, scheduledRowsByActiveProductId.keySet());
        Map<Long, IntakeRecord> recordsByProductId = findTodayRecordEntitiesByProductId(intakeDay, scheduledRows);

        LocalDateTime now = currentDateTime();
        List<IntakeRecord> newRecords = new ArrayList<>();
        for (TodayScheduledProductRow scheduledRow : scheduledRows) {
            MemberActiveProduct activeProduct = activeProductsById.get(scheduledRow.activeProductId());
            if (activeProduct == null) {
                throw new IntakeException(IntakeErrorCode.ACTIVE_PRODUCT_NOT_FOUND);
            }

            IntakeRecord record = recordsByProductId.get(scheduledRow.productId());
            if (record == null) {
                record = IntakeRecord.createScheduled(intakeDay, activeProduct);
                recordsByProductId.put(scheduledRow.productId(), record);
                newRecords.add(record);
            }
            record.saveTodayState(activeProduct, takenActiveProductIds.contains(scheduledRow.activeProductId()), now);
        }
        if (!newRecords.isEmpty()) {
            intakeRecordRepository.saveAll(newRecords);
        }

        int takenCount = (int) scheduledRows.stream()
                .map(row -> recordsByProductId.get(row.productId()))
                .filter(record -> record != null && record.isTaken())
                .count();
        intakeDay.changeCompletion(takenCount == scheduledRows.size(), now);

        return IntakeConverter.toSaveTodayIntakeRecords(
                currentDate,
                scheduledRows,
                recordsByProductId,
                intakeDay.getCompletedAt()
        );
    }

    @Transactional
    public IntakeResponse.RegisterActiveProduct registerActiveProduct(
            Long memberId,
            IntakeRequest.RegisterActiveProduct request
    ) {
        Member member = getMember(memberId);
        validateOnboardingCompleted(member);

        RegisterActiveProductRequestValues values = validateRegisterActiveProductRequest(request);
        MemberProduct targetMemberProduct = memberProductRepository
                .findActiveIntakeRegistrationTarget(memberId, values.memberProductId())
                .orElseThrow(() -> new IntakeException(IntakeErrorCode.REGISTRATION_TARGET_NOT_FOUND));
        validateNotAlreadyActive(memberId, targetMemberProduct.getId());

        LocalDate currentDate = currentDate();
        MemberActiveProduct activeProduct = MemberActiveProduct.create(
                targetMemberProduct,
                member,
                currentDate,
                values.intakeTime(),
                values.frequency()
        );
        memberActiveProductRepository.save(activeProduct);
        memberActiveProductScheduleHistoryRepository.save(
                MemberActiveProductScheduleHistory.createInitial(activeProduct)
        );

        ActiveProductRow activeProductRow = memberActiveProductRepository
                .findActiveProductRow(memberId, activeProduct.getId())
                .orElseThrow(() -> new IntakeException(IntakeErrorCode.REGISTRATION_TARGET_NOT_FOUND));
        return IntakeConverter.toRegisterActiveProduct(activeProduct, activeProductRow, storagePublicBaseUrl);
    }

    @Transactional
    public IntakeResponse.UpdateActiveProductSettings updateActiveProductSettings(
            Long memberId,
            String activeProductId,
            IntakeRequest.UpdateActiveProductSettings request
    ) {
        Member member = getMember(memberId);
        validateOnboardingCompleted(member);

        Long parsedActiveProductId = validateActiveProductId(activeProductId);
        UpdateActiveProductSettingsRequestValues values = validateUpdateActiveProductSettingsRequest(request);
        MemberActiveProduct activeProduct = memberActiveProductRepository
                .findActiveSettingsUpdateTarget(memberId, parsedActiveProductId)
                .orElseThrow(() -> new IntakeException(IntakeErrorCode.ACTIVE_PRODUCT_NOT_FOUND));

        LocalDate currentDate = currentDate();
        if (values.intakeTime() != null) {
            activeProduct.changeIntakeTime(values.intakeTime());
        }
        if (values.notificationEnabled() != null) {
            activeProduct.changeNotificationEnabled(values.notificationEnabled());
        }
        if (values.frequency() != null && values.frequency() != activeProduct.getFrequency()) {
            activeProduct.changeFrequency(values.frequency(), currentDate);
            updateScheduleHistory(activeProduct, currentDate);
        }

        ActiveProductSettingsRow activeProductSettingsRow = memberActiveProductRepository
                .findActiveProductSettingsRow(memberId, parsedActiveProductId)
                .orElseThrow(() -> new IntakeException(IntakeErrorCode.ACTIVE_PRODUCT_NOT_FOUND));
        return IntakeConverter.toUpdateActiveProductSettings(
                activeProductSettingsRow,
                currentDate,
                storagePublicBaseUrl
        );
    }

    @Transactional
    public IntakeResponse.RemoveActiveProduct removeActiveProduct(Long memberId, String activeProductId) {
        Member member = getMember(memberId);
        validateOnboardingCompleted(member);

        Long parsedActiveProductId = validateActiveProductId(activeProductId);
        MemberActiveProduct activeProduct = memberActiveProductRepository
                .findActiveStopTarget(memberId, parsedActiveProductId)
                .orElseThrow(() -> new IntakeException(IntakeErrorCode.ACTIVE_PRODUCT_NOT_FOUND));

        LocalDate currentDate = currentDate();
        activeProductStopService.stop(activeProduct, currentDate);
        return IntakeConverter.toRemoveActiveProduct(activeProduct, currentDate);
    }

    public IntakeResponse.CompatibilityCheck checkCompatibility(
            Long memberId,
            IntakeRequest.CompatibilityCheck request
    ) {
        Member member = getMember(memberId);
        validateOnboardingCompleted(member);

        Long memberProductId = validateCompatibilityCheckRequest(request);
        MemberProduct targetMemberProduct = memberProductRepository
                .findActiveIntakeRegistrationTarget(memberId, memberProductId)
                .orElseThrow(() -> new IntakeException(IntakeErrorCode.REGISTRATION_TARGET_NOT_FOUND));
        validateNotAlreadyActive(memberId, targetMemberProduct.getId());

        List<CompatibilityConflictRow> conflicts = memberActiveProductRepository.findCompatibilityConflicts(
                memberId,
                targetMemberProduct.getId(),
                WARNING_COMBINATION_TYPES
        );
        return IntakeConverter.toCompatibilityCheck(conflicts);
    }

    private void updateScheduleHistory(MemberActiveProduct activeProduct, LocalDate currentDate) {
        memberActiveProductScheduleHistoryRepository.findActiveByActiveProductId(activeProduct.getId())
                .ifPresentOrElse(
                        activeHistory -> {
                            if (currentDate.equals(activeHistory.getEffectiveFrom())) {
                                activeHistory.changeFrequency(activeProduct.getFrequency(), currentDate);
                                return;
                            }
                            activeHistory.close(currentDate);
                            memberActiveProductScheduleHistoryRepository.save(
                                    MemberActiveProductScheduleHistory.createChanged(activeProduct)
                            );
                        },
                        () -> memberActiveProductScheduleHistoryRepository.save(
                                MemberActiveProductScheduleHistory.createChanged(activeProduct)
                        )
                );
    }

    private List<TodayScheduledProductRow> findTodayScheduledRows(Long memberId, LocalDate currentDate) {
        return memberActiveProductRepository
                .findTodayScheduleCandidateRows(memberId, currentDate)
                .stream()
                .filter(row -> isScheduledOn(row, currentDate))
                .toList();
    }

    private void validateTodayPopupTarget(
            List<TodayScheduledProductRow> scheduledRows,
            Map<Long, TodayIntakeRecordRow> recordsByActiveProductId
    ) {
        int scheduledCount = scheduledRows.size();
        int takenCount = (int) scheduledRows.stream()
                .map(row -> recordsByActiveProductId.get(row.activeProductId()))
                .filter(record -> record != null && Boolean.TRUE.equals(record.taken()))
                .count();

        if (scheduledCount == 0 || takenCount == scheduledCount) {
            throw new IntakeException(IntakeErrorCode.TODAY_POPUP_NOT_TARGET);
        }
    }

    private Set<Long> validateSaveTodayIntakeRecordsRequest(
            IntakeRequest.SaveTodayIntakeRecords request
    ) {
        if (request == null || request.takenActiveProductIds() == null) {
            throw new IntakeException(IntakeErrorCode.TODAY_RECORD_REQUEST_INVALID);
        }

        Set<Long> takenActiveProductIds = new LinkedHashSet<>();
        for (Long activeProductId : request.takenActiveProductIds()) {
            if (activeProductId == null || activeProductId < 1 || !takenActiveProductIds.add(activeProductId)) {
                throw new IntakeException(IntakeErrorCode.TODAY_RECORD_REQUEST_INVALID);
            }
        }
        return takenActiveProductIds;
    }

    private void validateTodayRecordTargets(
            Long memberId,
            Set<Long> takenActiveProductIds,
            Map<Long, TodayScheduledProductRow> scheduledRowsByActiveProductId
    ) {
        if (takenActiveProductIds.isEmpty()) {
            return;
        }

        Map<Long, MemberActiveProduct> requestedActiveProductsById =
                findActiveTodayRecordTargetsById(memberId, takenActiveProductIds);
        if (requestedActiveProductsById.size() != takenActiveProductIds.size()) {
            throw new IntakeException(IntakeErrorCode.ACTIVE_PRODUCT_NOT_FOUND);
        }
        if (!scheduledRowsByActiveProductId.keySet().containsAll(takenActiveProductIds)) {
            throw new IntakeException(IntakeErrorCode.TODAY_RECORD_REQUEST_INVALID);
        }
    }

    private Map<Long, MemberActiveProduct> findActiveTodayRecordTargetsById(
            Long memberId,
            Collection<Long> activeProductIds
    ) {
        if (activeProductIds.isEmpty()) {
            return Map.of();
        }

        return memberActiveProductRepository.findActiveTodayRecordTargets(memberId, activeProductIds)
                .stream()
                .collect(Collectors.toMap(
                        MemberActiveProduct::getId,
                        Function.identity(),
                        (first, ignored) -> first
                ));
    }

    private Map<Long, TodayIntakeRecordRow> findTodayRecordsByActiveProductId(
            IntakeDay intakeDay,
            List<TodayScheduledProductRow> scheduledRows
    ) {
        if (intakeDay == null || scheduledRows.isEmpty()) {
            return Map.of();
        }

        List<Long> activeProductIds = scheduledRows.stream()
                .map(TodayScheduledProductRow::activeProductId)
                .toList();
        return intakeRecordRepository.findTodayRecordRows(intakeDay.getId(), activeProductIds)
                .stream()
                .collect(Collectors.toMap(
                        TodayIntakeRecordRow::activeProductId,
                        Function.identity(),
                        (first, ignored) -> first
                ));
    }

    private Map<Long, IntakeRecord> findTodayRecordEntitiesByProductId(
            IntakeDay intakeDay,
            List<TodayScheduledProductRow> scheduledRows
    ) {
        if (scheduledRows.isEmpty()) {
            return Map.of();
        }

        List<Long> productIds = scheduledRows.stream()
                .map(TodayScheduledProductRow::productId)
                .toList();
        return intakeRecordRepository.findTodayRecordEntities(intakeDay.getId(), productIds)
                .stream()
                .collect(Collectors.toMap(
                        record -> record.getProduct().getId(),
                        Function.identity(),
                        (first, ignored) -> first
                ));
    }

    private boolean isScheduledOn(TodayScheduledProductRow row, LocalDate currentDate) {
        if (row.scheduleAnchorOn() == null || row.frequencyIntervalDays() == null
                || row.frequencyIntervalDays() < 1) {
            return false;
        }

        long daysSinceAnchor = ChronoUnit.DAYS.between(row.scheduleAnchorOn(), currentDate);
        return daysSinceAnchor >= 0 && daysSinceAnchor % row.frequencyIntervalDays() == 0;
    }

    private LocalDate currentDate() {
        return LocalDate.now(SERVICE_ZONE_ID);
    }

    private LocalDateTime currentDateTime() {
        return LocalDateTime.now(SERVICE_ZONE_ID);
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.UNAUTHORIZED));
    }

    private void validateOnboardingCompleted(Member member) {
        if (member.getOnboardingCompletedAt() == null) {
            throw new IntakeException(IntakeErrorCode.ONBOARDING_NOT_COMPLETED);
        }
    }

    private Long validateCompatibilityCheckRequest(IntakeRequest.CompatibilityCheck request) {
        if (request == null || request.memberProductId() == null || request.memberProductId() < 1) {
            throw new IntakeException(IntakeErrorCode.REGISTRATION_REQUEST_INVALID);
        }
        return request.memberProductId();
    }

    private Long validateActiveProductId(String activeProductId) {
        if (activeProductId == null || activeProductId.isBlank()) {
            throw new IntakeException(IntakeErrorCode.ACTIVE_PRODUCT_ID_INVALID);
        }

        try {
            long parsedActiveProductId = Long.parseLong(activeProductId.trim());
            if (parsedActiveProductId < 1) {
                throw new IntakeException(IntakeErrorCode.ACTIVE_PRODUCT_ID_INVALID);
            }
            return parsedActiveProductId;
        } catch (NumberFormatException e) {
            throw new IntakeException(IntakeErrorCode.ACTIVE_PRODUCT_ID_INVALID);
        }
    }

    private RegisterActiveProductRequestValues validateRegisterActiveProductRequest(
            IntakeRequest.RegisterActiveProduct request
    ) {
        if (request == null || request.memberProductId() == null || request.memberProductId() < 1) {
            throw new IntakeException(IntakeErrorCode.REGISTRATION_REQUEST_INVALID);
        }
        if (request.intakeTime() == null || !INTAKE_TIME_PATTERN.matcher(request.intakeTime()).matches()) {
            throw new IntakeException(IntakeErrorCode.REGISTRATION_REQUEST_INVALID);
        }
        if (request.frequency() == null || request.frequency().isBlank()) {
            throw new IntakeException(IntakeErrorCode.REGISTRATION_REQUEST_INVALID);
        }

        IntakeFrequency frequency;
        try {
            frequency = IntakeFrequency.valueOf(request.frequency());
        } catch (IllegalArgumentException e) {
            throw new IntakeException(IntakeErrorCode.REGISTRATION_REQUEST_INVALID);
        }

        LocalTime intakeTime = LocalTime.parse(request.intakeTime(), INTAKE_TIME_FORMATTER);
        return new RegisterActiveProductRequestValues(request.memberProductId(), intakeTime, frequency);
    }

    private UpdateActiveProductSettingsRequestValues validateUpdateActiveProductSettingsRequest(
            IntakeRequest.UpdateActiveProductSettings request
    ) {
        if (request == null || (
                request.intakeTime() == null
                        && request.frequency() == null
                        && request.notificationEnabled() == null
        )) {
            throw new IntakeException(IntakeErrorCode.SETTINGS_UPDATE_REQUEST_INVALID);
        }

        LocalTime intakeTime = null;
        if (request.intakeTime() != null) {
            if (!INTAKE_TIME_PATTERN.matcher(request.intakeTime()).matches()) {
                throw new IntakeException(IntakeErrorCode.SETTINGS_UPDATE_REQUEST_INVALID);
            }
            intakeTime = LocalTime.parse(request.intakeTime(), INTAKE_TIME_FORMATTER);
        }

        IntakeFrequency frequency = null;
        if (request.frequency() != null) {
            if (request.frequency().isBlank()) {
                throw new IntakeException(IntakeErrorCode.SETTINGS_UPDATE_REQUEST_INVALID);
            }
            try {
                frequency = IntakeFrequency.valueOf(request.frequency());
            } catch (IllegalArgumentException e) {
                throw new IntakeException(IntakeErrorCode.SETTINGS_UPDATE_REQUEST_INVALID);
            }
        }

        return new UpdateActiveProductSettingsRequestValues(
                intakeTime,
                frequency,
                request.notificationEnabled()
        );
    }

    private void validateNotAlreadyActive(Long memberId, Long memberProductId) {
        if (memberActiveProductRepository.existsByMemberIdAndMemberProductIdAndStoppedOnIsNull(
                memberId,
                memberProductId
        )) {
            throw new IntakeException(IntakeErrorCode.ACTIVE_PRODUCT_ALREADY_EXISTS);
        }
    }

    private record RegisterActiveProductRequestValues(
            Long memberProductId,
            LocalTime intakeTime,
            IntakeFrequency frequency
    ) {
    }

    private record UpdateActiveProductSettingsRequestValues(
            LocalTime intakeTime,
            IntakeFrequency frequency,
            Boolean notificationEnabled
    ) {
    }
}
