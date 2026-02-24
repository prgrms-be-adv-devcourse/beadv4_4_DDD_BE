package com.modeunsa.boundedcontext.inventory.app.common;

import com.modeunsa.boundedcontext.inventory.domain.Inventory;
import com.modeunsa.boundedcontext.inventory.out.InventoryRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.inventory.dto.InventoryDto;
import com.modeunsa.shared.inventory.dto.InventoryListRequest;
import com.modeunsa.shared.inventory.dto.InventoryListResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryGetInventoriesUseCase {
  private final InventoryRepository inventoryRepository;
  private final InventoryMapper inventoryMapper;

  public InventoryListResponse getInventories(Long sellerId, InventoryListRequest request) {
    List<Inventory> inventories =
        inventoryRepository.findByProductIdInAndSellerId(request.productIds(), sellerId);

    for (Inventory inventory : inventories) {
      if (!inventory.getSellerId().equals(sellerId)) {
        throw new GeneralException(ErrorStatus.INVENTORY_ACCESS_DENIED);
      }
    }

    List<InventoryDto> dtoList = inventoryMapper.toDtoList(inventories);

    return new InventoryListResponse(dtoList);
  }
}
