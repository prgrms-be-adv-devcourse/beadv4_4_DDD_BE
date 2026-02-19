package com.modeunsa.boundedcontext.product.in;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;
import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import com.modeunsa.boundedcontext.product.app.search.ProductSearchFacade;
import com.modeunsa.shared.product.dto.search.ProductSearchRequest;
import com.modeunsa.shared.product.event.ProductCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
public class ProductSearchEventListener {

  private final ProductSearchFacade productSearchFacade;

  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Transactional(propagation = REQUIRES_NEW)
  public void handle(ProductCreatedEvent event) {
    productSearchFacade.createProductSearch(
        new ProductSearchRequest(
            event.productDto().getId(),
            event.productDto().getName(),
            event.productDto().getSellerBusinessName(),
            event.productDto().getDescription(),
            event.productDto().getCategory(),
            event.productDto().getSaleStatus(),
            event.productDto().getProductStatus(),
            event.productDto().getSalePrice(),
            event.productDto().getPrimaryImageUrl(),
            event.productDto().getCreatedAt()));
  }
}
