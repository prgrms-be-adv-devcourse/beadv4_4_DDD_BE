package com.modeunsa.boundedcontext.product.in;

import com.modeunsa.boundedcontext.product.app.ProductFacade;
import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.status.SuccessStatus;
import com.modeunsa.shared.product.dto.ProductDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/product/products")
@RequiredArgsConstructor
public class ApiV1ProductController {

  private final ProductFacade productFacade;

  @PostMapping
  @Transactional
  public ResponseEntity<ApiResponse> createProduct(@RequestBody ProductDto productDto) {
    Product product = productFacade.createProduct(productDto);
    return ApiResponse.onSuccess(SuccessStatus._OK, product);
  }
}
