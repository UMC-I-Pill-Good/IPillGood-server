package com.ipillgood.server.domain.intake.service;

import com.ipillgood.server.domain.intake.entity.MemberActiveProduct;
import com.ipillgood.server.domain.intake.repository.MemberActiveProductScheduleHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ActiveProductStopService {

    private final MemberActiveProductScheduleHistoryRepository memberActiveProductScheduleHistoryRepository;

    public void stop(MemberActiveProduct activeProduct, LocalDate stoppedOn) {
        activeProduct.markStopped(stoppedOn);
        memberActiveProductScheduleHistoryRepository.findActiveByActiveProductId(activeProduct.getId())
                .ifPresent(activeHistory -> activeHistory.close(stoppedOn));
    }
}
