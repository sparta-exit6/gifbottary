package com.example.gifbottary.domain.product.repository;

import com.example.gifbottary.domain.product.dto.request.ProductSearchRequest;
import com.example.gifbottary.domain.product.dto.response.MyProductSummaryResponse;
import com.example.gifbottary.domain.product.dto.response.ProductSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductSearchRepository {

    /**
     * 공개 상품 검색입니다.
     * 공개 조회에서는 판매 가능한 상품만 노출합니다.
     */
    Page<ProductSummaryResponse> searchProducts(ProductSearchRequest request, Pageable pageable);


    /**
     * 내 판매글 조회입니다.
     * @param sellerId 기준으로 내 상품 목록을 조회합니다.
     * @param request
     * @param pageable
     * @return
     */
    Page<MyProductSummaryResponse> searchMyProducts(Long sellerId, ProductSearchRequest request, Pageable pageable);
}
