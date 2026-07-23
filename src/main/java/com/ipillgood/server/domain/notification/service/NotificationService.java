package com.ipillgood.server.domain.notification.service;

import com.ipillgood.server.domain.intake.repository.IntakeNotificationActiveProductRow;
import com.ipillgood.server.domain.intake.repository.MemberActiveProductRepository;
import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.member.repository.MemberRepository;
import com.ipillgood.server.domain.notification.code.NotificationErrorCode;
import com.ipillgood.server.domain.notification.converter.NotificationConverter;
import com.ipillgood.server.domain.notification.dto.NotificationRequest;
import com.ipillgood.server.domain.notification.dto.NotificationResponse;
import com.ipillgood.server.domain.notification.entity.MemberNotificationSetting;
import com.ipillgood.server.domain.notification.entity.MemberPushToken;
import com.ipillgood.server.domain.notification.entity.enums.PushPlatform;
import com.ipillgood.server.domain.notification.exception.NotificationException;
import com.ipillgood.server.domain.notification.repository.MemberNotificationSettingRepository;
import com.ipillgood.server.domain.notification.repository.MemberPushTokenRepository;
import com.ipillgood.server.global.apiPayload.code.GeneralErrorCode;
import com.ipillgood.server.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private static final boolean DEFAULT_PUSH_ENABLED = true;
    private static final boolean DEFAULT_INTAKE_PUSH_ENABLED = true;
    private static final int MAX_PUSH_TOKEN_LENGTH = 512;
    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final MemberRepository memberRepository;
    private final MemberNotificationSettingRepository memberNotificationSettingRepository;
    private final MemberPushTokenRepository memberPushTokenRepository;
    private final MemberActiveProductRepository memberActiveProductRepository;

    public NotificationResponse.AppPushSetting getAppPushSetting(Long memberId) {
        Member member = getMember(memberId);
        validateOnboardingCompleted(member);

        boolean pushEnabled = memberNotificationSettingRepository.findById(memberId)
                .map(MemberNotificationSetting::isPushEnabled)
                .orElse(DEFAULT_PUSH_ENABLED);
        return NotificationConverter.toAppPushSetting(pushEnabled);
    }

    public NotificationResponse.IntakeNotificationSettings getIntakeNotificationSettings(Long memberId) {
        Member member = getMember(memberId);
        validateOnboardingCompleted(member);

        NotificationSettingValues settingValues = memberNotificationSettingRepository.findById(memberId)
                .map(setting -> new NotificationSettingValues(
                        setting.isPushEnabled(),
                        setting.isIntakePushEnabled()
                ))
                .orElseGet(() -> new NotificationSettingValues(
                        DEFAULT_PUSH_ENABLED,
                        DEFAULT_INTAKE_PUSH_ENABLED
                ));
        List<IntakeNotificationActiveProductRow> activeProductRows =
                memberActiveProductRepository.findIntakeNotificationActiveProductRows(memberId);

        return NotificationConverter.toIntakeNotificationSettings(
                settingValues.pushEnabled(),
                settingValues.intakePushEnabled(),
                activeProductRows
        );
    }

    @Transactional
    public NotificationResponse.AppPushSetting updateAppPushSetting(
            Long memberId,
            NotificationRequest.UpdateAppPushSetting request
    ) {
        Member member = getMember(memberId);
        validateOnboardingCompleted(member);

        boolean pushEnabled = validateUpdateAppPushSettingRequest(request);
        MemberNotificationSetting setting = memberNotificationSettingRepository.findById(memberId)
                .orElseGet(() -> memberNotificationSettingRepository.save(
                        MemberNotificationSetting.createDefault(member)
                ));
        setting.changePushEnabled(pushEnabled);

        return NotificationConverter.toAppPushSetting(setting.isPushEnabled());
    }

    @Transactional
    public NotificationResponse.PushTokenRegistration registerPushToken(
            Long memberId,
            NotificationRequest.RegisterPushToken request
    ) {
        Member member = getMember(memberId);
        RegisterPushTokenRequestValues values = validateRegisterPushTokenRequest(request);
        LocalDateTime now = LocalDateTime.now(SERVICE_ZONE_ID);

        MemberPushToken pushToken = memberPushTokenRepository.findByToken(values.token())
                .map(existingPushToken -> {
                    existingPushToken.renew(member, values.platform(), now);
                    return existingPushToken;
                })
                .orElseGet(() -> memberPushTokenRepository.save(
                        MemberPushToken.create(member, values.platform(), values.token(), now)
                ));

        return NotificationConverter.toPushTokenRegistration(pushToken);
    }

    @Transactional
    public NotificationResponse.PushTokenDeactivation deactivatePushToken(Long memberId, Long pushTokenId) {
        getMember(memberId);
        validatePushTokenId(pushTokenId);

        MemberPushToken pushToken = memberPushTokenRepository.findByIdAndMember_Id(pushTokenId, memberId)
                .orElseThrow(() -> new NotificationException(NotificationErrorCode.PUSH_TOKEN_NOT_FOUND));
        pushToken.deactivate();

        return NotificationConverter.toPushTokenDeactivation(pushToken);
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.UNAUTHORIZED));
    }

    private void validateOnboardingCompleted(Member member) {
        if (member.getOnboardingCompletedAt() == null) {
            throw new NotificationException(NotificationErrorCode.ONBOARDING_NOT_COMPLETED);
        }
    }

    private boolean validateUpdateAppPushSettingRequest(NotificationRequest.UpdateAppPushSetting request) {
        if (request == null
                || request.pushEnabled() == null
                || !(request.pushEnabled() instanceof Boolean pushEnabled)) {
            throw new NotificationException(NotificationErrorCode.APP_PUSH_SETTING_REQUEST_INVALID);
        }
        return pushEnabled;
    }

    private RegisterPushTokenRequestValues validateRegisterPushTokenRequest(
            NotificationRequest.RegisterPushToken request
    ) {
        if (request == null
                || !(request.platform() instanceof String platform)
                || !(request.token() instanceof String token)) {
            throw new NotificationException(NotificationErrorCode.PUSH_TOKEN_REGISTER_REQUEST_INVALID);
        }
        if (!PushPlatform.WEB.name().equals(platform)
                || token.isBlank()
                || token.length() > MAX_PUSH_TOKEN_LENGTH) {
            throw new NotificationException(NotificationErrorCode.PUSH_TOKEN_REGISTER_REQUEST_INVALID);
        }
        return new RegisterPushTokenRequestValues(PushPlatform.WEB, token);
    }

    private void validatePushTokenId(Long pushTokenId) {
        if (pushTokenId == null || pushTokenId < 1) {
            throw new NotificationException(NotificationErrorCode.PUSH_TOKEN_ID_INVALID);
        }
    }

    private record RegisterPushTokenRequestValues(
            PushPlatform platform,
            String token
    ) {
    }

    private record NotificationSettingValues(
            boolean pushEnabled,
            boolean intakePushEnabled
    ) {
    }
}
