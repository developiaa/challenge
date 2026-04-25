package pro.developia._2026_05.purchase.application.port.`in`

import pro.developia._2026_05.purchase.application.port.`in`.command.PlaceOrderCommand
import pro.developia._2026_05.purchase.domain.model.Order

interface PlaceOrderUseCase {
    suspend fun placeOrder(command: PlaceOrderCommand): Order
}
