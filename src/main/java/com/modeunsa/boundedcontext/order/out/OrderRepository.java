package com.modeunsa.boundedcontext.order.out;

import com.modeunsa.boundedcontext.order.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
  Page<Order> findAllByOrderMemberId(long memberId, Pageable pageable);
}
