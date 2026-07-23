package com.ipillgood.server.domain.notification.repository;

import com.ipillgood.server.domain.notification.entity.MemberPushToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberPushTokenRepository extends JpaRepository<MemberPushToken, Long> {
    Optional<MemberPushToken> findByToken(String token);

    Optional<MemberPushToken> findByIdAndMember_Id(Long id, Long memberId);
}
