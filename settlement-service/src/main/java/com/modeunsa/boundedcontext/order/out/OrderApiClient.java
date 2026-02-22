package com.modeunsa.boundedcontext.order.out;

import com.modeunsa.shared.order.dto.OrderDto;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class OrderApiClient {
  private final RestClient restClient;
  private static final String V1_BASE_PATH = "/api/v1/orders/internal";
  private static final String V2_BASE_PATH = "/api/v2/orders/internal";

  public OrderApiClient(
      @Value("${custom.global.internalBackUrl}") String internalBackUrl,
      @Value("${internal.api-key}") String internalApiKey) {
    this.restClient =
        RestClient.builder()
            .baseUrl(internalBackUrl)
            .defaultHeader("X-INTERNAL-API-KEY", internalApiKey)
            .build();
  }

  public OrderDto getOrder(Long id) {
    return restClient.get().uri(V1_BASE_PATH + "/{id}", id).retrieve().body(OrderDto.class);
  }

  public List<Long> getCartItemIds(Long memberId) {
    return restClient
        .get()
        .uri(V2_BASE_PATH + "/cart-items/{memberId}", memberId)
        .retrieve()
        .body(new ParameterizedTypeReference<>() {});
  }
}
