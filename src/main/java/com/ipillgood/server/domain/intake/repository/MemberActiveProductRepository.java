package com.ipillgood.server.domain.intake.repository;

import com.ipillgood.server.domain.intake.entity.MemberActiveProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberActiveProductRepository extends JpaRepository<MemberActiveProduct, Long> {

    @Query("""
            select ap
            from MemberActiveProduct ap
            where ap.member.id = :memberId
              and ap.memberProduct.id in :memberProductIds
              and ap.stoppedOn is null
            """)
    List<MemberActiveProduct> findActiveByMemberProductIds(
            @Param("memberId") Long memberId,
            @Param("memberProductIds") Collection<Long> memberProductIds
    );

    @Query("""
            select ap
            from MemberActiveProduct ap
            join fetch ap.memberProduct mp
            join fetch mp.product p
            where ap.member.id = :memberId
              and ap.id = :activeProductId
              and ap.stoppedOn is null
              and mp.deletedAt is null
              and p.deletedAt is null
            """)
    Optional<MemberActiveProduct> findActiveReviewPromptDismissTarget(
            @Param("memberId") Long memberId,
            @Param("activeProductId") Long activeProductId
    );
}
