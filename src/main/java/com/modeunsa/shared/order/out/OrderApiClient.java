package com.modeunsa.shared.order.out;

import com.modeunsa.shared.order.dto.OrderDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class OrderApiClient {
  private final RestClient restClient;

  public OrderApiClient(
      @Value("${custom.global.internalBackUrl}") String internalBackUrl,
      @Value("${internal.api-key}") String internalApiKey) {
    this.restClient =
        RestClient.builder()
            .baseUrl(internalBackUrl + "/api/v1/orders")
            .defaultHeader("X-INTERNAL-API-KEY", internalApiKey)
            .build();
  }

  public OrderDto getOrder(Long id) {
    return restClient.get().uri("/internal/{id}", id).retrieve().body(OrderDto.class);
  }
}
