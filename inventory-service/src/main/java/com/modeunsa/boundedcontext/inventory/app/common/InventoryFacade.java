package com.modeunsa.boundedcontext.inventory.app.common;

import com.modeunsa.boundedcontext.inventory.app.command.InventoryCreateInventoryUseCase;
import com.modeunsa.boundedcontext.inventory.app.command.InventoryCreateProductUseCase;
import com.modeunsa.boundedcontext.inventory.app.command.InventoryDecreaseStockUseCase;
import com.modeunsa.boundedcontext.inventory.app.command.InventoryIncreaseStockUseCase;
import com.modeunsa.boundedcontext.inventory.app.command.InventoryInitializeUseCase;
import com.modeunsa.boundedcontext.inventory.app.command.InventoryRegisterSellerUseCase;
import com.modeunsa.boundedcontext.inventory.app.command.InventoryReleaseInventoryUseCase;
import com.modeunsa.boundedcontext.inventory.app.command.InventoryReserveInventoryUseCase;
import com.modeunsa.boundedcontext.inventory.app.command.InventoryUpdateInventoryUseCase;
import com.modeunsa.boundedcontext.inventory.domain.InventoryProduct;
import com.modeunsa.shared.inventory.dto.InventoryDto;
import com.modeunsa.shared.inventory.dto.InventoryInitializeRequest;
import com.modeunsa.shared.inventory.dto.InventoryListRequest;
import com.modeunsa.shared.inventory.dto.InventoryListResponse;
import com.modeunsa.shared.inventory.dto.InventoryReserveRequest;
import com.modeunsa.shared.order.dto.OrderItemDto;
import com.modeunsa.shared.product.dto.ProductDto;
import java.util.List;
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
  private final InventoryUpdateInventoryUseCase inventoryUpdateInventoryUseCase;
  private final InventorySupport inventorySupport;
  private final InventoryMapper inventoryMapper;
  private final InventoryReserveInventoryUseCase inventoryReserveInventoryUseCase;
  private final InventoryReleaseInventoryUseCase inventoryReleaseInventoryUseCase;
  private final InventoryDecreaseStockUseCase inventoryDecreaseStockUseCase;
  private final InventoryIncreaseStockUseCase inventoryIncreaseStockUseCase;
  private final InventoryInitializeUseCase inventoryInitializeInventoryUseCase;
  private final InventoryGetInventoriesUseCase inventoryGetInventoriesUseCase;

  @Transactional
  public void registerSeller(Long sellerId, String businessName, String representativeName) {
    inventoryRegisterSellerUseCase.registerSeller(sellerId, businessName, representativeName);
  }

  @Transactional
  public void createProduct(ProductDto productDto) {
    InventoryProduct product = inventoryCreateProductUseCase.createProduct(productDto);
    inventoryCreateInventoryUseCase.createInventory(product);
  }

  /* TODO: 업데이트 로직 분리
  @Transactional
  public InventoryUpdateResponse updateInventory(
      Long sellerId, Long productId, InventoryUpdateRequest inventoryUpdateRequest) {
    return inventoryUpdateInventoryUseCase.updateInventory(
        sellerId, productId, inventoryUpdateRequest);
  }
   */

  public InventoryDto getInventory(Long productId) {
    return inventoryMapper.toInventoryDto(inventorySupport.getInventory(productId));
  }

  @Transactional
  public void reserveInventory(InventoryReserveRequest request) {
    inventoryReserveInventoryUseCase.reserveInventory(request);
  }

  @Transactional
  public void releaseInventory(List<OrderItemDto> orderItems) {
    inventoryReleaseInventoryUseCase.releaseInventory(orderItems);
  }

  @Transactional
  public void decreaseStock(List<OrderItemDto> orderItems) {
    inventoryDecreaseStockUseCase.decreaseStock(orderItems);
  }

  @Transactional
  public void increaseStock(List<OrderItemDto> orderItems) {
    inventoryIncreaseStockUseCase.increaseStock(orderItems);
  }

  @Transactional
  public void initializeInventory(
      Long sellerId, Long productId, InventoryInitializeRequest request) {
    inventoryInitializeInventoryUseCase.initializeInventory(sellerId, productId, request);
  }

  public InventoryListResponse getInventories(Long sellerId, InventoryListRequest request) {
    return inventoryGetInventoriesUseCase.getInventories(sellerId, request);
  }
}
