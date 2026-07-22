package com.ipillgood.server.domain.notification.service;

import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.member.repository.MemberRepository;
import com.ipillgood.server.domain.notification.code.NotificationErrorCode;
import com.ipillgood.server.domain.notification.converter.NotificationConverter;
import com.ipillgood.server.domain.notification.dto.NotificationResponse;
import com.ipillgood.server.domain.notification.entity.MemberNotificationSetting;
import com.ipillgood.server.domain.notification.exception.NotificationException;
import com.ipillgood.server.domain.notification.repository.MemberNotificationSettingRepository;
import com.ipillgood.server.global.apiPayload.code.GeneralErrorCode;
import com.ipillgood.server.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private static final boolean DEFAULT_PUSH_ENABLED = true;

    private final MemberRepository memberRepository;
    private final MemberNotificationSettingRepository memberNotificationSettingRepository;

    public NotificationResponse.AppPushSetting getAppPushSetting(Long memberId) {
        Member member = getMember(memberId);
        validateOnboardingCompleted(member);

        boolean pushEnabled = memberNotificationSettingRepository.findById(memberId)
                .map(MemberNotificationSetting::isPushEnabled)
                .orElse(DEFAULT_PUSH_ENABLED);
        return NotificationConverter.toAppPushSetting(pushEnabled);
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
}
