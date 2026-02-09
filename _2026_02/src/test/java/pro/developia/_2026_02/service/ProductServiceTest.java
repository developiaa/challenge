package pro.developia._2026_02.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import pro.developia._2026_02.domain.Product;
import pro.developia._2026_02.domain.ProductStatus;
import pro.developia._2026_02.repository.ProductRepository;
import pro.developia._2026_02.service.dto.ProductDto;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class ProductServiceTest {
    @InjectMocks
    ProductService productService;
    @Mock
    ProductRepository productRepository;

    @DisplayName("상품이 있는 경우 조회한 값을 가져온다.")
    @Test
    void test1() {
        String productId = "test";
        LocalDate now = LocalDate.now();
        Product product = Product.builder()
                .productId(productId)
                .productName("test")
                .manufacturer("test")
                .salesPrice(10000)
                .stockQuantity(100)
                .brand("test")
                .salesStartDate(now)
                .salesEndDate(now.plusMonths(1))
                .productStatus(ProductStatus.AVAILABLE)
                .build();

        given(productRepository.findById(productId))
                .willReturn(Optional.of(product));

        ProductDto result = productService.getOrDefault(productId);

        assertThat(result.getProductId()).isEqualTo(productId);
    }

    @DisplayName("상품이 없는 경우 기본 상품을 가져온다.")
    @Test
    void test2() {
        String productId = "test";
        LocalDate now = LocalDate.now();
        Product product = Product.builder()
                .productId(productId)
                .productName("test")
                .manufacturer("test")
                .salesPrice(10000)
                .stockQuantity(100)
                .brand("test")
                .salesStartDate(now)
                .salesEndDate(now.plusMonths(1))
                .productStatus(ProductStatus.AVAILABLE)
                .build();

        given(productRepository.findById(productId))
                .willReturn(Optional.empty());

        ProductDto result = productService.getOrDefault(productId);

        assertThat(result.getProductId()).isEqualTo("new" + productId);
    }

    @Test
    void test3(CapturedOutput output) {
        String productId = "test";
        LocalDate now = LocalDate.now();
        Product product = Product.builder()
                .productId(productId)
                .productName("test")
                .manufacturer("test")
                .salesPrice(10000)
                .stockQuantity(100)
                .brand("test")
                .salesStartDate(now)
                .salesEndDate(now.plusMonths(1))
                .productStatus(ProductStatus.AVAILABLE)
                .build();

        given(productRepository.findById(productId))
                .willReturn(Optional.of(product));

        productService.logProduct(productId);

        assertThat(output.getOut()).contains("test exist");
    }

    @Test
    void test4(CapturedOutput output) {
        String productId = "test";

        given(productRepository.findById(productId))
                .willReturn(Optional.empty());

        productService.logProduct(productId);
        assertThat(output.getOut()).contains("not found");
    }
}
