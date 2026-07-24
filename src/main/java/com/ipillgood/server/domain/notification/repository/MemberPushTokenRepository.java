package com.ipillgood.server.domain.notification.repository;

import com.ipillgood.server.domain.notification.entity.MemberPushToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberPushTokenRepository extends JpaRepository<MemberPushToken, Long> {
    Optional<MemberPushToken> findByToken(String token);

    Optional<MemberPushToken> findByIdAndMember_Id(Long id, Long memberId);

    @Query("""
            select token
            from MemberPushToken token
            join fetch token.member member
            where member.id in :memberIds
              and token.active = true
            order by member.id asc, token.id asc
            """)
    List<MemberPushToken> findActiveTokensByMemberIds(@Param("memberIds") Collection<Long> memberIds);

    @Query("""
            select token
            from MemberPushToken token
            join fetch token.member member
            left join MemberNotificationSetting setting on setting.member = member
            where token.active = true
              and member.onboardingCompletedAt is not null
              and (setting.memberId is null or setting.pushEnabled = true)
              and not exists (
                  select record.id
                  from ConditionWeeklyRecord record
                  where record.member = member
                    and record.weekStartOn = :weekStartOn
              )
            order by member.id asc, token.id asc
            """)
    List<MemberPushToken> findConditionCheckDeliveryTokens(@Param("weekStartOn") LocalDate weekStartOn);
}
