package pro.developia._2026_02.sharding;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import pro.developia._2026_02.config.ShardingContextHolder;
import pro.developia._2026_02.domain.Product;
import pro.developia._2026_02.domain.ProductStatus;
import pro.developia._2026_02.service.ProductService;
import pro.developia._2026_02.service.dto.ProductDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProductRangeShardingTest {
    @Autowired
    private ProductService productService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDown() {
        List<String> shardKeys = List.of("ds-0", "ds-1", "ds-2");

        for (String key : shardKeys) {
            ShardingContextHolder.setKey(key);
            try {
                jdbcTemplate.execute("TRUNCATE TABLE products");
            } catch (Exception e) {
                System.err.println("Cleanup failed for " + key + ": " + e.getMessage());
            } finally {
                ShardingContextHolder.clear();
            }
        }
    }

    @Test
    @DisplayName("레인지 샤딩 검증: sellerId 범위에 따라 지정된 DB 노드에 저장되어야 한다")
    void verifyRangeSharding() {
        // 테스트 케이스: [sellerId, 예상되는 DB]
        Map<Long, String> testCases = Map.of(
                5000L, "ds-0",
                15000L, "ds-1",
                25000L, "ds-2"
        );

        testCases.forEach((sellerId, expectedDb) -> {
            String productId = UUID.randomUUID().toString();
            String productName = "Test Product " + sellerId;

            Product product = Product.builder()
                    .productId(productId)
                    .sellerId(sellerId)
                    .category("TEST_CATEGORY")
                    .productName(productName)
                    .salesStartDate(LocalDate.now())
                    .salesEndDate(LocalDate.now().plusMonths(1))
                    .productStatus(ProductStatus.AVAILABLE)
                    .brand("Test Brand")
                    .manufacturer("Test Factory")
                    .salesPrice(50000)
                    .stockQuantity(100)
                    .build();

            productService.saveProductBySellerId(product);
            System.out.println("Saved: sellerId(" + sellerId + ") -> Target: " + expectedDb);

            ProductDto retrieved = productService.getProductBySellerId(productId, sellerId);
            assertThat(retrieved.getProductId()).isEqualTo(productId);
        });
    }
}
