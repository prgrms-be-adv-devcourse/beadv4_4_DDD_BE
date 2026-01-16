package com.modeunsa.boundedcontext.order.out;

import com.modeunsa.boundedcontext.order.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {}
