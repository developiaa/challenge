package pro.developia._2026_05.catalog.adapter.`in`.web

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pro.developia._2026_05.catalog.adapter.`in`.web.dto.CatalogListResponse
import pro.developia._2026_05.catalog.adapter.`in`.web.dto.CatalogResponse
import pro.developia._2026_05.catalog.application.port.`in`.ViewCatalogUseCase

@RestController
@RequestMapping("/catalogs")
class CatalogController(
    private val viewCatalogUseCase: ViewCatalogUseCase
) {
    @GetMapping("/{productId}")
    suspend fun getCatalog(@PathVariable productId: Long): ResponseEntity<CatalogResponse> {
        val result = viewCatalogUseCase.viewSingleCatalog(productId)
        return ResponseEntity.ok(CatalogResponse.from(result))
    }

    @GetMapping
    suspend fun getCatalogs(
        @RequestParam(required = false) cursorId: Long?,
        @RequestParam(defaultValue = "20") limit: Int
    ): ResponseEntity<CatalogListResponse> {
        val items = viewCatalogUseCase.viewCatalogs(cursorId, limit)
        val nextCursor = items.lastOrNull()?.productId

        return ResponseEntity.ok(
            CatalogListResponse(
                items = items.map { CatalogResponse.from(it) },
                nextCursor = nextCursor
            )
        )
    }
}
