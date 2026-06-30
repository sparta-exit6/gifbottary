package com.example.gifbottary.infra.dummy;

import com.example.gifbottary.domain.product.enums.SaleStatus;
import com.example.gifbottary.domain.product.enums.SaleType;
import com.example.gifbottary.domain.user.entity.Role;
import com.example.gifbottary.domain.user.entity.User;
import com.example.gifbottary.domain.user.repository.UserRepository;
import com.example.gifbottary.infra.dummy.config.SearchDummyDataProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 앱 시작 시 조건부로 실행합니다.
 * 더미 판매자 생성 또는 재사용
 * 기존 더미 데이터 cleanup
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "dummy.search", name = "enabled", havingValue = "true")
public class SearchDummyDataLoader implements ApplicationRunner {

    private static final List<String> BRANDS = List.of(
            "스타벅스", "배스킨라빈스", "투썸플레이스", "올리브영", "메가커피",
            "이디야", "교촌치킨", "BBQ", "버거킹", "맥도날드"
    );

    private static final List<String> PRODUCT_SUFFIXES = List.of(
            "아메리카노", "카페라떼", "파인트", "치킨세트", "햄버거세트",
            "상품권", "디저트세트", "콜드브루", "샐러드", "케이크"
    );

    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SearchDummyDataProperties properties;

    @Override
    public void run(ApplicationArguments args) {
        validateProperties();

        Long sellerId = findOrCreateDummySeller();

        if (properties.isCleanupBeforeLoad()) {
            cleanupExistingDummyData();
        }

        insertProductsAndSales(sellerId);
    }

    private void validateProperties() {
        if (properties.getSize() < 1) {
            throw new IllegalArgumentException("dummy.search.size 는 1 이상이어야 합니다.");
        }

        if (properties.getBatchSize() < 1) {
            throw new IllegalArgumentException("dummy.search.batch-size 는 1 이상이어야 합니다.");
        }

        if (properties.getProductNamePrefix() == null || properties.getProductNamePrefix().isBlank()) {
            throw new IllegalArgumentException("dummy.search.product-name-prefix 는 비어 있을 수 없습니다.");
        }
    }

    private Long findOrCreateDummySeller() {
        return userRepository.findByEmail(properties.getSellerEmail())
                .map(User::getId)
                .orElseGet(() -> {
                    User user = User.builder()
                            .email(properties.getSellerEmail())
                            .password(passwordEncoder.encode("dummy-password"))
                            .name(properties.getSellerName())
                            .role(Role.ADMIN)
                            .pointBalance(0)
                            .build();

                    return userRepository.save(user).getId();
                });
    }

    private void cleanupExistingDummyData() {
        String likePattern = properties.getProductNamePrefix() + "-%";

        int deletedSales = jdbcTemplate.update(
                """
                delete from gifticon_sale
                where product_id in (
                    select id from gifticon_product where product_name like ?
                )
                """,
                likePattern
        );

        int deletedProducts = jdbcTemplate.update(
                "delete from gifticon_product where product_name like ?",
                likePattern
        );

        log.info("기존 더미 데이터 삭제 완료 - sales: {}, products: {}", deletedSales, deletedProducts);
    }

    private void insertProductsAndSales(Long sellerId) {
        String productSql = """
                insert into gifticon_product
                (brand, product_name, face_value, image_url, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?)
                """;

        String saleSql = """
                insert into gifticon_sale
                (seller_id, product_id, sale_type, sale_status, sale_price, expire_at, stock, created_at, updated_at)
                select ?, id, ?, ?, ?, ?, ?, ?, ?
                from gifticon_product
                where brand = ? and product_name = ?
                """;

        int total = properties.getSize();
        int batchSize = properties.getBatchSize();

        for (int start = 0; start < total; start += batchSize) {
            int end = Math.min(start + batchSize, total);

            List<Object[]> productArgs = new ArrayList<>(end - start);
            List<Object[]> saleArgs = new ArrayList<>(end - start);

            for (int i = start; i < end; i++) {
                String brand = BRANDS.get(i % BRANDS.size());
                String suffix = PRODUCT_SUFFIXES.get(i % PRODUCT_SUFFIXES.size());
                String productName = properties.getProductNamePrefix() + "-" + i + "-" + suffix;

                int faceValue = 3000 + (i % 20) * 500;
                int salePrice = Math.max(1000, faceValue - 300 - (i % 5) * 100);
                int stock = 10 + (i % 20);

                LocalDate expireAt = LocalDate.now().plusDays(30L + (i % 365));
                LocalDateTime now = LocalDateTime.now();
                String imageUrl = "https://dummy.example.com/products/" + i + ".png";

                productArgs.add(new Object[]{
                        brand,
                        productName,
                        faceValue,
                        imageUrl,
                        now,
                        now
                });

                saleArgs.add(new Object[]{
                        sellerId,
                        SaleType.PLATFORM.name(),
                        SaleStatus.ON_SALE.name(),
                        salePrice,
                        expireAt,
                        stock,
                        now,
                        now,
                        brand,
                        productName
                });
            }

            jdbcTemplate.batchUpdate(productSql, productArgs);
            jdbcTemplate.batchUpdate(saleSql, saleArgs);

            log.info("더미 데이터 적재 진행 중... {} / {}", end, total);
        }

        log.info("검색 성능 테스트용 더미 데이터 적재 완료 - 총 {}건", total);
    }
}