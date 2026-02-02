package com.modeunsa.boundedcontext.product.in;

import com.modeunsa.boundedcontext.product.app.ProductFacade;
import com.modeunsa.boundedcontext.product.domain.ProductCategory;
import com.modeunsa.boundedcontext.product.domain.ProductStatus;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.security.CustomUserDetails;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.global.status.SuccessStatus;
import com.modeunsa.shared.product.dto.ProductCreateRequest;
import com.modeunsa.shared.product.dto.ProductDetailResponse;
import com.modeunsa.shared.product.dto.ProductFavoriteResponse;
import com.modeunsa.shared.product.dto.ProductOrderResponse;
import com.modeunsa.shared.product.dto.ProductOrderValidateRequest;
import com.modeunsa.shared.product.dto.ProductResponse;
import com.modeunsa.shared.product.dto.ProductStockResponse;
import com.modeunsa.shared.product.dto.ProductStockUpdateRequest;
import com.modeunsa.shared.product.dto.ProductUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
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
      @AuthenticationPrincipal CustomUserDetails user,
      @Valid @RequestBody ProductCreateRequest productCreateRequest) {
    // TODO: sellerId userDetail 가져오도록 수정
    ProductDetailResponse productDetailResponse =
        productFacade.createProduct(user.getMemberId(), productCreateRequest);
    return ApiResponse.onSuccess(SuccessStatus.CREATED, productDetailResponse);
  }

  @Operation(summary = "상품 상세 조회", description = "상품 id를 이용해 상품 상세를 조회합니다.")
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse> getProduct(
      @AuthenticationPrincipal CustomUserDetails user, @PathVariable(name = "id") Long productId) {
    if (productId == null) {
      throw new GeneralException(ErrorStatus.PRODUCT_FIELD_REQUIRED);
    }
    ProductDetailResponse productDetailResponse =
        productFacade.getProduct(user != null ? user.getMemberId() : null, productId);
    return ApiResponse.onSuccess(SuccessStatus.OK, productDetailResponse);
  }

  @Operation(summary = "상품 리스트 조회", description = "상품 리스트를 조회합니다.")
  @GetMapping
  public ResponseEntity<ApiResponse> getProducts(
      @RequestParam(name = "category") ProductCategory category,
      @RequestParam(name = "page") int page,
      @RequestParam(name = "size") int size) {
    Pageable pageable =
        PageRequest.of(
            page, size, Sort.by(Sort.Direction.DESC, "createdAt") // 정렬 고정
            );
    Page<ProductResponse> productResponses = productFacade.getProducts(category, pageable);
    return ApiResponse.onSuccess(SuccessStatus.OK, productResponses);
  }

  @Operation(summary = "상품 수정", description = "상품을 수정합니다.")
  @PatchMapping("/{id}")
  public ResponseEntity<ApiResponse> updateProduct(
      @AuthenticationPrincipal CustomUserDetails user,
      @PathVariable(name = "id") Long productId,
      @Valid @RequestBody ProductUpdateRequest productRequest) {
    // TODO: sellerId userDetail 가져오도록 수정
    ProductDetailResponse productDetailResponse =
        productFacade.updateProduct(user.getMemberId(), productId, productRequest);
    return ApiResponse.onSuccess(SuccessStatus.OK, productDetailResponse);
  }

  @Operation(summary = "상품 상태 변경", description = "상품의 등록 상태를 변경합니다 (DRAFT, COMPLETED, CANCELED)")
  @PatchMapping("/{id}/status")
  public ResponseEntity<ApiResponse> updateProductStatus(
      @AuthenticationPrincipal CustomUserDetails user,
      @PathVariable(name = "id") Long productId,
      @RequestParam(name = "status") ProductStatus productStatus) {
    ProductDetailResponse productDetailResponse =
        productFacade.updateProductStatus(user.getMemberId(), productId, productStatus);
    return ApiResponse.onSuccess(SuccessStatus.OK, productDetailResponse);
  }

  @Operation(summary = "관심상품 추가", description = "상품을 관심상품에 추가합니다.")
  @PostMapping("/{id}/favorite")
  public ResponseEntity<ApiResponse> createProductFavorite(
      @AuthenticationPrincipal CustomUserDetails user,
      @Valid @PathVariable(name = "id") Long productId) {
    Long memberId = user.getMemberId();
    if (memberId == null || productId == null) {
      throw new GeneralException(ErrorStatus.PRODUCT_FIELD_REQUIRED);
    }
    productFacade.createProductFavorite(memberId, productId);
    return ApiResponse.onSuccess(SuccessStatus.CREATED);
  }

  @Operation(summary = "관심상품 삭제", description = "상품을 관심상품에서 삭제합니다.")
  @DeleteMapping("/{id}/favorite")
  public ResponseEntity<ApiResponse> deleteProductFavorite(
      @AuthenticationPrincipal CustomUserDetails user,
      @Valid @PathVariable(name = "id") Long productId) {
    Long memberId = user.getMemberId();
    if (memberId == null || productId == null) {
      throw new GeneralException(ErrorStatus.PRODUCT_FIELD_REQUIRED);
    }
    productFacade.deleteProductFavorite(memberId, productId);
    return ApiResponse.onSuccess(SuccessStatus.OK);
  }

  @Operation(summary = "관심상품 내역 조회", description = "마이페이지 > 관심상품 내역을 조회합니다.")
  @GetMapping("/favorites")
  public ResponseEntity<ApiResponse> getProductFavorites(
      @AuthenticationPrincipal CustomUserDetails user,
      @RequestParam(name = "page") int page,
      @RequestParam(name = "size") int size) {
    Pageable pageable =
        PageRequest.of(
            page, size, Sort.by(Sort.Direction.DESC, "createdAt") // 정렬 고정
            );
    Page<ProductFavoriteResponse> productFavoriteResponses =
        productFacade.getProductFavorites(user.getMemberId(), pageable);
    return ApiResponse.onSuccess(SuccessStatus.OK, productFavoriteResponses);
  }

  @Operation(summary = "주문 상품 검증용 상품 리스트 조회", description = "주문 직전 상품의 유효성 검증을 위해 상품 리스트를 조회합니다.")
  @PostMapping("/validate-order")
  public List<ProductOrderResponse> validateOrderProducts(
      @Valid @RequestBody ProductOrderValidateRequest productOrderValidateRequest) {
    return productFacade.getProducts(productOrderValidateRequest.productIds());
  }

  @Operation(summary = "재고 차감 API", description = "주문 생성 시 재고를 차감합니다.")
  @PatchMapping("/stock")
  public List<ProductStockResponse> deductStock(
      @Valid @RequestBody ProductStockUpdateRequest productStockUpdateRequest) {
    return productFacade.deductStock(productStockUpdateRequest);
  }

  @Operation(summary = "(판매자용) 상품 리스트 조회", description = "판매자용 상품 리스트를 조회합니다.")
  @GetMapping("/products/seller")
  public ResponseEntity<ApiResponse> getProductsForSeller(
      @AuthenticationPrincipal CustomUserDetails user,
      @RequestParam(name = "page") int page,
      @RequestParam(name = "size") int size) {
    Pageable pageable =
        PageRequest.of(
            page, size, Sort.by(Sort.Direction.DESC, "createdAt") // 정렬 고정
            );
    Page<ProductResponse> productResponses =
        productFacade.getProducts(user.getMemberId(), pageable);
    return ApiResponse.onSuccess(SuccessStatus.OK, productResponses);
  }
}
