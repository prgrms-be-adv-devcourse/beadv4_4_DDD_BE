package com.modeunsa.boundedcontext.inventory.app;

import com.modeunsa.boundedcontext.inventory.out.InventoryRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.order.dto.OrderItemDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
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
        log.error(
            "[CRITICAL] 재고 복구 실패, 대상 상품이 존재하지 않거나 삭제되었습니다. productId: {}, quantity: {}",
            item.getProductId(),
            item.getQuantity());

        // TODO: 운영자가 확인할 수 있게 슬랙 알림이나 데드레터 큐로 보내기

        // 상품이 존재하지 않는 경우 예외 처리
        throw new GeneralException(ErrorStatus.INVENTORY_RESTORE_FAILED, item);
      }
    }
  }
}
