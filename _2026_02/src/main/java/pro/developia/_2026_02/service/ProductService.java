package pro.developia._2026_02.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.developia._2026_02.annotation.Sharding;
import pro.developia._2026_02.domain.Product;
import pro.developia._2026_02.repository.ProductRepository;
import pro.developia._2026_02.service.dto.ProductDto;

@RequiredArgsConstructor
@Service
public class ProductService {
    private final ProductRepository productRepository;


    @Transactional
    @Sharding(key = "#productId")
    public ProductDto getProduct(String productId) {
        // 이 시점에 이미 ContextHolder에 "ds-N"이 세팅되어 있음.
        // LazyConnectionDataSourceProxy 덕분에 쿼리 실행 직전에 커넥션이 맺어지며 올바른 DB로 감.
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return ProductDto.from(product);
    }
}
