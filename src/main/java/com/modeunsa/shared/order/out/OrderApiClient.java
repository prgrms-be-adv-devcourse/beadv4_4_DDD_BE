package com.modeunsa.shared.order.out;

import com.modeunsa.shared.order.dto.OrderDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class OrderApiClient {
  private final RestClient restClient;

  public OrderApiClient(@Value("${custom.global.internalBackUrl}") String internalBackUrl) {
    this.restClient = RestClient.builder().baseUrl(internalBackUrl + "/api/v1/orders").build();
  }

  public OrderDto getOrder(Long id) {
    return restClient
        .get()
        .uri("/%d".formatted(id))
        .retrieve()
        .body(new ParameterizedTypeReference<>() {});
  }
}
