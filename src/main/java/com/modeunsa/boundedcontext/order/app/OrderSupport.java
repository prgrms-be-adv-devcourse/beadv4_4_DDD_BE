package com.modeunsa.boundedcontext.order.app;

import com.modeunsa.boundedcontext.order.domain.OrderMember;
import com.modeunsa.boundedcontext.order.domain.OrderProduct;
import com.modeunsa.boundedcontext.order.out.OrderMemberRepository;
import com.modeunsa.boundedcontext.order.out.OrderProductRepository;
import com.modeunsa.boundedcontext.order.out.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class OrderSupport {
  private final OrderMemberRepository orderMemberRepository;
  private final OrderProductRepository orderProductRepository;
  private final OrderRepository orderRepository;

  public long countProduct() {
    return orderProductRepository.count();
  }

  public long countMember() {
    return orderMemberRepository.count();
  }

  public OrderMember findByMemberId(long memberId) {
    return orderMemberRepository.findById(memberId).orElseThrow();
  }

  public OrderProduct findByProductId(long productId) {
    return orderProductRepository.findById(productId).orElseThrow();
  }

  public long countOrder() {
    return orderRepository.count();
  }
}
