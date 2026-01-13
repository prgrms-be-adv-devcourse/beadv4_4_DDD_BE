package com.modeunsa.boundedcontext.order.app;

import com.modeunsa.boundedcontext.order.domain.CartItem;
import com.modeunsa.boundedcontext.order.domain.OrderMapper;
import com.modeunsa.boundedcontext.order.out.CartItemRepository;
import com.modeunsa.shared.order.dto.CreateCartItemRequestDto;
import com.modeunsa.shared.order.dto.CreateCartItemResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderCreateCartItemUsecase {
  private final CartItemRepository cartItemRepository;
  private final OrderMapper orderMapper;

  public CreateCartItemResponseDto createCartItem(
      long memberId, CreateCartItemRequestDto createCartItemRequestDto) {

    CartItem cartItem = orderMapper.toCartItemEntity(memberId, createCartItemRequestDto);

    cartItemRepository.save(cartItem);

    return orderMapper.toCreateCartItemResponseDto(cartItem);
  }
}
