package com.ipillgood.server.domain.cabinet.repository;

import com.ipillgood.server.domain.cabinet.entity.MemberProduct;
import com.ipillgood.server.domain.ingredient.entity.Ingredient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberProductRepository extends JpaRepository<MemberProduct, Long> {

    @Query(
            value = """
                    select new com.ipillgood.server.domain.cabinet.repository.CabinetProductCandidateRow(
                        p.id,
                        p.brand,
                        p.name,
                        count(distinct pi.id),
                        min(i.imageKey),
                        avg(pr.rating),
                        count(distinct pr.id),
                        case when count(distinct mp.id) > 0 then true else false end
                    )
                    from Product p
                    left join ProductIngredient searchPi on searchPi.product = p
                    left join searchPi.ingredient searchIngredient
                    left join ProductIngredient pi on pi.product = p
                    left join pi.ingredient i
                    left join ProductReview pr on pr.product = p
                        and pr.deletedAt is null
                    left join MemberProduct mp on mp.product = p
                        and mp.member.id = :memberId
                        and mp.deletedAt is null
                    where p.deletedAt is null
                      and (
                          :keyword is null
                          or lower(p.brand) like concat('%', :keyword, '%')
                          or lower(p.name) like concat('%', :keyword, '%')
                          or lower(searchIngredient.name) like concat('%', :keyword, '%')
                      )
                    group by p.id, p.brand, p.name
                    order by count(distinct pr.id) desc, p.name asc, p.id asc
                    """,
            countQuery = """
                    select count(distinct p.id)
                    from Product p
                    left join ProductIngredient searchPi on searchPi.product = p
                    left join searchPi.ingredient searchIngredient
                    where p.deletedAt is null
                      and (
                          :keyword is null
                          or lower(p.brand) like concat('%', :keyword, '%')
                          or lower(p.name) like concat('%', :keyword, '%')
                          or lower(searchIngredient.name) like concat('%', :keyword, '%')
                      )
                    """
    )
    Page<CabinetProductCandidateRow> findProductCandidatesOrderByReviewCountDesc(
            @Param("memberId") Long memberId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query(
            value = """
                    select new com.ipillgood.server.domain.cabinet.repository.CabinetProductCandidateRow(
                        p.id,
                        p.brand,
                        p.name,
                        count(distinct pi.id),
                        min(i.imageKey),
                        avg(pr.rating),
                        count(distinct pr.id),
                        case when count(distinct mp.id) > 0 then true else false end
                    )
                    from Product p
                    left join ProductIngredient searchPi on searchPi.product = p
                    left join searchPi.ingredient searchIngredient
                    left join ProductIngredient pi on pi.product = p
                    left join pi.ingredient i
                    left join ProductReview pr on pr.product = p
                        and pr.deletedAt is null
                    left join MemberProduct mp on mp.product = p
                        and mp.member.id = :memberId
                        and mp.deletedAt is null
                    where p.deletedAt is null
                      and (
                          :keyword is null
                          or lower(p.brand) like concat('%', :keyword, '%')
                          or lower(p.name) like concat('%', :keyword, '%')
                          or lower(searchIngredient.name) like concat('%', :keyword, '%')
                      )
                    group by p.id, p.brand, p.name
                    order by case when avg(pr.rating) is null then 1 else 0 end asc,
                        avg(pr.rating) desc, p.name asc, p.id asc
                    """,
            countQuery = """
                    select count(distinct p.id)
                    from Product p
                    left join ProductIngredient searchPi on searchPi.product = p
                    left join searchPi.ingredient searchIngredient
                    where p.deletedAt is null
                      and (
                          :keyword is null
                          or lower(p.brand) like concat('%', :keyword, '%')
                          or lower(p.name) like concat('%', :keyword, '%')
                          or lower(searchIngredient.name) like concat('%', :keyword, '%')
                      )
                    """
    )
    Page<CabinetProductCandidateRow> findProductCandidatesOrderByRatingDesc(
            @Param("memberId") Long memberId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
            select new com.ipillgood.server.domain.cabinet.repository.CabinetProductCandidateTagRow(
                p.id,
                ek.keyword
            )
            from Product p
            join ProductIngredient pi on pi.product = p
            join pi.ingredient i
            join EffectKeyword ek on ek.ingredient = i
            where p.id in :productIds
              and p.deletedAt is null
            order by p.id asc, pi.id asc, ek.id asc
            """)
    List<CabinetProductCandidateTagRow> findProductCandidateTags(
            @Param("productIds") Collection<Long> productIds
    );

    @Query("""
            select new com.ipillgood.server.domain.cabinet.repository.CabinetProductRow(
                mp.id,
                p.id,
                p.name,
                p.mfdsCertified,
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
            group by mp.id, p.id, p.name, p.mfdsCertified, mp.addedAt, ap.id
            order by mp.addedAt desc
            """)
    List<CabinetProductRow> findActiveCabinetProducts(@Param("memberId") Long memberId);

    @Query("""
            select new com.ipillgood.server.domain.cabinet.repository.CabinetReviewPromptRow(
                ap.id,
                p.id,
                p.name
            )
            from MemberActiveProduct ap
            join ap.memberProduct mp
            join mp.product p
            left join ProductReview pr on pr.product = p
                and pr.member.id = :memberId
                and pr.deletedAt is null
            where ap.member.id = :memberId
              and ap.stoppedOn is null
              and ap.startedOn <= :dueStartedOn
              and ap.reviewPromptDismissedAt is null
              and mp.deletedAt is null
              and p.deletedAt is null
              and pr.id is null
            order by ap.startedOn asc, p.name asc, ap.id asc
            """)
    List<CabinetReviewPromptRow> findDueReviewPrompts(
            @Param("memberId") Long memberId,
            @Param("dueStartedOn") LocalDate dueStartedOn
    );

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
            select mp
            from MemberProduct mp
            join fetch mp.product p
            where mp.member.id = :memberId
              and mp.id = :memberProductId
              and mp.deletedAt is null
              and p.deletedAt is null
            """)
    Optional<MemberProduct> findActiveIntakeRegistrationTarget(
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

    @Query("""
            select mp
            from MemberProduct mp
            join fetch mp.product p
            where mp.member.id = :memberId
              and mp.id in :memberProductIds
              and mp.deletedAt is null
              and p.deletedAt is null
            """)
    List<MemberProduct> findActiveProductsForDelete(
            @Param("memberId") Long memberId,
            @Param("memberProductIds") Collection<Long> memberProductIds
    );

    @Query("""
            select count(mp)
            from MemberProduct mp
                join mp.product p
            where mp.member.id = :memberId
                and mp.deletedAt is null
                and p.deletedAt is null
    """)
    long countOwnedProductsByMemberId(@Param("memberId") Long memberId);

    @Query("""
            select distinct pi.ingredient
            from MemberProduct mp
                join mp.product p
                join ProductIngredient pi on pi.product = p
            where mp.member.id = :memberId
                and mp.deletedAt is null
                and p.deletedAt is null
    """)
    List<Ingredient> findOwnedIngredientsByMemberId(@Param("memberId") Long memberId);
}
