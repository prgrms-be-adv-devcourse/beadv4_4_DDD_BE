package com.modeunsa.boundedcontext.inventory.app.common;

import com.modeunsa.boundedcontext.inventory.domain.Inventory;
import com.modeunsa.boundedcontext.inventory.domain.InventoryProduct;
import com.modeunsa.boundedcontext.inventory.domain.InventorySeller;
import com.modeunsa.shared.inventory.dto.InventoryDto;
import com.modeunsa.shared.inventory.dto.InventoryUpdateResponse;
import com.modeunsa.shared.product.dto.ProductDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

  @Mapping(target = "id", source = "sellerId")
  InventorySeller toInventorySeller(Long sellerId, String businessName, String representativeName);

  @Mapping(target = "id", source = "id")
  InventoryProduct toInventoryProduct(ProductDto productDto);

  @Mapping(target = "productId", source = "id")
  Inventory toInventoryFromProduct(InventoryProduct product);

  InventoryUpdateResponse toInventoryUpdateResponse(Inventory inventory);

  InventoryDto toInventoryDto(Inventory inventory);

  List<InventoryDto> toDtoList(List<Inventory> inventories);
}
