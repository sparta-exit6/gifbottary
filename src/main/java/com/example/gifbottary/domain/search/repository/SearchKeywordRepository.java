package com.example.gifbottary.domain.search.repository;

import com.example.gifbottary.domain.search.entity.SearchKeyword;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SearchKeywordRepository extends JpaRepository<SearchKeyword, Long> {

    List<SearchKeyword> findTop10ByUser_IdOrderByLastSearchedAtDesc(Long userId);

    Optional<SearchKeyword> findByUser_IdAndKeyword(Long userId, String keyword);

    void deleteByUser_IdAndKeyword(Long userId, String keyword);

    void deleteAllByUser_Id(Long userId);

    @Query("""
            select sk.keyword as keyword, sum(sk.searchCount) as totalCount
            from SearchKeyword sk
            group by sk.keyword
            order by sum(sk.searchCount) desc
            """)
    List<PopularKeywordProjection> findPopularKeywords(Pageable pageable);

    interface PopularKeywordProjection {
        String getKeyword();
        Long getTotalCount();
    }
}
