package com.modeunsa.boundedcontext.order.out;

import com.modeunsa.boundedcontext.order.domain.OrderMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderMemberRepository extends JpaRepository<OrderMember, Long> {}
