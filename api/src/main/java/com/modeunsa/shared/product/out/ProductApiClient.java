package com.modeunsa.shared.product.out;

import com.modeunsa.shared.product.dto.ProductOrderResponse;
import com.modeunsa.shared.product.dto.ProductOrderValidateRequest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class ProductApiClient {

  private final RestClient restClient;

  public ProductApiClient(
      @Value("${custom.global.productInternalBackUrl}") String internalBackUrl,
      @Value("${internal.api-key}") String internalApiKey) {
    this.restClient =
        RestClient.builder()
            .baseUrl(internalBackUrl + "/api/v1/products")
            .defaultHeader("X-INTERNAL-API-KEY", internalApiKey)
            .build();
  }

  public List<ProductOrderResponse> validateOrderProducts(ProductOrderValidateRequest request) {
    return restClient
        .post()
        .uri("/internal/validate-order")
        .body(request)
        .retrieve()
        .body(new ParameterizedTypeReference<>() {});
  }
}
