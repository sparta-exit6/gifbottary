package com.example.gifbottary.domain.product.repository;

import com.example.gifbottary.domain.product.dto.request.ProductSearchRequest;
import com.example.gifbottary.domain.product.entity.GifticonSale;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * QueryDSL 대신 현재 단계에서 바로 사용할 수 있는 동적 검색 조건입니다.
 */
public final class GifticonSaleSpecification {

    private GifticonSaleSpecification() {
    }

    public static Specification<GifticonSale> publicSearch(ProductSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            if (!Long.class.equals(query.getResultType()) && !long.class.equals(query.getResultType())) {
                root.fetch("product", JoinType.INNER);
                root.fetch("seller", JoinType.INNER);
                query.distinct(true);
            }

            Join<Object, Object> productJoin = root.join("product", JoinType.INNER);
            List<Predicate> predicates = new ArrayList<>();

            if (request.keyword() != null && !request.keyword().isBlank()) {
                String keywordPattern = "%" + request.keyword().trim().toLowerCase() + "%";
                predicates.add(
                        criteriaBuilder.or(
                                criteriaBuilder.like(criteriaBuilder.lower(productJoin.get("productName")), keywordPattern),
                                criteriaBuilder.like(criteriaBuilder.lower(productJoin.get("brand")), keywordPattern)
                        )
                );
            }

            if (request.brand() != null && !request.brand().isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(productJoin.get("brand")),
                        "%" + request.brand().trim().toLowerCase() + "%"
                ));
            }

            if (request.saleType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("saleType"), request.saleType()));
            }

            if (request.saleStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("saleStatus"), request.saleStatus()));
            }

            if (request.minPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("salePrice"), request.minPrice()));
            }

            if (request.maxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("salePrice"), request.maxPrice()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<GifticonSale> sellerSearch(Long sellerId, ProductSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            if (!Long.class.equals(query.getResultType()) && !long.class.equals(query.getResultType())) {
                root.fetch("product", JoinType.INNER);
                root.fetch("seller", JoinType.INNER);
                query.distinct(true);
            }

            Join<Object, Object> sellerJoin = root.join("seller", JoinType.INNER);
            Join<Object, Object> productJoin = root.join("product", JoinType.INNER);
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(sellerJoin.get("id"), sellerId));

            if (request.keyword() != null && !request.keyword().isBlank()) {
                String keywordPattern = "%" + request.keyword().trim().toLowerCase() + "%";
                predicates.add(
                        criteriaBuilder.or(
                                criteriaBuilder.like(criteriaBuilder.lower(productJoin.get("productName")), keywordPattern),
                                criteriaBuilder.like(criteriaBuilder.lower(productJoin.get("brand")), keywordPattern)
                        )
                );
            }

            if (request.saleStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("saleStatus"), request.saleStatus()));
            }

            if (request.saleType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("saleType"), request.saleType()));
            }

            if (request.brand() != null && !request.brand().isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(productJoin.get("brand")),
                        "%" + request.brand().trim().toLowerCase() + "%"
                ));
            }

            if (request.minPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("salePrice"), request.minPrice()));
            }

            if (request.maxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("salePrice"), request.maxPrice()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
