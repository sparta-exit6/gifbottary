package com.example.gifbottary.domain.search.repository;

import com.example.gifbottary.domain.search.entity.SearchKeyword;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SearchKeywordRepository extends JpaRepository<SearchKeyword, Long> {

    List<SearchKeyword> findTop10ByUser_IdOrderByLastSearchedAtDesc(Long userId);

    Optional<SearchKeyword> findByUser_IdAndKeyword(Long userId, String keyword);

    void deleteByUser_IdAndKeyword(Long userId, String keyword);

    void deleteAllByUser_Id(Long userId);

    @Modifying
    @Query("""
    update SearchKeyword sk
    set sk.searchCount = sk.searchCount + 1,
        sk.lastSearchedAt = :lastSearchedAt
    where sk.user.id = :userId
      and sk.keyword = :keyword
""")
    int increaseCount(@Param("userId") Long userId,
                      @Param("keyword") String keyword,
                      @Param("lastSearchedAt") LocalDateTime lastSearchedAt);
}
