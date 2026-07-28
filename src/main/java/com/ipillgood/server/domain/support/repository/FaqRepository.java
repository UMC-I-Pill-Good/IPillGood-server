package com.ipillgood.server.domain.support.repository;

import com.ipillgood.server.domain.support.entity.Faq;
import com.ipillgood.server.domain.support.entity.enums.FaqCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FaqRepository extends JpaRepository<Faq, Long> {

    List<Faq> findByActiveTrueOrderByDisplayOrderAsc();

    // 관리자 FAQ 목록 조회 - 카테고리/키워드(질문 부분 일치) 조건, 최신 등록순
    @Query("""
            select f from Faq f
            where (:category is null or f.category = :category)
              and (:keyword is null or lower(f.question) like concat('%', cast(:keyword as string), '%'))
            order by f.id desc
            """)
    Page<Faq> searchForAdmin(
            @Param("category") FaqCategory category,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // 관리자 FAQ 등록 - 사용자 화면 노출 순서(displayOrder)는 마지막 순번 다음으로 배정
    @Query("select coalesce(max(f.displayOrder), 0) from Faq f")
    int findMaxDisplayOrder();
}
