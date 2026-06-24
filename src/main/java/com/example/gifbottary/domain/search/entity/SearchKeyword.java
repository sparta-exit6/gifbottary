package com.example.gifbottary.domain.search.entity;

import com.example.gifbottary.common.entity.BaseEntity;
import com.example.gifbottary.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "search_keyword", uniqueConstraints = {
        @UniqueConstraint(name = "uk_search_keyword_user_keyword", columnNames = {"user_id", "keyword"})
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchKeyword extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private String keyword;

    private Integer searchCount;

    private LocalDateTime lastSearchedAt;

    public SearchKeyword(User user, String keyword) {
        this.user = user;
        this.keyword = keyword;
        this.searchCount = 1;
        this.lastSearchedAt = LocalDateTime.now();
    }

    public void increaseCount() {
        this.searchCount++;
        this.lastSearchedAt = LocalDateTime.now();
    }

    public void refreshLastSearchedAt() {
        this.lastSearchedAt = LocalDateTime.now();
    }
}
