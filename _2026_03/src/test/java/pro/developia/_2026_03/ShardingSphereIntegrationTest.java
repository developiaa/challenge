package pro.developia._2026_03;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import pro.developia._2026_03.domain.Product;
import pro.developia._2026_03.domain.ProductRepository;
import pro.developia._2026_03.domain.ProductStatus;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("shardingsphere") // application-shardingsphere.yaml 활성화
public class ShardingSphereIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @AfterEach
    void tearDown() {
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("ShardingSphere 자동 라우팅 및 결과 병합(Merge) 테스트")
    void testShardingSphereMagic() {
        // 1. 기존 데이터 초기화 (전체 샤드에 Delete 쿼리가 브로드캐스팅 됨)
        productRepository.deleteAll();

        // 2. 데이터 Insert (sellerId 값에 따라 알아서 3개의 DB로 흩어짐)
        // sellerId % 3
        // 100 % 3 = 1 -> ds-1
        // 101 % 3 = 2 -> ds-2
        // 102 % 3 = 0 -> ds-0
        productRepository.save(createProduct(100L, "MacBook 1"));
        productRepository.save(createProduct(101L, "MacBook 2"));
        productRepository.save(createProduct(102L, "MacBook 3"));

        // 3. 마법의 순간: 결과 병합 (Merge)
        // JPA는 "SELECT COUNT(*) FROM products"를 한 번만 날리지만,
        // ShardingSphere가 3개 DB로 쪼개서 날린 뒤, 결과를 더해서 3을 반환함!
        long totalCount = productRepository.count();

        System.out.println("전체 상품 개수: " + totalCount);
        assertThat(totalCount).isEqualTo(3);
    }

    private Product createProduct(Long sellerId, String name) {
        return Product.builder()
                .productId(UUID.randomUUID().toString())
                .sellerId(sellerId)
                .category("ELECTRONICS")
                .productName(name)
                .salesStartDate(LocalDate.now())
                .salesEndDate(LocalDate.now().plusMonths(1))
                .productStatus(ProductStatus.AVAILABLE)
                .salesPrice(3000000)
                .brand("Test Brand")
                .manufacturer("Test Factory")
                .stockQuantity(100)
                .build();
    }
}
