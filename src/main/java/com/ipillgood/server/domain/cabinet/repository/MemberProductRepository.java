package com.ipillgood.server.domain.cabinet.repository;

import com.ipillgood.server.domain.cabinet.entity.MemberProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

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
}
