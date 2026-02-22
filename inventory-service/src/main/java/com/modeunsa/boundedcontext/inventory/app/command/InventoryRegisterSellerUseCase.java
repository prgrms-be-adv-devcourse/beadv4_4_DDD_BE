package com.modeunsa.boundedcontext.inventory.app.command;

import com.modeunsa.boundedcontext.inventory.app.common.InventoryMapper;
import com.modeunsa.boundedcontext.inventory.domain.InventorySeller;
import com.modeunsa.boundedcontext.inventory.out.InventorySellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryRegisterSellerUseCase {
  private final InventoryMapper inventoryMapper;
  private final InventorySellerRepository inventorySellerRepository;

  public void registerSeller(Long sellerId, String businessName, String representativeName) {
    InventorySeller seller =
        inventoryMapper.toInventorySeller(sellerId, businessName, representativeName);
    inventorySellerRepository.save(seller);
  }
}
