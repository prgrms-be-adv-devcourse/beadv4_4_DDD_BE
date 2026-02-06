package com.modeunsa.boundedcontext.product.app.search;

import com.modeunsa.boundedcontext.product.domain.search.document.ProductSearch;
import com.modeunsa.shared.product.dto.search.ProductSearchRequest;
import com.modeunsa.shared.product.dto.search.ProductSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductSearchFacade {

  private final ProductSearchUseCase productSearchUseCase;

  @Transactional
  public ProductSearchResponse createProductSearch(ProductSearchRequest request) {

    ProductSearch productSearch =
        productSearchUseCase.createProductSearch(
            request.name(),
            request.description(),
            request.category(),
            request.saleStatus(),
            request.price());

    return new ProductSearchResponse(
        productSearch.getId(),
        productSearch.getName(),
        productSearch.getDescription(),
        productSearch.getCategory(),
        productSearch.getSaleStatus(),
        productSearch.getPrice(),
        productSearch.getCreatedAt(),
        productSearch.getUpdatedAt());
  }
}
