package com.ipillgood.server.domain.support.config;

import com.ipillgood.server.domain.support.entity.Faq;
import com.ipillgood.server.domain.support.entity.enums.FaqCategory;
import com.ipillgood.server.domain.support.repository.FaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

// 앱 기동 시 FAQ가 비어있으면 기본 3종을 등록 (관리자 FAQ 등록 API 완성 전까지 문의/고객센터 미리보기용 시드 데이터)
@Component
@RequiredArgsConstructor
public class FaqSeeder implements ApplicationRunner {

    private final FaqRepository faqRepository;

    @Override
    public void run(ApplicationArguments args) {

        // 이미 데이터가 있으면 바로 return (관리자가 등록한 FAQ를 덮어쓰지 않기 위함)
        if (faqRepository.count() > 0) {
            return;
        }

        faqRepository.saveAll(List.of(
                Faq.builder()
                        .category(FaqCategory.RECOMMENDATION_INGREDIENT)
                        .question("영양 성분 추천은 어떻게 이루어지나요?")
                        .answer("아필굿은 설문 조사 결과와 개인의 건강 정보, 섭취 이력 등을 종합적으로 분석해 사용자에게 가장 적합한 영양 성분을 추천해 드려요. "
                                + "추천 결과는 참고용이며, 필요 시 전문가와 상담을 권장드려요!")
                        .displayOrder(1)
                        .active(true)
                        .build(),
                Faq.builder()
                        .category(FaqCategory.INTAKE)
                        .question("연속 섭취일 복구 방법은 없나요?")
                        .answer("연속 섭취일은 복구할 수 없어요.")
                        .displayOrder(2)
                        .active(true)
                        .build(),
                Faq.builder()
                        .category(FaqCategory.NOTIFICATION)
                        .question("복용 알림은 어떻게 설정하나요?")
                        .answer("마이페이지 > 설정 > 복용 시간 알림에서 설정할 수 있어요.")
                        .displayOrder(3)
                        .active(true)
                        .build()
        ));
    }
}
