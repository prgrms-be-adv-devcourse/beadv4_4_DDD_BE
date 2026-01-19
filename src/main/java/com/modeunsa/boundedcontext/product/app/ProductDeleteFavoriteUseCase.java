package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.ProductMember;
import com.modeunsa.boundedcontext.product.out.ProductFavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductDeleteFavoriteUseCase {

  private final ProductSupport productSupport;
  private final ProductFavoriteRepository productFavoriteRepository;

  public void deleteProductFavorite(Long memberId, Long productId) {
    ProductMember member = productSupport.getProductMember(memberId);
    Product product = productSupport.getProduct(productId);

    boolean deleted =
        productFavoriteRepository.deleteByMemberIdAndProductId(member.getId(), product.getId())
            == 1;
    if (deleted) {
      productSupport.decreaseFavoriteCount(product.getId());
    }
  }
}
