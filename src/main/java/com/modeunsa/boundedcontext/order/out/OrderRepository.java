package com.modeunsa.boundedcontext.order.out;

import com.modeunsa.boundedcontext.order.domain.Order;
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
}
