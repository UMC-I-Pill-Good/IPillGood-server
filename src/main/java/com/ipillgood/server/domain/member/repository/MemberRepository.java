package com.ipillgood.server.domain.member.repository;

import com.ipillgood.server.domain.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByUsername(String username);

    Optional<Member> findByUsername(String username);

    Optional<Member> findByEmail(String email);
}
