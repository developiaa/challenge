package pro.developia._2026_03.domain.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
            SELECT o
            FROM Order o
            JOIN FETCH o.orderItems
            WHERE o.orderId = :orderId""")
    Optional<Order> findOrderWithItems(@Param("orderId") Long orderId);
}
