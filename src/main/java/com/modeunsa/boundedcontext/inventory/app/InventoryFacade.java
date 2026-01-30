package com.modeunsa.boundedcontext.inventory.app;

import com.modeunsa.shared.product.dto.ProductDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryFacade {
  private final InventoryRegisterSellerUseCase inventoryRegisterSellerUseCase;
  private final InventoryCreateProductUseCase inventoryCreateProductUseCase;

  @Transactional
  public void registerSeller(Long sellerId, String businessName, String representativeName) {
    inventoryRegisterSellerUseCase.registerSeller(sellerId, businessName, representativeName);
  }

  @Transactional
  public void createProduct(ProductDto productDto) {
    inventoryCreateProductUseCase.createProduct(productDto);
  }
}
