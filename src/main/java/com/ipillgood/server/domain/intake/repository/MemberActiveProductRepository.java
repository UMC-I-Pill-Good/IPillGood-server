package com.ipillgood.server.domain.intake.repository;

import com.ipillgood.server.domain.ingredient.entity.enums.CombinationType;
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
              and ap.id = :activeProductId
              and ap.stoppedOn is null
              and mp.deletedAt is null
              and p.deletedAt is null
            group by ap.id, mp.id, p.id, p.name, ap.createdAt
            """)
    Optional<ActiveProductRow> findActiveProductRow(
            @Param("memberId") Long memberId,
            @Param("activeProductId") Long activeProductId
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
    Optional<MemberActiveProduct> findActiveSettingsUpdateTarget(
            @Param("memberId") Long memberId,
            @Param("activeProductId") Long activeProductId
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
    Optional<MemberActiveProduct> findActiveStopTarget(
            @Param("memberId") Long memberId,
            @Param("activeProductId") Long activeProductId
    );

    @Query("""
            select new com.ipillgood.server.domain.intake.repository.ActiveProductSettingsRow(
                ap.id,
                mp.id,
                p.id,
                p.brand,
                p.name,
                ap.startedOn,
                ap.notificationEnabled,
                ap.intakeTime,
                ap.frequency,
                ap.frequencyIntervalDays,
                ap.scheduleAnchorOn,
                count(pi.id),
                min(i.imageKey)
            )
            from MemberActiveProduct ap
            join ap.memberProduct mp
            join mp.product p
            left join ProductIngredient pi on pi.product = p
            left join pi.ingredient i
            where ap.member.id = :memberId
              and ap.id = :activeProductId
              and ap.stoppedOn is null
              and mp.deletedAt is null
              and p.deletedAt is null
            group by ap.id, mp.id, p.id, p.brand, p.name, ap.startedOn, ap.notificationEnabled,
                ap.intakeTime, ap.frequency, ap.frequencyIntervalDays, ap.scheduleAnchorOn
            """)
    Optional<ActiveProductSettingsRow> findActiveProductSettingsRow(
            @Param("memberId") Long memberId,
            @Param("activeProductId") Long activeProductId
    );

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

    boolean existsByMemberIdAndMemberProductIdAndStoppedOnIsNull(Long memberId, Long memberProductId);

    @Query("""
            select distinct new com.ipillgood.server.domain.intake.repository.CompatibilityConflictRow(
                ic.id,
                ic.type,
                currentIngredient.id,
                currentIngredient.name,
                targetIngredient.id,
                targetIngredient.name,
                ic.reason
            )
            from MemberActiveProduct ap
            join ap.memberProduct currentMemberProduct
            join currentMemberProduct.product currentProduct
            join ProductIngredient currentProductIngredient on currentProductIngredient.product = currentProduct
            join currentProductIngredient.ingredient currentIngredient
            join MemberProduct targetMemberProduct on targetMemberProduct.id = :targetMemberProductId
            join targetMemberProduct.product targetProduct
            join ProductIngredient targetProductIngredient on targetProductIngredient.product = targetProduct
            join targetProductIngredient.ingredient targetIngredient
            join IngredientCombination ic on (
                (ic.ingredientA = currentIngredient and ic.ingredientB = targetIngredient)
                or (ic.ingredientA = targetIngredient and ic.ingredientB = currentIngredient)
            )
            where ap.member.id = :memberId
              and ap.stoppedOn is null
              and currentMemberProduct.deletedAt is null
              and currentProduct.deletedAt is null
              and targetMemberProduct.member.id = :memberId
              and targetMemberProduct.deletedAt is null
              and targetProduct.deletedAt is null
              and ic.type in :types
            order by ic.id asc, currentIngredient.id asc, targetIngredient.id asc
            """)
    List<CompatibilityConflictRow> findCompatibilityConflicts(
            @Param("memberId") Long memberId,
            @Param("targetMemberProductId") Long targetMemberProductId,
            @Param("types") Collection<CombinationType> types
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
