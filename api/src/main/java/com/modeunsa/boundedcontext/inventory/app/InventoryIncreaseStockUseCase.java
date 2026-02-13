package com.modeunsa.boundedcontext.inventory.app;

import com.modeunsa.boundedcontext.inventory.out.InventoryRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.order.dto.OrderItemDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryIncreaseStockUseCase {

  private final InventoryRepository inventoryRepository;

  public void increaseStock(List<OrderItemDto> orderItems) {
    for (OrderItemDto item : orderItems) {
      // DB 실재고 복구
      int updatedRows =
          inventoryRepository.increaseStockQuantity(item.getProductId(), item.getQuantity());

      if (updatedRows == 0) {
        // 상품이 존재하지 않는 경우 예외 처리
        throw new GeneralException(ErrorStatus.INVENTORY_NOT_FOUND, item);
      }
    }
  }
}
