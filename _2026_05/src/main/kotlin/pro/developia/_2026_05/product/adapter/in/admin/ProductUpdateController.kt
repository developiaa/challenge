package pro.developia._2026_05.product.adapter.`in`.admin

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import pro.developia._2026_05.product.adapter.`in`.admin.dto.ProductUpdateRequest
import pro.developia._2026_05.product.adapter.`in`.admin.dto.ProductUpdateResponse
import pro.developia._2026_05.product.application.port.`in`.ManageProductUseCase

@RestController
class ProductUpdateController(
    private val manageProductUseCase: ManageProductUseCase
) {
    @PutMapping("/{id}")
    suspend fun updateProduct(
        @PathVariable id: Long,
        @RequestBody productUpdateRequest: ProductUpdateRequest
    ): ResponseEntity<ProductUpdateResponse> {
        val result = manageProductUseCase.updateProduct(productUpdateRequest.toCommand())
        return ResponseEntity.ok(ProductUpdateResponse.from(result))
    }
}
