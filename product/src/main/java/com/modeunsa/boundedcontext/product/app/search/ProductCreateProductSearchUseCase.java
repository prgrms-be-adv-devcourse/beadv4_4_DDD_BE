package com.modeunsa.boundedcontext.product.app.search;

import com.modeunsa.boundedcontext.product.domain.search.document.ProductSearch;
import com.modeunsa.boundedcontext.product.out.search.ProductSearchRepository;
import com.modeunsa.shared.product.dto.search.ProductSearchRequest;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@ConditionalOnProperty(name = "app.elasticsearch.enabled", havingValue = "true")
@Service
@RequiredArgsConstructor
public class ProductCreateProductSearchUseCase {

  private final ProductSearchRepository productSearchRepository;

  public ProductSearch createProductSearch(ProductSearchRequest request) {
    ProductSearch productSearch =
        ProductSearch.create(
            request.id().toString(),
            request.name(),
            request.sellerBusinessName(),
            request.description(),
            request.category(),
            request.saleStatus(),
            request.productStatus(),
            request.salePrice(),
            request.primaryImageUrl(),
            request.createdAt().atZone(ZoneId.of("Asia/Seoul")).toInstant());
    return productSearchRepository.save(productSearch);
  }
}
