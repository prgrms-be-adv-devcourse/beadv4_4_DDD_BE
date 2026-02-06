package com.modeunsa.boundedcontext.product.app.search;

import com.modeunsa.boundedcontext.product.domain.search.document.ProductSearch;
import com.modeunsa.shared.product.dto.search.ProductSearchRequest;
import com.modeunsa.shared.product.dto.search.ProductSearchResponse;
import java.util.List;
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
        productSearchUseCase.createproductSearch(
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

  public List<ProductSearchResponse> findAll() {
    return productSearchUseCase.findAll().stream()
        .map(
            ps ->
                new ProductSearchResponse(
                    ps.getId(),
                    ps.getName(),
                    ps.getDescription(),
                    ps.getCategory(),
                    ps.getSaleStatus(),
                    ps.getPrice(),
                    ps.getCreatedAt(),
                    ps.getUpdatedAt()))
        .toList();
  }

  public ProductSearchResponse findById(String id) {
    ProductSearch ps = productSearchUseCase.findById(id).orElseThrow();

    return new ProductSearchResponse(
        ps.getId(),
        ps.getName(),
        ps.getDescription(),
        ps.getCategory(),
        ps.getSaleStatus(),
        ps.getPrice(),
        ps.getCreatedAt(),
        ps.getUpdatedAt());
  }
}
