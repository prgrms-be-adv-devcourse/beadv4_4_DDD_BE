package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.ProductCategory;
import com.modeunsa.boundedcontext.product.domain.ProductMemberSeller;
import com.modeunsa.boundedcontext.product.domain.ProductPolicy;
import com.modeunsa.boundedcontext.product.out.ProductMemberSellerRepository;
import com.modeunsa.boundedcontext.product.out.ProductRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductSupport {

  private final ProductMemberSellerRepository productMemberSellerRepository;
  private final ProductRepository productRepository;

  public boolean existsBySellerId(Long sellerId) {
    return productMemberSellerRepository.existsById(sellerId);
  }

  public Product getProduct(Long productId) {
    return productRepository
        .findById(productId)
        .orElseThrow(() -> new GeneralException(ErrorStatus.PRODUCT_NOT_FOUND));
  }

  public Page<Product> getProducts(Long memberId, ProductCategory category, Pageable pageable) {
    // TODO: seller 가 보는 조회 쿼리와 member가 보는 조회 쿼리 다르게 가져가기
    return productRepository.findAllByCategoryAndSaleStatusInAndProductStatusIn(
        category,
        ProductPolicy.DISPLAYABLE_SALE_STATUES_FOR_ALL,
        ProductPolicy.DISPLAYABLE_PRODUCT_STATUSES_FOR_ALL,
        pageable);
  }

  public ProductMemberSeller getProductMemberSeller(Long sellerId) {
    return productMemberSellerRepository
        .findById(sellerId)
        .orElseThrow(() -> new GeneralException(ErrorStatus.SELLER_NOT_FOUND));
  }
}
