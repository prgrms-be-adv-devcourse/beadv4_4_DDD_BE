package com.modeunsa.boundedcontext.order.out;

import com.modeunsa.boundedcontext.order.domain.Order;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
  @EntityGraph(attributePaths = {"orderItems"})
  List<Order> findAllByOrderMemberIdOrderByCreatedAtDesc(long memberId);
}
