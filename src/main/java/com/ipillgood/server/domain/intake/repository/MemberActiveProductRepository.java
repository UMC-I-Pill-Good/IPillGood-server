package com.ipillgood.server.domain.intake.repository;

import com.ipillgood.server.domain.intake.entity.MemberActiveProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

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
}
