package com.modeunsa.boundedcontext.product.in;

import com.modeunsa.boundedcontext.product.app.ProductFacade;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.status.SuccessStatus;
import com.modeunsa.shared.product.dto.ProductRequest;
import com.modeunsa.shared.product.dto.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Product", description = "상품 도메인 API")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ApiV1ProductController {

  private final ProductFacade productFacade;

  @Operation(summary = "상품 생성", description = "상품을 생성합니다.")
  @PostMapping
  public ResponseEntity<ApiResponse> createProduct(
      @Valid @RequestBody ProductRequest productRequest) {
    ProductResponse productResponse = productFacade.createProduct(productRequest);
    return ApiResponse.onSuccess(SuccessStatus.CREATED, productResponse);
  }

  @Operation(summary = "상품 상세 조회", description = "상품 id를 이용해 상품 상세를 조회합니다.")
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse> getProduct(@PathVariable(name = "id") Long productId) {
    ProductResponse productResponse = productFacade.getProduct(productId);
    return ApiResponse.onSuccess(SuccessStatus.OK, productResponse);
  }
}
