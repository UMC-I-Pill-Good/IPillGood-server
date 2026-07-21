package com.ipillgood.server.domain.policy.repository;

import com.ipillgood.server.domain.policy.entity.MemberPolicyAgreement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberPolicyAgreementRepository extends JpaRepository<MemberPolicyAgreement, Long> {
}
