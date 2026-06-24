package com.example.gifbottary.domain.search.repository;

import com.example.gifbottary.domain.search.entity.SearchKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SearchKeywordRepository extends JpaRepository<SearchKeyword, Long> {

    List<SearchKeyword> findTop10ByUser_IdOrderByLastSearchedAtDesc(Long userId);

    Optional<SearchKeyword> findByUser_IdAndKeyword(Long userId, String keyword);

    void deleteByUser_IdAndKeyword(Long userId, String keyword);

    void deleteAllByUser_Id(Long userId);
}
