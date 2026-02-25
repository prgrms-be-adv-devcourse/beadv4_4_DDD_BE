package com.modeunsa.boundedcontext.order.app;

import com.modeunsa.boundedcontext.inventory.out.InventoryApiClient;
import com.modeunsa.boundedcontext.order.domain.CartItem;
import com.modeunsa.boundedcontext.order.domain.OrderMapper;
import com.modeunsa.boundedcontext.order.domain.OrderMember;
import com.modeunsa.boundedcontext.order.domain.OrderProduct;
import com.modeunsa.boundedcontext.order.out.OrderCartItemRepository;
import com.modeunsa.shared.inventory.dto.InventoryDto;
import com.modeunsa.shared.order.dto.SyncCartItemRequestDto;
import com.modeunsa.shared.order.dto.SyncCartItemResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderSyncCartItemUseCase {
  private final OrderCartItemRepository orderCartItemRepository;
  private final OrderMapper orderMapper;
  private final OrderSupport orderSupport;
  private final InventoryApiClient inventoryApiClient;

  public SyncCartItemResponseDto syncCartItem(
      Long memberId, SyncCartItemRequestDto syncCartItemRequestDto) {
    // 회원 확인
    OrderMember member = orderSupport.findByMemberId(memberId);

    // 상품 확인
    OrderProduct product = orderSupport.findByProductId(syncCartItemRequestDto.productId());

    // 재고 확인
    InventoryDto inventory = inventoryApiClient.getInventory(syncCartItemRequestDto.productId());
    // TODO: 예약주문가능 재고 조회

    CartItem cartItem =
        orderCartItemRepository
            .findByMemberIdAndProductId(member.getId(), product.getId())
            .map(
                item -> {
                  item.updateQuantity(syncCartItemRequestDto.quantity());
                  return item;
                })
            .orElseGet(
                () -> {
                  CartItem newItem = orderMapper.toCartItemEntity(memberId, syncCartItemRequestDto);
                  return orderCartItemRepository.save(newItem);
                });

    return orderMapper.toSyncCartItemResponseDto(cartItem);
  }
}
