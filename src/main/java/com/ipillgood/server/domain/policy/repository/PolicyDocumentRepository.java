package com.ipillgood.server.domain.policy.repository;

import com.ipillgood.server.domain.policy.entity.PolicyDocument;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyDocumentRepository extends JpaRepository<PolicyDocument, Long> {

    List<PolicyDocument> findByActiveTrue();

    List<PolicyDocument> findByActiveTrueAndRequiredTrue();

    List<PolicyDocument> findByIdInAndActiveTrue(Collection<Long> ids);
}
