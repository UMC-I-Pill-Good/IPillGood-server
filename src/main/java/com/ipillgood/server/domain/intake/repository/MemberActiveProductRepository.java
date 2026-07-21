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
            select new com.ipillgood.server.domain.intake.repository.ActiveProductRow(
                ap.id,
                mp.id,
                p.id,
                p.name,
                count(pi.id),
                min(i.imageKey)
            )
            from MemberActiveProduct ap
            join ap.memberProduct mp
            join mp.product p
            left join ProductIngredient pi on pi.product = p
            left join pi.ingredient i
            where ap.member.id = :memberId
              and ap.stoppedOn is null
              and mp.deletedAt is null
              and p.deletedAt is null
            group by ap.id, mp.id, p.id, p.name, ap.createdAt
            order by ap.createdAt asc, ap.id asc
            """)
    List<ActiveProductRow> findActiveProductRows(@Param("memberId") Long memberId);

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
