package com.modeunsa.boundedcontext.product.in.api.v2;

import com.modeunsa.boundedcontext.product.app.search.ProductSearchUseCase;
import com.modeunsa.boundedcontext.product.domain.search.document.ProductSearch;
import com.modeunsa.shared.product.dto.search.ProductSearchRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Product Search", description = "검색 상품 도메인 API")
@RestController
@RequestMapping("/api/v2/products/searches")
@RequiredArgsConstructor
public class ProductSearchController {

  private final ProductSearchUseCase productSearchUseCase;

  @Operation(summary = "ES 상품 등록", description = "상품을 등록하면 ElasticSearch에도 save된다.")
  @PostMapping
  public ProductSearch create(@RequestBody ProductSearchRequest request) {
    return productSearchUseCase.createProductSearch(
        request.name(),
        request.description(),
        request.category(),
        request.saleStatus(),
        request.price());
  }

  @Operation(summary = "검색 상품 조회", description = "name, description 기준으로 상품을 검색한다.")
  @GetMapping
  public List<ProductSearch> search(@RequestParam String keyword) {
    return productSearchUseCase.search(keyword);
  }
}
