package com.modeunsa.boundedcontext.order.out;

import com.modeunsa.boundedcontext.order.domain.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {}
