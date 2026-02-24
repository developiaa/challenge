package pro.developia._2026_03;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;
import pro.developia._2026_03.domain.product.Product;
import pro.developia._2026_03.domain.product.ProductRepository;
import pro.developia._2026_03.domain.product.ProductStatus;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("shardingsphere") // application-shardingsphere.yaml 활성화
@ExtendWith(OutputCaptureExtension.class)
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

    @Test
    @DisplayName("샤딩 키(sellerId)가 없는 INSERT는 브로드캐스트되지 않고 즉시 에러가 발생한다")
    void testInsertWithoutShardingKeyThrowsException() {
        // given: 샤딩 키(sellerId)를 null로 세팅하여 객체 생성
        Product productWithoutSeller = Product.builder()
                .productId(UUID.randomUUID().toString())
                .sellerId(null)
                .category("ELECTRONICS")
                .productName("product")
                .salesStartDate(LocalDate.now())
                .salesEndDate(LocalDate.now().plusMonths(1))
                .productStatus(ProductStatus.AVAILABLE)
                .salesPrice(3000000)
                .brand("Test Brand")
                .manufacturer("Test Factory")
                .stockQuantity(100)
                .build();

        // when & then: save(INSERT)를 시도하면 예외가 터져야 성공하는 테스트
        Exception exception = assertThrows(Exception.class, () -> {
            productRepository.save(productWithoutSeller);
            productRepository.flush();
        });

        System.out.println("==================================================");
        System.out.println("예측된 에러가 정상적으로 발생했습니다!");
        System.out.println("에러 메시지: " + exception.getMessage());
        System.out.println("==================================================");
    }


    @Test
    @DisplayName("SELECT 라우팅 테스트: 샤딩키 유무에 따른 쿼리 횟수 검증")
    void testSelectRoutingWithCapture(CapturedOutput output) {
        // [사전 데이터 세팅] (이 과정에서도 INSERT SQL 로그가 찍힘)
        productRepository.save(createProduct(100L, "MacBook 1"));
        productRepository.save(createProduct(101L, "MacBook 2"));
        productRepository.save(createProduct(102L, "MacBook 3"));
        productRepository.flush();

        // ==========================================
        // [1-A] 샤딩 키 포함 (sellerId = 100)
        // ==========================================
        int preLength1 = output.getOut().length(); // 현재까지의 로그 길이 저장

        productRepository.findBySellerId(100L); // 실제 액션

        // 방금 실행한 액션 이후의 로그만 추출
        String logs1 = output.getOut().substring(preLength1);
        // "Actual SQL: ds_" 문자열이 몇 번 등장했는지 카운트
        int queryCount1 = logs1.split("Actual SQL: ds_").length - 1;

        System.out.println("[단일 라우팅] 발생한 물리 쿼리 수: " + queryCount1);
        assertThat(queryCount1).isEqualTo(1);


        // ==========================================
        // [1-B] 샤딩 키 미포함 (category = ELECTRONICS)
        // ==========================================
        int preLength2 = output.getOut().length(); // 현재까지의 로그 길이 다시 저장

        productRepository.findByCategory("ELECTRONICS"); // 실제 액션

        String logs2 = output.getOut().substring(preLength2);
        int queryCount2 = logs2.split("Actual SQL: ds_").length - 1;

        System.out.println("[브로드캐스트] 발생한 물리 쿼리 수: " + queryCount2);
        assertThat(queryCount2).isEqualTo(3);
    }

    @Test
    @DisplayName("UPDATE 라우팅 테스트: 샤딩키 유무에 따른 쿼리 횟수 검증")
    void testUpdateRoutingWithCapture(CapturedOutput output) {
        // [사전 데이터 세팅]
        productRepository.save(createProduct(100L, "MacBook 1"));
        productRepository.save(createProduct(101L, "MacBook 2"));
        productRepository.save(createProduct(102L, "MacBook 3"));
        productRepository.flush(); // 영속성 컨텍스트 비우기 (INSERT 쿼리 발생)

        // ==========================================
        // [2-A] 샤딩 키 포함 UPDATE (sellerId = 100)
        // ==========================================
        int preLength1 = output.getOut().length();

        productRepository.updatePriceBySellerId(100L, 5000); // 실제 액션

        String logs1 = output.getOut().substring(preLength1);
        int queryCount1 = logs1.split("Actual SQL: ds_").length - 1;

        System.out.println("[단일 UPDATE] 발생한 물리 쿼리 수: " + queryCount1);
        assertThat(queryCount1).isEqualTo(1); // 💡 검증: 정확히 1개의 DB(ds_1)만 타격해야 함!


        // ==========================================
        // [2-B] 샤딩 키 미포함 UPDATE (category = ELECTRONICS)
        // ==========================================
        int preLength2 = output.getOut().length();

        productRepository.updatePriceByCategory("ELECTRONICS", 9000); // 실제 액션

        String logs2 = output.getOut().substring(preLength2);
        int queryCount2 = logs2.split("Actual SQL: ds_").length - 1;

        System.out.println("[브로드캐스트 UPDATE] 발생한 물리 쿼리 수: " + queryCount2);
        assertThat(queryCount2).isEqualTo(3); // 💡 검증: 3개의 DB 모두에 UPDATE 쿼리가 뿌려져야 함!
    }

    @Test
    @DisplayName("DELETE 라우팅 테스트: 샤딩키 유무에 따른 쿼리 횟수 검증")
    void testDeleteRoutingWithCapture(CapturedOutput output) {
        // [사전 데이터 세팅]
        productRepository.save(createProduct(100L, "MacBook 1"));
        productRepository.save(createProduct(101L, "MacBook 2"));
        productRepository.save(createProduct(102L, "MacBook 3"));
        productRepository.flush();

        // ==========================================
        // [3-A] 샤딩 키 포함 DELETE (sellerId = 100)
        // ==========================================
        int preLength1 = output.getOut().length();

        productRepository.deleteBySellerId(100L); // 실제 액션

        String logs1 = output.getOut().substring(preLength1);
        int queryCount1 = logs1.split("Actual SQL: ds_").length - 1;

        System.out.println("[단일 DELETE] 발생한 물리 쿼리 수: " + queryCount1);
        assertThat(queryCount1).isEqualTo(1); // 💡 검증: 정확히 1개의 DB(ds_1)에서만 지워져야 함!


        // ==========================================
        // [3-B] 샤딩 키 미포함 DELETE (category = ELECTRONICS)
        // ==========================================
        int preLength2 = output.getOut().length();

        productRepository.deleteByCategory("ELECTRONICS"); // 실제 액션

        String logs2 = output.getOut().substring(preLength2);
        int queryCount2 = logs2.split("Actual SQL: ds_").length - 1;

        System.out.println("[브로드캐스트 DELETE] 발생한 물리 쿼리 수: " + queryCount2);
        assertThat(queryCount2).isEqualTo(3); // 💡 검증: 3개의 DB 전체에 DELETE 쿼리가 뿌려져야 함!
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
