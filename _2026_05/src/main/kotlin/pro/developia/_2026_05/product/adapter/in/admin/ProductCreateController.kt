package pro.developia._2026_05.product.adapter.`in`.admin

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import pro.developia._2026_05.product.adapter.`in`.admin.dto.ProductCreateRequest
import pro.developia._2026_05.product.adapter.`in`.admin.dto.ProductCreateResponse
import pro.developia._2026_05.product.application.port.`in`.ManageProductUseCase

@RestController
class ProductCreateController(
    private val manageProductUseCase: ManageProductUseCase
) {
    @PostMapping("/products")
    suspend fun createProduct(@RequestBody productCreateRequest: ProductCreateRequest): ResponseEntity<ProductCreateResponse> {
        val result = manageProductUseCase.createProduct(productCreateRequest.toCommand())
        return ResponseEntity.ok(ProductCreateResponse.from(result))
    }
}
