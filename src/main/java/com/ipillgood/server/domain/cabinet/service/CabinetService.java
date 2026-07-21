package com.ipillgood.server.domain.cabinet.service;

import com.ipillgood.server.domain.cabinet.code.CabinetErrorCode;
import com.ipillgood.server.domain.cabinet.converter.CabinetConverter;
import com.ipillgood.server.domain.cabinet.dto.CabinetResponse;
import com.ipillgood.server.domain.cabinet.exception.CabinetException;
import com.ipillgood.server.domain.cabinet.repository.CabinetProductRow;
import com.ipillgood.server.domain.cabinet.repository.MemberProductRepository;
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
public class CabinetService {

    private final MemberRepository memberRepository;
    private final MemberProductRepository memberProductRepository;

    @Value("${app.storage.public-base-url:https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com}")
    private String storagePublicBaseUrl;

    public CabinetResponse.ProductList getProducts(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.UNAUTHORIZED));
        validateOnboardingCompleted(member);

        List<CabinetProductRow> products = memberProductRepository.findActiveCabinetProducts(memberId);
        return CabinetConverter.toProductList(member.getNickname(), products, storagePublicBaseUrl);
    }

    private void validateOnboardingCompleted(Member member) {
        if (member.getOnboardingCompletedAt() == null) {
            throw new CabinetException(CabinetErrorCode.ONBOARDING_NOT_COMPLETED);
        }
    }
}
