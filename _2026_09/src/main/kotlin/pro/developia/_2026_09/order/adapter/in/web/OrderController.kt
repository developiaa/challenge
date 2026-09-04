package pro.developia._2026_09.order.adapter.`in`.web

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pro.developia._2026_09.order.application.OrderService

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService
) {
    @PostMapping
    fun placeOrder(@RequestBody request: OrderRequest): ResponseEntity<String> {
        val orderId = orderService.placeOrder(request.productId, request.quantity)
        return ResponseEntity.ok("주문 완료: $orderId")
    }
}

data class OrderRequest(
    val productId: String,
    val quantity: Int
)
