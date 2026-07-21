package com.ipillgood.server.domain.intake.service;

import com.ipillgood.server.domain.intake.code.IntakeErrorCode;
import com.ipillgood.server.domain.intake.converter.IntakeConverter;
import com.ipillgood.server.domain.intake.dto.IntakeResponse;
import com.ipillgood.server.domain.intake.exception.IntakeException;
import com.ipillgood.server.domain.intake.repository.ActiveProductRow;
import com.ipillgood.server.domain.intake.repository.MemberActiveProductRepository;
import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.member.repository.MemberRepository;
import com.ipillgood.server.global.apiPayload.code.GeneralErrorCode;
import com.ipillgood.server.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IntakeService {

    private final MemberRepository memberRepository;
    private final MemberActiveProductRepository memberActiveProductRepository;

    @Value("${app.storage.public-base-url:https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com}")
    private String storagePublicBaseUrl;

    public IntakeResponse.ActiveProducts getActiveProducts(Long memberId) {
        Member member = getMember(memberId);
        validateOnboardingCompleted(member);

        List<ActiveProductRow> activeProducts = memberActiveProductRepository.findActiveProductRows(memberId);
        return IntakeConverter.toActiveProducts(activeProducts, storagePublicBaseUrl);
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
}

