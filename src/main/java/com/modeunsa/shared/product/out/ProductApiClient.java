package com.modeunsa.shared.product.out;

import com.modeunsa.shared.product.dto.ProductOrderResponse;
import com.modeunsa.shared.product.dto.ProductOrderValidateRequest;
import com.modeunsa.shared.product.dto.ProductStockResponse;
import com.modeunsa.shared.product.dto.ProductStockUpdateRequest;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ProductApiClient {

  private final RestClient restClient;

  public ProductApiClient(
      @Value("${custom.global.internalBackUrl}") String internalBackUrl,
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

  public List<ProductStockResponse> updateStock(ProductStockUpdateRequest request) {
    return restClient
        .patch()
        .uri("/stock")
        .body(request)
        .retrieve()
        .body(new ParameterizedTypeReference<>() {});
  }
}
