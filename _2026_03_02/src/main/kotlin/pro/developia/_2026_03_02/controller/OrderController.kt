package pro.developia._2026_03_02.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pro.developia._2026_03_02.controller.dto.OrderCreateRequest
import pro.developia._2026_03_02.controller.dto.OrderResponse
import pro.developia._2026_03_02.service.OrderService

@RestController
@RequestMapping("/api/v1/orders")
class OrderController(
    private val orderService: OrderService
) {
    @GetMapping("/{id}")
    fun getOrder(id: Long): OrderResponse {
        return orderService.getOrder(id)
    }

    @PostMapping
    fun createOrder(@RequestBody request: OrderCreateRequest): OrderResponse {
        return orderService.createOrder(request)
    }
}
