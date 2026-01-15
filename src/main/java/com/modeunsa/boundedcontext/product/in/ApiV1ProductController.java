package com.modeunsa.boundedcontext.product.in;

import com.modeunsa.boundedcontext.product.app.ProductFacade;
import com.modeunsa.boundedcontext.product.domain.ProductCategory;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.status.SuccessStatus;
import com.modeunsa.shared.product.dto.ProductRequest;
import com.modeunsa.shared.product.dto.ProductResponse;
import com.modeunsa.shared.product.dto.ProductUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    // TODO: sellerId 는 나중에 security 에서 가져올 것
    Long sellerId = 1L;
    ProductResponse productResponse = productFacade.createProduct(sellerId, productRequest);
    return ApiResponse.onSuccess(SuccessStatus.CREATED, productResponse);
  }

  @Operation(summary = "상품 상세 조회", description = "상품 id를 이용해 상품 상세를 조회합니다.")
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse> getProduct(@PathVariable(name = "id") Long productId) {
    ProductResponse productResponse = productFacade.getProduct(productId);
    return ApiResponse.onSuccess(SuccessStatus.OK, productResponse);
  }

  @Operation(summary = "상품 리스트 조회", description = "상품 리스트를 조회합니다.")
  @GetMapping
  public ResponseEntity<ApiResponse> getProducts(
      @RequestParam(name = "category") ProductCategory category,
      @PageableDefault(size = 20, sort = "createdAt", direction = Direction.DESC)
          Pageable pageable) {
    // TODO: memberId / role 받아와서 처리 예정
    Long memberId = 1L;
    Page<ProductResponse> productResponses =
        productFacade.getProducts(memberId, category, pageable);
    return ApiResponse.onSuccess(SuccessStatus.OK, productResponses);
  }

  @Operation(summary = "상품 수정", description = "상품을 수정합니다.")
  @PatchMapping("/{id}")
  public ResponseEntity<ApiResponse> updateProduct(
      @PathVariable(name = "id") Long productId,
      @Valid @RequestBody ProductUpdateRequest productRequest) {
    // TODO: sellerId 는 나중에 security 에서 가져올것
    Long sellerId = 1L;
    ProductResponse productResponse =
        productFacade.updateProduct(sellerId, productId, productRequest);
    return ApiResponse.onSuccess(SuccessStatus.OK, productResponse);
  }
}
