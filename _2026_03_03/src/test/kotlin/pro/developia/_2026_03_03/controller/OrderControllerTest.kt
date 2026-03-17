package pro.developia._2026_03_03.controller

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import pro.developia._2026_03_03.domain.Order
import pro.developia._2026_03_03.domain.OrderStatus
import pro.developia._2026_03_03.dto.OrderCheckoutRequest
import pro.developia._2026_03_03.repository.OrderRepository
import kotlin.test.Test

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class OrderControllerTest {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var orderRepository: OrderRepository


    @BeforeEach
    fun setUp() = runTest {
        // 매 테스트 전 DB 초기화 (suspend 함수이므로 runTest 내부에서 호출)
        orderRepository.deleteAll()
    }

    @Test
    fun `API를 통해 주문 체크아웃을 수행하면 DB 상태가 COMPLETED로 변경된다`() = runTest {
        // given: 실제 DB에 테스트 데이터 세팅
        val savedOrder = orderRepository.save(
            Order(userId = 1L, productId = 99L, status = OrderStatus.PENDING)
        )
        val request = OrderCheckoutRequest(shippingAddress = "판교역", couponCode = null)

        // when: WebTestClient를 이용한 논블로킹 API 호출
        webTestClient.patch()
            .uri("/api/v1/orders/${savedOrder.id}/checkout")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange() // API 호출
            // then: HTTP 응답 검증
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.status").isEqualTo(OrderStatus.COMPLETED.name)
            .jsonPath("$.productId").isEqualTo(99L)

        // then: 실제 DB 데이터 변경 검증
        val updatedOrder = orderRepository.findById(savedOrder.id!!)
        updatedOrder?.status shouldBe OrderStatus.COMPLETED
    }

}
