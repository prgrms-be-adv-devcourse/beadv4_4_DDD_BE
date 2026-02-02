package com.modeunsa.shared.inventory.out;

import com.modeunsa.shared.inventory.dto.InventoryDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class InventoryApiClient {
  private final RestClient restClient;

  public InventoryApiClient(
      @Value("${custom.global.internalBackUrl}") String internalBackUrl,
      @Value("${internal.api-key}") String internalApiKey) {
    this.restClient =
        RestClient.builder()
            .baseUrl(internalBackUrl + "/api/v2/inventories")
            .defaultHeader("X-INTERNAL-API-KEY", internalApiKey)
            .build();
  }

  public InventoryDto getInventory(Long productId) {
    return restClient
        .get()
        .uri("/internal/{productId}", productId)
        .retrieve()
        .body(InventoryDto.class);
  }
}
