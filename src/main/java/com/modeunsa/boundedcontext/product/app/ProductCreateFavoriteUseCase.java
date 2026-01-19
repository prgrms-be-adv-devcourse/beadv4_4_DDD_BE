package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.ProductMember;
import com.modeunsa.boundedcontext.product.out.ProductFavoriteRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductCreateFavoriteUseCase {

  private final ProductSupport productSupport;
  private final ProductFavoriteRepository productFavoriteRepository;

  public void createProductFavorite(Long memberId, Long productId) {
    // 회원 검증
    if (memberId == null || !productSupport.existsByMemberId(memberId)) {
      throw new GeneralException(ErrorStatus.PRODUCT_MEMBER_NOT_FOUND);
    }
    ProductMember member = productSupport.getProductMember(memberId);

    // 상품 검증
    Product product = productSupport.getProduct(productId);

    boolean inserted = productFavoriteRepository.insertIgnore(member.getId(), product.getId()) == 1;
    if (inserted) {
      productSupport.increaseFavoriteCount(product.getId());
    }
  }
}
