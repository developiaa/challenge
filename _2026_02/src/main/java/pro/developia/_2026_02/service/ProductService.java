package pro.developia._2026_02.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.developia._2026_02.annotation.Sharding;
import pro.developia._2026_02.domain.Product;
import pro.developia._2026_02.domain.ProductStatus;
import pro.developia._2026_02.repository.ProductRepository;
import pro.developia._2026_02.service.dto.ProductDto;
import pro.developia._2026_02.strategy.sharding.ShardingStrategyType;

import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductDto getOrDefault(String productId) {
        Product product = productRepository.findById(productId)
                .orElseGet(() -> Product.builder()
                        .productId("new" + productId)
                        .productName("test")
                        .manufacturer("test")
                        .salesPrice(10000)
                        .stockQuantity(100)
                        .brand("test")
                        .salesStartDate(LocalDate.now())
                        .salesEndDate(LocalDate.now().plusMonths(1))
                        .productStatus(ProductStatus.AVAILABLE)
                        .build());

        return ProductDto.from(product);
    }

    public void logProduct(String productId) {
        Optional<Product> product = productRepository.findById(productId);

        product.ifPresentOrElse(
                p -> log.info("{} exist", p.getProductName()),
                () -> log.info("not found")
        );
    }

    /**
     * hash sharding
     */
    @Transactional
    @Sharding(key = "#productId", strategy = ShardingStrategyType.HASH)
    public ProductDto getProduct(String productId) {
        // 이 시점에 이미 ContextHolder에 "ds-N"이 세팅되어 있음.
        // LazyConnectionDataSourceProxy 덕분에 쿼리 실행 직전에 커넥션이 맺어지며 올바른 DB로 감.
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return ProductDto.from(product);
    }

    @Transactional
    @Sharding(key = "#product.productId", strategy = ShardingStrategyType.HASH)
    public String saveProduct(Product product) {
        productRepository.save(product);
        return product.getProductId();
    }

    /*
     * range sharding
     */
    @Transactional(readOnly = true)
    @Sharding(key = "#sellerId", strategy = ShardingStrategyType.RANGE)
    public ProductDto getProductBySellerId(String productId, Long sellerId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return ProductDto.from(product);
    }

    @Transactional
    @Sharding(key = "#product.sellerId", strategy = ShardingStrategyType.RANGE)
    public String saveProductBySellerId(Product product) {
        productRepository.save(product);
        return product.getProductId();
    }
}
