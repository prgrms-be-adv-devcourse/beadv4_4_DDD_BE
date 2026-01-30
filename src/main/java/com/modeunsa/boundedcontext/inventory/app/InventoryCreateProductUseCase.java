package com.modeunsa.boundedcontext.inventory.app;

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

  public void createProduct(ProductDto productDto) {
    InventoryProduct product = inventoryMapper.toInventoryProduct(productDto);
    inventoryProductRepository.save(product);
  }
}
