package pro.developia._2026_03_02.service

import org.springframework.stereotype.Service
import pro.developia._2026_03_02.OrderRepository

@Service
class OrderService (
    private val orderRepository: OrderRepository
){
}
