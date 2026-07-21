package com.ipillgood.server.domain.cabinet.repository;

import com.ipillgood.server.domain.cabinet.entity.MemberProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberProductRepository extends JpaRepository<MemberProduct, Long> {

    @Query("""
            select new com.ipillgood.server.domain.cabinet.repository.CabinetProductRow(
                mp.id,
                p.id,
                p.name,
                mp.addedAt,
                ap.id,
                count(pi.id),
                min(i.imageKey)
            )
            from MemberProduct mp
            join mp.product p
            left join ProductIngredient pi on pi.product = p
            left join pi.ingredient i
            left join MemberActiveProduct ap on ap.memberProduct = mp and ap.stoppedOn is null
            where mp.member.id = :memberId
              and mp.deletedAt is null
              and p.deletedAt is null
            group by mp.id, p.id, p.name, mp.addedAt, ap.id
            order by mp.addedAt desc
            """)
    List<CabinetProductRow> findActiveCabinetProducts(@Param("memberId") Long memberId);

    @Query("""
            select new com.ipillgood.server.domain.cabinet.repository.CabinetProductDetailRow(
                mp.id,
                p.id,
                p.brand,
                p.name,
                ap.id,
                ap.startedOn,
                ap.notificationEnabled,
                ap.intakeTime,
                ap.frequency,
                ap.frequencyIntervalDays,
                ap.scheduleAnchorOn,
                case when count(pr.id) > 0 then true else false end
            )
            from MemberProduct mp
            join mp.product p
            left join MemberActiveProduct ap on ap.memberProduct = mp and ap.stoppedOn is null
            left join ProductReview pr on pr.product = p
                and pr.member.id = :memberId
                and pr.deletedAt is null
            where mp.member.id = :memberId
              and mp.id = :memberProductId
              and mp.deletedAt is null
              and p.deletedAt is null
            group by mp.id, p.id, p.brand, p.name, ap.id, ap.startedOn, ap.notificationEnabled,
                ap.intakeTime, ap.frequency, ap.frequencyIntervalDays, ap.scheduleAnchorOn
            """)
    Optional<CabinetProductDetailRow> findActiveCabinetProductDetail(
            @Param("memberId") Long memberId,
            @Param("memberProductId") Long memberProductId
    );

    @Query("""
            select new com.ipillgood.server.domain.cabinet.repository.CabinetProductIngredientKeywordRow(
                i.id,
                i.name,
                i.imageKey,
                i.description,
                ek.keyword
            )
            from MemberProduct mp
            join mp.product p
            join ProductIngredient pi on pi.product = p
            join pi.ingredient i
            left join EffectKeyword ek on ek.ingredient = i
            where mp.member.id = :memberId
              and mp.id = :memberProductId
              and mp.deletedAt is null
              and p.deletedAt is null
            order by pi.id asc, ek.id asc
            """)
    List<CabinetProductIngredientKeywordRow> findProductIngredientKeywordRows(
            @Param("memberId") Long memberId,
            @Param("memberProductId") Long memberProductId
    );

    @Query("""
            select mp.product.id
            from MemberProduct mp
            where mp.member.id = :memberId
              and mp.product.id in :productIds
              and mp.deletedAt is null
            """)
    List<Long> findActiveOwnedProductIds(
            @Param("memberId") Long memberId,
            @Param("productIds") Collection<Long> productIds
    );

    @Query("""
            select new com.ipillgood.server.domain.cabinet.repository.CabinetAddedProductRow(
                mp.id,
                p.id,
                p.brand,
                p.name,
                mp.addedAt,
                count(pi.id),
                min(i.imageKey)
            )
            from MemberProduct mp
            join mp.product p
            left join ProductIngredient pi on pi.product = p
            left join pi.ingredient i
            where mp.member.id = :memberId
              and mp.id in :memberProductIds
              and mp.deletedAt is null
              and p.deletedAt is null
            group by mp.id, p.id, p.brand, p.name, mp.addedAt
            """)
    List<CabinetAddedProductRow> findAddedProductsByIds(
            @Param("memberId") Long memberId,
            @Param("memberProductIds") Collection<Long> memberProductIds
    );
}
