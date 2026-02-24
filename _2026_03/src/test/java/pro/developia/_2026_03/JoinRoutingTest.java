package pro.developia._2026_03;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;
import pro.developia._2026_03.domain.order.Order;
import pro.developia._2026_03.domain.order.OrderItem;
import pro.developia._2026_03.domain.order.OrderItemRepository;
import pro.developia._2026_03.domain.order.OrderRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("shardingsphere")
@ExtendWith(OutputCaptureExtension.class)
public class JoinRoutingTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @AfterEach
    void tearDown() {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
    }

    @Test
    @DisplayName("Binding Table을 활용한 크로스 샤드 JOIN 페치 테스트")
    void testCrossShardJoinWithBindingTable(CapturedOutput output) {
        // Data Setup: order_id = 100 ( 100 % 3 = ds_1로 저장될 예정 )
        Long orderId = 100L;

        Order order = Order.builder()
                .orderId(orderId)
                .customerName("CEO 개발자님")
                .build();

        // 동일한 order_id를 갖는 자식 데이터 생성
        order.addOrderItem(OrderItem.builder().itemId(1001L).productName("MacBook Pro").build());
        order.addOrderItem(OrderItem.builder().itemId(1002L).productName("AirPods Max").build());

        orderRepository.save(order); // CASCADE 로 인해 order_items 도 함께 INSERT 됨
        orderRepository.flush(); // DB에 쿼리 밀어내기

        int preLength1 = output.getOut().length();

        System.out.println("\n=======================================================");
        System.out.println(" [JOIN 실행] order_id 를 기준으로 페치 조인 실행");
        System.out.println("=======================================================");

        Optional<Order> fetchedOrder = orderRepository.findOrderWithItems(orderId);

        // 방금 실행한 액션 이후의 로그만 추출
        String logs1 = output.getOut().substring(preLength1);
        // "Actual SQL: ds_" 문자열이 몇 번 등장했는지 카운트
        int queryCount1 = logs1.split("Actual SQL: ds_").length - 1;

        assertThat(queryCount1).isEqualTo(1);

        fetchedOrder.ifPresent(o -> {
            System.out.println("조회된 고객: " + o.getCustomerName());
            System.out.println("조회된 아이템 개수: " + o.getOrderItems().size() + "개");
        });
    }

}
