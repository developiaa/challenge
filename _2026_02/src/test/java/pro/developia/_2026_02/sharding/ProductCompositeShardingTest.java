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

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ProductCompositeShardingTest {
    @Autowired
    ProductService productService;
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
    @DisplayName("복합 샤딩 검증: 모든 DB 노드(ds-0,1,2)가 전략에 따라 활용되어야 한다")
    void verifyAllNodesUsed() {
        int sumCount = 10;
        int count = 5;

        // ds-0, ds-1 분산
        for (int i = 0; i < sumCount; i++) {
            String pId = UUID.randomUUID().toString();
            // ELECTRONICS는 ds-0 또는 ds-1로 가야 함
            productService.saveProductComposite(
                    Product.builder()
                            .productId(pId)
                            .category("ELECTRONICS") // ds-0 또는 ds-1로 라우팅됨
                            .productName("MacBook Pro " + i)
                            .sellerId(100L)
                            .productStatus(ProductStatus.AVAILABLE)
                            .salesPrice(3000000)
                            .stockQuantity(50)
                            .build()
            );
        }

        // ds-2 집중
        for (int i = 0; i < count; i++) {
            String pId = UUID.randomUUID().toString();
            // FASHION은 DEFAULT 그룹인 ds-2로 가야 함
            productService.saveProductComposite(
                    Product.builder()
                            .productId(pId)
                            .category("FASHION") // ds-2 라우팅됨
                            .productName("Cloth-" + i)
                            .sellerId(100L)
                            .productStatus(ProductStatus.AVAILABLE)
                            .salesPrice(3000000)
                            .stockQuantity(50)
                            .build()
            );
        }

        // ds-0: N개, ds-1: M개 (N+M = 10)
        // ds-2: 5개
        ShardingContextHolder.setKey("ds-0");
        int resultSum = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM products", Integer.class);
        ShardingContextHolder.setKey("ds-1");
        resultSum += jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM products", Integer.class);
        ShardingContextHolder.setKey("ds-2");
        int resultCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM products", Integer.class);

        assertThat(resultSum).isEqualTo(sumCount);
        assertThat(resultCount).isEqualTo(count);
    }


}
