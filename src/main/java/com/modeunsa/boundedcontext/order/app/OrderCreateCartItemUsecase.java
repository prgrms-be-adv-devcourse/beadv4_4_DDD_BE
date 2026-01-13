package com.modeunsa.boundedcontext.order.app;

import com.modeunsa.boundedcontext.order.domain.CartItem;
import com.modeunsa.boundedcontext.order.domain.OrderMapper;
import com.modeunsa.boundedcontext.order.domain.OrderMember;
import com.modeunsa.boundedcontext.order.domain.OrderProduct;
import com.modeunsa.boundedcontext.order.out.CartItemRepository;
import com.modeunsa.boundedcontext.order.out.OrderMemberRepository;
import com.modeunsa.boundedcontext.order.out.OrderProductRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.order.dto.CreateCartItemRequestDto;
import com.modeunsa.shared.order.dto.CreateCartItemResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderCreateCartItemUsecase {
  private final CartItemRepository cartItemRepository;
  private final OrderMapper orderMapper;
  private final OrderProductRepository orderProductRepository;
  private final OrderMemberRepository orderMemberRepository;

  public CreateCartItemResponseDto createCartItem(
      long memberId, CreateCartItemRequestDto createCartItemRequestDto) {
    // 회원 확인
    OrderMember member =
        orderMemberRepository
            .findById(memberId)
            .orElseThrow(() -> new GeneralException(ErrorStatus.ORDERMEMBER_NOT_FOUND));

    // 상품 확인
    OrderProduct product =
        orderProductRepository
            .findById(createCartItemRequestDto.getProductId())
            .orElseThrow(() -> new GeneralException(ErrorStatus.ORDERPRODUCT_NOT_FOUND));

    CartItem cartItem = orderMapper.toCartItemEntity(memberId, createCartItemRequestDto);

    cartItemRepository.save(cartItem);

    return orderMapper.toCreateCartItemResponseDto(cartItem);
  }
}
