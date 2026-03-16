package pro.developia._2026_03_03.controller

import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pro.developia._2026_03_03.OrderCheckoutService
import pro.developia._2026_03_03.dto.OrderCheckoutRequest
import pro.developia._2026_03_03.dto.OrderResponse

@RestController
@RequestMapping("/api/v1/orders")
class OrderController(
    private val orderCheckoutService: OrderCheckoutService
) {
    @PatchMapping("/{id}/checkout")
    suspend fun checkoutOrder(
        @PathVariable id: Long,
        @RequestBody request: OrderCheckoutRequest
    ): OrderResponse {
        return orderCheckoutService.checkoutOrder(id, request)
    }

}
