package com.modeunsa.boundedcontext.product.in.api.v2;

import com.modeunsa.boundedcontext.product.app.search.ProductSearchFacade;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.status.SuccessStatus;
import com.modeunsa.shared.product.dto.search.ProductSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@ConditionalOnProperty(name = "app.elasticsearch.enabled", havingValue = "true")
@Tag(name = "Product Search", description = "검색 상품 도메인 API")
@RestController
@RequestMapping("/api/v2/products/searches")
@RequiredArgsConstructor
public class ProductSearchController {

  private final ProductSearchFacade productSearchFacade;

  @Operation(summary = "검색 상품 조회", description = "name, description 기준으로 상품을 검색한다.")
  @GetMapping
  public ResponseEntity<ApiResponse> search(@RequestParam String keyword) {
    List<ProductSearchResponse> response = productSearchFacade.search(keyword);
    return ApiResponse.onSuccess(SuccessStatus.OK, response);
  }
}
