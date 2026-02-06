package pro.developia._2026_02.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.developia._2026_02.controller.request.CreateProductRequest;
import pro.developia._2026_02.service.ProductService;
import pro.developia._2026_02.service.dto.ProductDto;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping
    public ResponseEntity<String> createProduct(@RequestBody CreateProductRequest request) {
        String savedId = productService.saveProduct(request.toEntity());
        return ResponseEntity.ok(savedId);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable String productId) {
        ProductDto productDto = productService.getProduct(productId);
        return ResponseEntity.ok(productDto);
    }
}
