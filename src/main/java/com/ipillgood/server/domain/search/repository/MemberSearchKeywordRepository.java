package com.ipillgood.server.domain.search.repository;

import com.ipillgood.server.domain.search.entity.MemberSearchKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberSearchKeywordRepository extends JpaRepository<MemberSearchKeyword, Long> {

    List<MemberSearchKeyword> findTop10ByMemberIdOrderBySearchedAtDesc(Long memberId);
}
