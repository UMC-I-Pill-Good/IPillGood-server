package com.ipillgood.server.domain.cabinet.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberProductQueryDsl {

    Page<CabinetProductCandidateRow> findProductCandidatesOrderByReviewCountDesc(
            Long memberId,
            String keyword,
            Pageable pageable
    );

    Page<CabinetProductCandidateRow> findProductCandidatesOrderByRatingDesc(
            Long memberId,
            String keyword,
            Pageable pageable
    );
}
