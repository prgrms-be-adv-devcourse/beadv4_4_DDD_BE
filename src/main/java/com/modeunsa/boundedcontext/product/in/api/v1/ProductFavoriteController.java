package com.modeunsa.boundedcontext.product.in.api.v1;

import com.modeunsa.boundedcontext.product.app.ProductFacade;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.security.CustomUserDetails;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.global.status.SuccessStatus;
import com.modeunsa.shared.product.dto.ProductFavoriteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Product Favorite", description = "관심상품 도메인 API")
@RestController
@RequestMapping("/api/v1/products/favorites")
@RequiredArgsConstructor
public class ProductFavoriteController {

  private final ProductFacade productFacade;

  @Operation(summary = "관심상품 내역 조회", description = "마이페이지 > 관심상품 내역을 조회합니다.")
  @GetMapping
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

  @Operation(summary = "관심상품 추가", description = "상품을 관심상품에 추가합니다.")
  @PostMapping("/{id}")
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
  @DeleteMapping("/{id}")
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
}
