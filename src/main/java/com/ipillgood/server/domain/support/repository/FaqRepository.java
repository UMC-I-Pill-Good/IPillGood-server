package com.ipillgood.server.domain.support.repository;

import com.ipillgood.server.domain.support.entity.Faq;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FaqRepository extends JpaRepository<Faq, Long> {

    List<Faq> findByActiveTrueOrderByDisplayOrderAsc();
}
