package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.ProductMemberSeller;
import com.modeunsa.boundedcontext.product.domain.ProductPolicy;
import com.modeunsa.boundedcontext.product.domain.ProductStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductUpdateProductStatusUseCase {

  private final ProductSupport productSupport;
  private final ProductPolicy productPolicy;

  public Product updateProductStatus(Long memberId, Long productId, ProductStatus productStatus) {
    // 1. 판매자 및 상품 검증
    ProductMemberSeller seller = productSupport.getProductMemberSellerByMemberId(memberId);
    Product product = productSupport.getProduct(productId, seller.getId());

    // 2. 정책 검증
    productPolicy.validateProductStatus(product.getProductStatus(), productStatus);

    product.updateProductStatus(productStatus);

    return product;
  }
}
