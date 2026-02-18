package com.modeunsa.boundedcontext.product.in.api.v2;

import com.modeunsa.boundedcontext.product.app.search.ProductSearchFacade;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.status.SuccessStatus;
import com.modeunsa.shared.product.dto.search.ProductSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@ConditionalOnProperty(name = "app.elasticsearch.enabled", havingValue = "true")
@Tag(name = "Product Search", description = "검색 상품 도메인 API")
@RestController
@RequestMapping("/api/v2/products/search")
@RequiredArgsConstructor
public class ProductSearchController {

  private final ProductSearchFacade productSearchFacade;

  @Operation(summary = "검색 상품 조회", description = "name, description 기준으로 상품을 검색한다.")
  @GetMapping
  public ResponseEntity<ApiResponse> search(
      @RequestParam String keyword, @RequestParam int page, @RequestParam int size) {
    Page<ProductSearchResponse> response = productSearchFacade.search(keyword, page, size);
    return ApiResponse.onSuccess(SuccessStatus.OK, response);
  }

  @Operation(summary = "ES 재색인", description = "RDB의 상품 데이터를 재색인합니다. 기존 index 삭제가 선행되어야 합니다.")
  @PostMapping("/reindex")
  public ResponseEntity<ApiResponse> reindex() {
    // TODO: admin 권한만 수행 가능
    //    if (!userDetails.getRole().equals(MemberRole.ADMIN)) {
    //      throw new GeneralException(ErrorStatus.FORBIDDEN);
    //    }
    productSearchFacade.reindexAll();
    return ApiResponse.onSuccess(SuccessStatus.NO_CONTENT);
  }

  @Operation(summary = "검색어 자동 완성", description = "검색어 자동 완성 기능을 제공합니다.")
  @GetMapping("/auto-complete")
  public ResponseEntity<ApiResponse> autoComplete(@RequestParam String keyword) {
    return ApiResponse.onSuccess(SuccessStatus.OK, productSearchFacade.autoComplete(keyword));
  }
}
