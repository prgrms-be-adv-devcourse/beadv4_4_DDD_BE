package com.modeunsa.boundedcontext.inventory.app;

import com.modeunsa.boundedcontext.inventory.domain.InventoryProduct;
import com.modeunsa.shared.inventory.dto.InventoryAvailableQuantityResponse;
import com.modeunsa.shared.inventory.dto.InventoryDto;
import com.modeunsa.shared.inventory.dto.InventoryReserveRequest;
import com.modeunsa.shared.inventory.dto.InventoryUpdateRequest;
import com.modeunsa.shared.inventory.dto.InventoryUpdateResponse;
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
  private final InventoryCreateInventoryUseCase inventoryCreateInventoryUseCase;
  private final InventoryUpdateInventoryUseCase invertoryUpdateInventoryUseCase;
  private final InventorySupport inventorySupport;
  private final InventoryMapper inventoryMapper;
  private final InventoryReserveInventoryUseCase inventoryReserveInventoryUseCase;
  private final InventoryGetAvailableQuantityUseCase inventoryGetAvailableQuantityUseCase;

  @Transactional
  public void registerSeller(Long sellerId, String businessName, String representativeName) {
    inventoryRegisterSellerUseCase.registerSeller(sellerId, businessName, representativeName);
  }

  @Transactional
  public void createProduct(ProductDto productDto) {
    InventoryProduct product = inventoryCreateProductUseCase.createProduct(productDto);
    inventoryCreateInventoryUseCase.createInventory(product);
  }

  @Transactional
  public InventoryUpdateResponse updateInventory(
      Long sellerId, Long productId, InventoryUpdateRequest inventoryUpdateRequest) {
    return invertoryUpdateInventoryUseCase.updateInventory(
        sellerId, productId, inventoryUpdateRequest);
  }

  public InventoryDto getInventory(Long productId) {
    return inventoryMapper.toInventoryDto(inventorySupport.getInventory(productId));
  }

  @Transactional
  public void reserveInventory(InventoryReserveRequest request) {
    inventoryReserveInventoryUseCase.reserveInventory(request);
  }

  public InventoryAvailableQuantityResponse getAvailableQuantity(Long productId) {
    return inventoryGetAvailableQuantityUseCase.getAvailableQuantity(productId);
  }
}
