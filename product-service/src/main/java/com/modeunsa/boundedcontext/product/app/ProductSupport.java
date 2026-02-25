package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.api.pagination.KeywordCursorDto;
import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.ProductCategory;
import com.modeunsa.boundedcontext.product.domain.ProductFavorite;
import com.modeunsa.boundedcontext.product.domain.ProductMember;
import com.modeunsa.boundedcontext.product.domain.ProductMemberSeller;
import com.modeunsa.boundedcontext.product.domain.ProductStatus;
import com.modeunsa.boundedcontext.product.domain.SaleStatus;
import com.modeunsa.boundedcontext.product.out.ProductFavoriteRepository;
import com.modeunsa.boundedcontext.product.out.ProductMemberRepository;
import com.modeunsa.boundedcontext.product.out.ProductMemberSellerRepository;
import com.modeunsa.boundedcontext.product.out.ProductRepository;
import com.modeunsa.boundedcontext.product.out.persistence.ProductJpaSearchAdapter;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductSupport {

  private final ProductMemberSellerRepository productMemberSellerRepository;
  private final ProductMemberRepository productMemberRepository;
  private final ProductRepository productRepository;
  private final ProductFavoriteRepository productFavoriteRepository;
  private final ProductJpaSearchAdapter jpaSearchAdapter;

  public boolean existsBySellerId(Long sellerId) {
    return productMemberSellerRepository.existsById(sellerId);
  }

  public boolean existsByMemberId(Long memberId) {
    return productMemberRepository.existsById(memberId);
  }

  public Product getProduct(Long productId) {
    return productRepository
        .findById(productId)
        .orElseThrow(() -> new GeneralException(ErrorStatus.PRODUCT_NOT_FOUND));
  }

  public Product getProduct(Long productId, Long sellerId) {
    return productRepository
        .findByIdAndSellerId(productId, sellerId)
        .orElseThrow(() -> new GeneralException(ErrorStatus.PRODUCT_SELLER_INCORRECT));
  }

  public Page<Product> getProducts(String category, Pageable pageable) {
    ProductCategory categoryEnum = ProductCategory.from(category);
    return productRepository.searchByConditions(categoryEnum, pageable);
  }

  public Slice<Product> getProducts(String keyword, KeywordCursorDto cursor, int size) {
    return jpaSearchAdapter.search(keyword, cursor, size);
  }

  public Page<Product> getProducts(
      Long sellerId,
      String name,
      ProductCategory category,
      SaleStatus saleStatus,
      ProductStatus productStatus,
      Pageable pageable) {
    ProductMemberSeller seller = this.getProductMemberSeller(sellerId);
    return productRepository.findAllBySeller(
        seller, name, category, saleStatus, productStatus, pageable);
  }

  public List<Product> getProducts(List<Long> productIds) {
    return productRepository.findAllById(productIds);
  }

  public ProductMemberSeller getProductMemberSeller(Long sellerId) {
    return productMemberSellerRepository
        .findById(sellerId)
        .orElseThrow(() -> new GeneralException(ErrorStatus.PRODUCT_SELLER_NOT_FOUND));
  }

  public ProductMember getProductMember(Long memberId) {
    return productMemberRepository
        .findById(memberId)
        .orElseThrow(() -> new GeneralException(ErrorStatus.PRODUCT_MEMBER_NOT_FOUND));
  }

  public int increaseFavoriteCount(Long productId) {
    return productRepository.increaseFavoriteCount(productId);
  }

  public int decreaseFavoriteCount(Long productId) {
    return productRepository.decreaseFavoriteCount(productId);
  }

  public boolean existsProductFavorite(Long memberId, Long productId) {
    return productFavoriteRepository.existsByMemberIdAndProductId(memberId, productId);
  }

  public Page<ProductFavorite> getProductFavorites(Long memberId, Pageable pageable) {
    return productFavoriteRepository.findAllByMemberId(memberId, pageable);
  }

  public List<ProductFavorite> getRecentFavoriteProducts(Long memberId, int n) {
    Pageable pageable = PageRequest.of(0, n);
    return productFavoriteRepository.findRecentFavoritesWithProduct(memberId, pageable);
  }
}
