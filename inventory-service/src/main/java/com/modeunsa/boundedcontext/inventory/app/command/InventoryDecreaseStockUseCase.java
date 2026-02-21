package com.modeunsa.boundedcontext.inventory.app.command;

import com.modeunsa.boundedcontext.inventory.out.InventoryRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.order.dto.OrderItemDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryDecreaseStockUseCase {
  private final InventoryRepository inventoryRepository;

  public void decreaseStock(List<OrderItemDto> orderItems) {
    for (OrderItemDto item : orderItems) {
      // 해당 상품의 재고 엔티티 조회 (벌크 업데이트)
      int updatedRows =
          inventoryRepository.decreaseStockQuantity(item.getProductId(), item.getQuantity());

      // 업데이트 된 행개수가 0이라면 재고가 부족하거나 상품이 없는 경우
      if (updatedRows == 0) {
        throw new GeneralException(ErrorStatus.INSUFFICIENT_STOCK, item);
      }
    }
  }
}
