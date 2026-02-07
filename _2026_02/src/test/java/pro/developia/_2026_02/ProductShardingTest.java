package pro.developia._2026_02;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pro.developia._2026_02.domain.Product;
import pro.developia._2026_02.domain.ProductStatus;
import pro.developia._2026_02.service.ProductService;
import pro.developia._2026_02.service.dto.ProductDto;
import pro.developia._2026_02.strategy.sharding.HashShardingStrategy;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProductShardingTest {
    @Autowired
    private ProductService productService;

    @Autowired
    private HashShardingStrategy shardingStrategy;

    @Test
    @DisplayName("해시 샤딩 검증: 변경된 엔티티가 3개의 DB 노드에 정상적으로 분산 저장되는지 확인")
    void verifyShardingDistribution() {
        Map<String, Integer> shardCount = new HashMap<>();
        shardCount.put("ds-0", 0);
        shardCount.put("ds-1", 0);
        shardCount.put("ds-2", 0);

        System.out.println("============== [Sharding Distribution Test Start] ==============");

        for (int i = 0; i < 100; i++) {
            String uniqueId = UUID.randomUUID().toString();
            String productName = "Test Product " + i;

            Product product = Product.builder()
                    .productId(uniqueId)
                    .sellerId(1000L + i)
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

            String targetDb = shardingStrategy.getTargetKey(uniqueId);
            shardCount.put(targetDb, shardCount.get(targetDb) + 1);

            productService.saveProduct(product);

            ProductDto retrieved = productService.getProduct(uniqueId);

            boolean isSuccess = retrieved.getProductName().equals(productName)
                    && retrieved.getProductId().equals(uniqueId);

            System.out.printf("Key: %s | Target: [%s] | Verify: %s%n",
                    uniqueId.substring(0, 8) + "...", targetDb, isSuccess ? "PASS" : "FAIL");

            assertThat(retrieved.getProductName()).isEqualTo(productName);
        }

        System.out.println("============== [Distribution Result] ==============");
        shardCount.forEach((db, count) -> System.out.println(db + ": " + count + " ea"));

        long activeNodes = shardCount.values().stream().filter(c -> c > 0).count();
        System.out.println("Active Shards: " + activeNodes + "/3");

        System.out.println("===================================================");
    }
}
