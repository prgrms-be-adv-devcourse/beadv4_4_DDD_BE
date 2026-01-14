package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.ProductCategory;
import com.modeunsa.boundedcontext.product.domain.ProductStatus;
import com.modeunsa.boundedcontext.product.domain.SaleStatus;
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

  private static SaleStatus[] PRODUCT_DISPLAY_SALE_STATUSES = {
    SaleStatus.SALE, SaleStatus.SOLD_OUT
  };

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

  public Page<Product> getProducts(ProductCategory category, Pageable pageable) {
    return productRepository.findAllByCategoryAndSaleStatusInAndProductStatus(
        category, PRODUCT_DISPLAY_SALE_STATUSES, ProductStatus.COMPLETED, pageable);
  }
}
