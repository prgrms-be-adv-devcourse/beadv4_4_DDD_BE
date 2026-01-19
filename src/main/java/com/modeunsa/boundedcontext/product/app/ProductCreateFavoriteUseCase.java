package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.ProductMember;
import com.modeunsa.boundedcontext.product.out.ProductFavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductCreateFavoriteUseCase {

  private final ProductSupport productSupport;
  private final ProductFavoriteRepository productFavoriteRepository;

  public void createProductFavorite(Long memberId, Long productId) {
    // 회원 검증
    ProductMember member = productSupport.getProductMember(memberId);

    // 상품 검증
    Product product = productSupport.getProduct(productId);

    boolean inserted = productFavoriteRepository.insertIgnore(member.getId(), product.getId()) == 1;
    if (inserted) {
      productSupport.increaseFavoriteCount(product.getId());
    }
  }
}
