package com.modeunsa.boundedcontext.order.app;

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

  @Transactional
  public CreateCartItemResponseDto createCartItem(
      long memberId, CreateCartItemRequestDto requestDto) {
    return orderCreateCartItemUsecase.createCartItem(memberId, requestDto);
  }
}
