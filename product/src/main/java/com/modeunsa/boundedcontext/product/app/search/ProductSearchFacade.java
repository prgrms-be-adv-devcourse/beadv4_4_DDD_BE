package com.modeunsa.boundedcontext.product.app.search;

import com.modeunsa.boundedcontext.product.app.ProductMapper;
import com.modeunsa.boundedcontext.product.domain.search.document.ProductSearch;
import com.modeunsa.shared.product.dto.search.ProductSearchRequest;
import com.modeunsa.shared.product.dto.search.ProductSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@ConditionalOnProperty(name = "app.elasticsearch.enabled", havingValue = "true")
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductSearchFacade {

  private final ProductCreateProductSearchUseCase productCreateProductSearchUseCase;
  private final ProductSearchReindexUseCase productSearchReindexUseCase;
  private final ProductSearchSupport productSearchSupport;
  private final ProductMapper productMapper;

  @Transactional
  public void createProductSearch(ProductSearchRequest request) {
    productCreateProductSearchUseCase.createProductSearch(request);
  }

  public Page<ProductSearchResponse> search(String keyword, int page, int size) {
    Page<ProductSearch> responses = productSearchSupport.searchByKeyword(keyword, page, size);
    return responses.map(productMapper::toProductSearchResponse);
  }

  public void reindexAll() {
    productSearchReindexUseCase.reindexAll();
  }

  public Page<String> autoComplete(String keyword) {
    return productSearchSupport.autoComplete(keyword);
  }
}
