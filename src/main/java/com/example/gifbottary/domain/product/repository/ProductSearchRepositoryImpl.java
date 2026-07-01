package com.example.gifbottary.domain.product.repository;

import com.example.gifbottary.domain.product.dto.request.ProductSearchRequest;
import com.example.gifbottary.domain.product.dto.response.MyProductSummaryResponse;
import com.example.gifbottary.domain.product.dto.response.ProductSummaryResponse;
import com.example.gifbottary.domain.product.entity.QGifticonProduct;
import com.example.gifbottary.domain.product.entity.QGifticonSale;
import com.example.gifbottary.domain.product.enums.SaleStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 상품 검색/목록 조회를 위한 QueryDSL 구현체입니다.
 */
@RequiredArgsConstructor
public class ProductSearchRepositoryImpl implements ProductSearchRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ProductSummaryResponse> searchProducts(ProductSearchRequest request, Pageable pageable) {
        QGifticonSale sale = QGifticonSale.gifticonSale;
        QGifticonProduct product = QGifticonProduct.gifticonProduct;

        // BooleanBuilder : 조건이 있을 때만 동적으로 붙음
        BooleanBuilder condition = new BooleanBuilder();

        // 공개 검색은 판매 가능 상품만 노출
        condition.and(sale.saleStatus.eq(SaleStatus.ON_SALE));

        // contains() -> LIKE '%keyword%'
        if (request.hasKeyword()) {
            String keyword = request.normalizedKeyword();
            condition.and(
                    product.productName.contains(keyword)
                            .or(product.brand.contains(keyword))
            );
        }

        if (request.hasBrand()) {
            condition.and(product.brand.contains(request.normalizedBrand()));
        }

        if (request.minPrice() != null) {
            condition.and(sale.salePrice.goe(request.minPrice()));
        }

        if (request.maxPrice() != null) {
            condition.and(sale.salePrice.loe(request.maxPrice()));
        }

        List<ProductSummaryResponse> content = queryFactory
                .select(Projections.constructor(
                        ProductSummaryResponse.class,
                        sale.id,
                        product.id,
                        sale.saleType,
                        product.brand,
                        product.productName,
                        product.faceValue,
                        sale.expireAt,
                        sale.salePrice,
                        sale.stock,
                        sale.saleStatus,
                        product.imageUrl
                ))
                .from(sale)
                .join(sale.product, product)
                .where(condition)
                .offset(pageable.getOffset())  // 시작 위치
                .limit(pageable.getPageSize()) // 개수
                .orderBy(sale.createdAt.desc())
                .fetch();

        Long total = queryFactory
                .select(sale.count())
                .from(sale)
                .join(sale.product, product)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }

    @Override
    public Page<MyProductSummaryResponse> searchMyProducts(Long sellerId, ProductSearchRequest request, Pageable pageable) {
        QGifticonSale sale = QGifticonSale.gifticonSale;
        QGifticonProduct product = QGifticonProduct.gifticonProduct;

        BooleanBuilder condition = new BooleanBuilder();

        // 내 판매글 조회는 판매자 기준으로 필터링합니다.
        condition.and(sale.seller.id.eq(sellerId));

        if (request.hasKeyword()) {
            String keyword = request.normalizedKeyword();
            condition.and(
                    product.productName.contains(keyword)
                            .or(product.brand.contains(keyword))
            );
        }

        if (request.hasBrand()) {
            condition.and(product.brand.contains(request.normalizedBrand()));
        }

        if (request.minPrice() != null) {
            condition.and(sale.salePrice.goe(request.minPrice()));
        }

        if (request.maxPrice() != null) {
            condition.and(sale.salePrice.loe(request.maxPrice()));
        }

        List<MyProductSummaryResponse> content = queryFactory
                .select(Projections.constructor(
                        MyProductSummaryResponse.class,
                        sale.id,
                        product.id,
                        sale.saleType,
                        product.brand,
                        product.productName,
                        sale.salePrice,
                        sale.saleStatus,
                        sale.expireAt,
                        sale.createdAt
                ))
                .from(sale)
                .join(sale.product, product)
                .where(condition)
                .orderBy(sale.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(sale.count())
                .from(sale)
                .join(sale.product, product)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }
}
