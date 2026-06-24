package com.example.gifbottary.domain.search.repository;

import com.example.gifbottary.domain.search.entity.SearchKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SearchKeywordRepository extends JpaRepository<SearchKeyword, Long> {

    List<SearchKeyword> findTop10ByUserIdOrderByLastSearchedAtDesc(Long userId);

    Optional<SearchKeyword> findByUserIdAndKeyword(Long userId, String keyword);

    void deleteByUserIdAndKeyword(Long userId, String keyword);

    void deleteAllByUserId(Long userId);
}
