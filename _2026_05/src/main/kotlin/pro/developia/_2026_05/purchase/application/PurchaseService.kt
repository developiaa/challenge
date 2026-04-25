package pro.developia._2026_05.purchase.application

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pro.developia._2026_05.purchase.application.port.`in`.PlaceOrderUseCase
import pro.developia._2026_05.purchase.application.port.`in`.command.PlaceOrderCommand
import pro.developia._2026_05.purchase.application.port.out.OrderPersistencePort
import pro.developia._2026_05.purchase.domain.model.Order
import pro.developia._2026_05.purchase.domain.model.OrderLineItem

@Service
class PurchaseService(
//    private val idempotencyPort: IdempotencyPort, // Redis
//    private val inventoryPort: InventoryPort,     // Redis (Lua Script)
    private val orderPersistencePort: OrderPersistencePort, // R2DBC
//    private val outboxEventPort: OutboxEventPort  // R2DBC (Transactional Outbox)
) : PlaceOrderUseCase {

    @Transactional // R2DBC 트랜잭션 활성화 (Order와 Outbox를 원자적으로 묶음)
    override suspend fun placeOrder(command: PlaceOrderCommand): Order {
        // 1. 멱등성 검증 (Redis SetNx 활용)
//        if (!idempotencyPort.tryLock(command.idempotencyKey)) {
//            throw IllegalStateException("이미 처리 중이거나 완료된 주문입니다. (중복 요청)")
//        }

//        try {
            // 2. 도메인 객체 생성
            val orderItems = command.items.map {
                OrderLineItem(it.productId, it.quantity, it.price)
            }
            val totalAmount = orderItems.sumOf { it.price.multiply(it.quantity.toBigDecimal()) }
            val newOrder = Order(customerId = command.customerId, totalAmount = totalAmount, items = orderItems)

            // 3. 주문 DB 저장 (PENDING)
            val savedOrder = orderPersistencePort.saveOrder(newOrder)

//            // 4. 재고 차감 (Redis 기반 고속 차감)
//            // 실패 시 Exception 발생하여 트랜잭션 롤백 및 멱등성 키 해제됨
//            inventoryPort.reserveStock(savedOrder.id!!, orderItems)
//
//            // 5. 상태 변경 (재고 확보됨)
//            val reservedOrder = savedOrder.markAsStockReserved()
//            orderPersistencePort.updateOrderStatus(reservedOrder.id, reservedOrder.status)
//
//            // 6. Transactional Outbox 패턴으로 결제(Payment) 요청 이벤트 저장
//            // Kafka로 바로 쏘지 않고 DB에 이벤트를 저장하여 트랜잭션 원자성 보장
//            outboxEventPort.saveEvent(
//                aggregateId = reservedOrder.id.toString(),
//                eventType = "ORDER_STOCK_RESERVED",
//                payload = reservedOrder // JSON 직렬화되어 저장됨
//            )

            return savedOrder

//        } catch (e: Exception) {
//            // 실패 시 멱등성 키를 즉시 해제하여 재시도 가능하도록 처리
//            idempotencyPort.releaseLock(command.idempotencyKey)
//            throw e
//        }
    }
}

