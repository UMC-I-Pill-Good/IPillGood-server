package com.ipillgood.server.domain.search.entity;

import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.*;
import org.hibernate.annotations.OnDelete;                                                                                                                                   
import org.hibernate.annotations.OnDeleteAction;


import java.time.LocalDateTime;

// 회원 최근 검색어
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(
        name = "member_search_keyword",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_member_search_keyword",
                columnNames = {"member_id", "keyword"}
        )
)
public class MemberSearchKeyword extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @Column(name = "keyword", nullable = false, length = 100)
    private String keyword;

    @Column(name = "searched_at", nullable = false)
    private LocalDateTime searchedAt;

    public void updateSearchedAt(LocalDateTime searchedAt) {
        this.searchedAt = searchedAt;
    }
}
