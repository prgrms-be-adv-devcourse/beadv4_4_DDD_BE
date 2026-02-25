package com.modeunsa.boundedcontext.inventory.app.command;

import com.modeunsa.boundedcontext.inventory.app.common.InventoryMapper;
import com.modeunsa.boundedcontext.inventory.domain.InventoryProduct;
import com.modeunsa.boundedcontext.inventory.out.InventoryProductRepository;
import com.modeunsa.shared.product.dto.ProductDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryCreateProductUseCase {
  private final InventoryMapper inventoryMapper;
  private final InventoryProductRepository inventoryProductRepository;

  public InventoryProduct createProduct(ProductDto productDto) {
    InventoryProduct product = inventoryMapper.toInventoryProduct(productDto);
    return inventoryProductRepository.save(product);
  }
}
