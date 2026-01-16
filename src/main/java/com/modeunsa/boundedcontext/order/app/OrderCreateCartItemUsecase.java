package com.modeunsa.boundedcontext.order.app;

import com.modeunsa.boundedcontext.order.domain.CartItem;
import com.modeunsa.boundedcontext.order.domain.OrderMapper;
import com.modeunsa.boundedcontext.order.domain.OrderMember;
import com.modeunsa.boundedcontext.order.domain.OrderProduct;
import com.modeunsa.boundedcontext.order.out.OrderCartItemRepository;
import com.modeunsa.shared.order.dto.CreateCartItemRequestDto;
import com.modeunsa.shared.order.dto.CreateCartItemResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderCreateCartItemUsecase {
  private final OrderCartItemRepository orderCartItemRepository;
  private final OrderMapper orderMapper;
  private final OrderSupport orderSupport;

  public CreateCartItemResponseDto createCartItem(
      long memberId, CreateCartItemRequestDto createCartItemRequestDto) {
    // 회원 확인
    OrderMember member = orderSupport.findByMemberId(memberId);

    // 상품 확인
    OrderProduct product = orderSupport.findByProductId(createCartItemRequestDto.getProductId());

    CartItem cartItem = orderMapper.toCartItemEntity(memberId, createCartItemRequestDto);

    orderCartItemRepository.save(cartItem);

    return orderMapper.toCreateCartItemResponseDto(cartItem);
  }
}
