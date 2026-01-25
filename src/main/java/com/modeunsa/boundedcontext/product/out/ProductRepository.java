package com.modeunsa.boundedcontext.product.out;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.ProductCategory;
import com.modeunsa.boundedcontext.product.domain.ProductStatus;
import com.modeunsa.boundedcontext.product.domain.SaleStatus;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

  Page<Product> findAllByCategoryAndSaleStatusInAndProductStatusIn(
      ProductCategory category,
      Collection<SaleStatus> saleStatus,
      Collection<ProductStatus> productStatus,
      Pageable pageable);

  @Modifying
  @Query(
      """
      update Product p
      set p.favoriteCount = p.favoriteCount + 1
      where p.id = :productId
      """)
  int increaseFavoriteCount(@Param("productId") Long productId);

  @Modifying
  @Query(
      """
      update Product p
      set p.favoriteCount = case when p.favoriteCount > 0 then p.favoriteCount - 1 else 0 end
      where p.id = :productId
      """)
  int decreaseFavoriteCount(@Param("productId") Long productId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from Product p where p.id = :id")
  Product findByIdForUpdate(@Param("id") Long id);

  Optional<Product> findByIdAndSellerId(Long productId, Long sellerId);
}
