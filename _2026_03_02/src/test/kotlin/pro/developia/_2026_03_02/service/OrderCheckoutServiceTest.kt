package pro.developia._2026_03_02.service


import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.repository.findByIdOrNull
import pro.developia._2026_03_02.controller.dto.OrderCheckoutRequest
import pro.developia._2026_03_02.domain.Order
import pro.developia._2026_03_02.domain.OrderStatus
import pro.developia._2026_03_02.repository.OrderRepository

@ExtendWith(MockKExtension::class)
class OrderCheckoutServiceTest {
    @MockK
    private lateinit var orderRepository: OrderRepository

    @InjectMockKs
    private lateinit var orderCheckoutService: OrderCheckoutService

    @Test
    fun `결제 대기 상태의 주문에 대해 체크아웃을 성공적으로 처리한다`() {
        // given
        val orderId = 1L
        val request = OrderCheckoutRequest(shippingAddress = "서울시 강남구", couponCode = "WELCOME10")
        val pendingOrder = Order(id = orderId, userId = 100L, productId = 200L, status = OrderStatus.PENDING)

        // Mocking: Mockito의 when().thenReturn()을 대체하는 MockK의 every { } returns
        every { orderRepository.findByIdOrNull(orderId) } returns pendingOrder

        // when
        val response = orderCheckoutService.checkoutOrder(orderId, request)

        // then
        // 💡 특징 2: AssertJ의 assertThat(A).isEqualTo(B) 대신 중위 함수(Infix function) shouldBe를 사용하여 영어 문장처럼 읽히게 합니다.
        response.status shouldBe OrderStatus.COMPLETED
        response.productId shouldBe 200L

        // 검증: Mockito의 verify()와 유사하지만 DSL 형태를 띱니다.
        verify(exactly = 1) { orderRepository.findByIdOrNull(orderId) }
    }

    @Test
    fun `주문이 존재하지 않으면 IllegalArgumentException 예외가 발생한다`() {
        // given
        val orderId = 999L
        val request = OrderCheckoutRequest(shippingAddress = "서울시 강남구")

        // 데이터가 없는 상황 모킹
        every { orderRepository.findByIdOrNull(orderId) } returns null

        // when & then
        // 💡 특징 3: 예외 검증 시 assertThrows 대신 shouldThrow 블록을 사용합니다.
        val exception = shouldThrow<IllegalArgumentException> {
            orderCheckoutService.checkoutOrder(orderId, request)
        }

        exception.message shouldBe "Order not found"
        verify(exactly = 1) { orderRepository.findByIdOrNull(orderId) }
    }
}
