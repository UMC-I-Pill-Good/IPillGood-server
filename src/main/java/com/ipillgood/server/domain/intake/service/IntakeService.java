package com.ipillgood.server.domain.intake.service;

import com.ipillgood.server.domain.cabinet.entity.MemberProduct;
import com.ipillgood.server.domain.cabinet.repository.MemberProductRepository;
import com.ipillgood.server.domain.ingredient.entity.enums.CombinationType;
import com.ipillgood.server.domain.intake.code.IntakeErrorCode;
import com.ipillgood.server.domain.intake.converter.IntakeConverter;
import com.ipillgood.server.domain.intake.dto.IntakeRequest;
import com.ipillgood.server.domain.intake.dto.IntakeResponse;
import com.ipillgood.server.domain.intake.entity.MemberActiveProduct;
import com.ipillgood.server.domain.intake.entity.MemberActiveProductScheduleHistory;
import com.ipillgood.server.domain.intake.entity.enums.IntakeFrequency;
import com.ipillgood.server.domain.intake.exception.IntakeException;
import com.ipillgood.server.domain.intake.repository.ActiveProductRow;
import com.ipillgood.server.domain.intake.repository.CompatibilityConflictRow;
import com.ipillgood.server.domain.intake.repository.MemberActiveProductRepository;
import com.ipillgood.server.domain.intake.repository.MemberActiveProductScheduleHistoryRepository;
import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.member.repository.MemberRepository;
import com.ipillgood.server.global.apiPayload.code.GeneralErrorCode;
import com.ipillgood.server.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IntakeService {

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

    @Value("${app.storage.public-base-url:https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com}")
    private String storagePublicBaseUrl;

    public IntakeResponse.ActiveProducts getActiveProducts(Long memberId) {
        Member member = getMember(memberId);
        validateOnboardingCompleted(member);

        List<ActiveProductRow> activeProducts = memberActiveProductRepository.findActiveProductRows(memberId);
        return IntakeConverter.toActiveProducts(activeProducts, storagePublicBaseUrl);
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

        LocalDate currentDate = LocalDate.now();
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
}
