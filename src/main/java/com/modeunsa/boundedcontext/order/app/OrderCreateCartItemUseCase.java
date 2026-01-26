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
public class OrderCreateCartItemUseCase {
  private final OrderCartItemRepository orderCartItemRepository;
  private final OrderMapper orderMapper;
  private final OrderSupport orderSupport;

  public CreateCartItemResponseDto createCartItem(
      Long memberId, CreateCartItemRequestDto createCartItemRequestDto) {
    // 회원 확인
    OrderMember member = orderSupport.findByMemberId(memberId);

    // 상품 확인
    OrderProduct product = orderSupport.findByProductId(createCartItemRequestDto.productId());

    CartItem cartItem =
        orderCartItemRepository
            .findByMemberIdAndProductId(member.getId(), product.getId())
            .map(
                item -> {
                  item.updateQuantity(createCartItemRequestDto.quantity());
                  return item;
                })
            .orElseGet(
                () -> {
                  CartItem newItem =
                      orderMapper.toCartItemEntity(memberId, createCartItemRequestDto);
                  return orderCartItemRepository.save(newItem);
                });

    return orderMapper.toCreateCartItemResponseDto(cartItem);
  }
}
