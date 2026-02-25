package com.modeunsa.boundedcontext.recommendation.out;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class OrderApiClient {
  private final RestClient restClient;
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

  public List<Long> getRecent10CartItems(Long memberId) {
    return restClient
        .get()
        .uri(V2_BASE_PATH + "/cart-items/{memberId}", memberId)
        .retrieve()
        .body(new ParameterizedTypeReference<>() {});
  }
}
