package com.modeunsa.boundedcontext.inventory.app;

import com.modeunsa.boundedcontext.inventory.domain.InventorySeller;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class InventoryMapper {

  public abstract InventorySeller toInventorySeller(
      Long sellerId, String businessName, String representativeName);
}
