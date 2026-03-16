package pro.developia._2026_03_03

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.extension.ExtendWith
import pro.developia._2026_03_03.domain.Order
import pro.developia._2026_03_03.domain.OrderStatus
import pro.developia._2026_03_03.dto.OrderCheckoutRequest
import pro.developia._2026_03_03.repository.OrderRepository
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class OrderCheckoutServiceTest {
    @MockK
    private lateinit var orderRepository: OrderRepository

    @InjectMockKs
    private lateinit var orderCheckoutService: OrderCheckoutService

    @Test
    // suspend 함수를 호출하려면 반드시 runTest 블록 안에서 실행
    fun `결제 대기 상태의 주문에 대해 체크아웃을 성공적으로 처리한다`() = runTest {
        // given
        val orderId = 1L
        val request = OrderCheckoutRequest(shippingAddress = "서울시 강남구", couponCode = "WELCOME10")

        val pendingOrder = Order(id = orderId, userId = 100L, productId = 200L, status = OrderStatus.PENDING)
        val completedOrder = pendingOrder.copy(status = OrderStatus.COMPLETED)

        // suspend 함수를 Mocking 할 때는 every 대신 coEvery를 사용
        coEvery { orderRepository.findById(orderId) } returns pendingOrder
        coEvery { orderRepository.save(any()) } returns completedOrder

        // when
        val response = orderCheckoutService.checkoutOrder(orderId, request)

        // then
        response.status shouldBe OrderStatus.COMPLETED

        // 💡 핵심 3: suspend 함수 호출 검증 시 verify 대신 coVerify를 사용합니다.
        coVerify(exactly = 1) { orderRepository.findById(orderId) }
        coVerify(exactly = 1) { orderRepository.save(any()) }
    }



}
