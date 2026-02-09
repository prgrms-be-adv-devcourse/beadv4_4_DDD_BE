package com.modeunsa.boundedcontext.inventory.in;

import com.modeunsa.boundedcontext.inventory.app.InventoryFacade;
import com.modeunsa.shared.inventory.dto.InventoryUpdateRequest;
import jakarta.transaction.Transactional;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

@Profile("!test")
@ConditionalOnProperty(name = "app.data-init.enabled", havingValue = "true", matchIfMissing = true)
// @Configuration
public class InventoryDataInit {
  private final com.modeunsa.boundedcontext.inventory.in.InventoryDataInit self;
  private final InventoryFacade inventoryFacade;

  public InventoryDataInit(
      @Lazy com.modeunsa.boundedcontext.inventory.in.InventoryDataInit self,
      InventoryFacade inventoryFacade) {

    this.self = self;
    this.inventoryFacade = inventoryFacade;
  }

  @Bean
  @Order(3)
  public ApplicationRunner inventoryDataInitRunner() {
    return args -> {
      self.makeBaseInventory();
    };
  }

  @Transactional
  public void makeBaseInventory() {

    inventoryFacade.updateInventory(1L, 2L, new InventoryUpdateRequest(100));

    inventoryFacade.updateInventory(1L, 3L, new InventoryUpdateRequest(100));

    inventoryFacade.updateInventory(1L, 4L, new InventoryUpdateRequest(100));

    inventoryFacade.updateInventory(1L, 5L, new InventoryUpdateRequest(100));

    inventoryFacade.updateInventory(1L, 6L, new InventoryUpdateRequest(100));

    inventoryFacade.updateInventory(1L, 7L, new InventoryUpdateRequest(100));

    inventoryFacade.updateInventory(1L, 8L, new InventoryUpdateRequest(100));

    inventoryFacade.updateInventory(1L, 1L, new InventoryUpdateRequest(100));
  }
}
