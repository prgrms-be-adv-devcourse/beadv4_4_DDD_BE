package com.modeunsa.boundedcontext.order.out;

import com.modeunsa.boundedcontext.order.domain.Order;
import com.modeunsa.boundedcontext.order.domain.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {
  Page<Order> findAllByOrderMemberId(long memberId, Pageable pageable);

  @Query(
      "SELECT DISTINCT o FROM Order o "
          + "JOIN FETCH o.orderMember "
          + "JOIN FETCH o.orderItems "
          + "WHERE o.id = :id")
  Optional<Order> findByIdWithFetch(@Param("id") Long id);

  Optional<Order> findTopByOrderMemberIdOrderByIdDesc(Long memberId);

  List<Order> findAllByStatusAndPaidAtBefore(OrderStatus status, LocalDateTime cutoff);

  Optional<Order> findByIdAndOrderMemberId(Long orderId, Long memberId);
}
