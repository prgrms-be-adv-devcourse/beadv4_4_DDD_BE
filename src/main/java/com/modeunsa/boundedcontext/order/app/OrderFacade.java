package com.modeunsa.boundedcontext.order.app;

import com.modeunsa.boundedcontext.order.domain.OrderMember;
import com.modeunsa.boundedcontext.order.domain.OrderProduct;
import com.modeunsa.shared.order.dto.CreateCartItemRequestDto;
import com.modeunsa.shared.order.dto.CreateCartItemResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderFacade {
  private final OrderCreateCartItemUsecase orderCreateCartItemUsecase;
  private final OrderSupport orderSupport;

  @Transactional
  public CreateCartItemResponseDto createCartItem(
      long memberId, CreateCartItemRequestDto requestDto) {
    return orderCreateCartItemUsecase.createCartItem(memberId, requestDto);
  }

  public long countProduct() {
    return orderSupport.countProduct();
  }

  public long countMember() {
    return orderSupport.countMember();
  }

  public OrderMember findByMemberId(long memberId) {
    return orderSupport.findByMemberId(memberId);
  }

  public OrderProduct findByProductId(long productId) {
    return orderSupport.findByProductId(productId);
  }
}
