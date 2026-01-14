package com.modeunsa.boundedcontext.product.in;

import com.modeunsa.boundedcontext.product.app.ProductFacade;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.status.SuccessStatus;
import com.modeunsa.shared.product.dto.ProductRequest;
import com.modeunsa.shared.product.dto.ProductResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ApiV1ProductController {

  private final ProductFacade productFacade;

  @PostMapping
  @Transactional
  public ResponseEntity<ApiResponse> createProduct(
      @Valid @RequestBody ProductRequest productRequest) {
    ProductResponse productResponse = productFacade.createProduct(productRequest);
    return ApiResponse.onSuccess(SuccessStatus._CREATED, productResponse);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse> getProduct(@PathVariable(name = "id") Long productId) {
    ProductResponse productResponse = productFacade.getProduct(productId);
    return ApiResponse.onSuccess(SuccessStatus._OK, productResponse);
  }
}
