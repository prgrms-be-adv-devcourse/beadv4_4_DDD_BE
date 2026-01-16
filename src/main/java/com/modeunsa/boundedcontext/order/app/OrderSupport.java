package com.modeunsa.boundedcontext.order.app;

import com.modeunsa.boundedcontext.order.domain.OrderMember;
import com.modeunsa.boundedcontext.order.domain.OrderProduct;
import com.modeunsa.boundedcontext.order.out.OrderMemberRepository;
import com.modeunsa.boundedcontext.order.out.OrderProductRepository;
import com.modeunsa.boundedcontext.order.out.OrderRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class OrderSupport {
  private final OrderMemberRepository orderMemberRepository;
  private final OrderProductRepository orderProductRepository;
  private final OrderRepository orderRepository;

  // orderMember
  public long countMember() {
    return orderMemberRepository.count();
  }

  public OrderMember findByMemberId(long memberId) {
    return orderMemberRepository
        .findById(memberId)
        .orElseThrow(() -> new GeneralException(ErrorStatus.ORDER_MEMBER_NOT_FOUND));
  }

  // orderProduct
  public long countProduct() {
    return orderProductRepository.count();
  }

  public OrderProduct findByProductId(long productId) {
    return orderProductRepository
        .findById(productId)
        .orElseThrow(() -> new GeneralException(ErrorStatus.ORDER_PRODUCT_NOT_FOUND));
  }

  // order
  public long countOrder() {
    return orderRepository.count();
  }
}
