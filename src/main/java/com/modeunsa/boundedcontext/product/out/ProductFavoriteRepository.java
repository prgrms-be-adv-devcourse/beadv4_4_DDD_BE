package com.modeunsa.boundedcontext.product.out;

import com.modeunsa.boundedcontext.product.domain.ProductFavorite;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ProductFavoriteRepository extends JpaRepository<ProductFavorite, Long> {
  @Modifying
  @Query(
      nativeQuery = true,
      value =
          """
          INSERT IGNORE INTO product_favorite (member_id, product_id, created_at, updated_at)
          VALUES (:memberId, :productId, NOW(), NOW())
          """)
  int insertIgnore(@Param("memberId") Long memberId, @Param("productId") Long productId);

  int deleteByMemberIdAndProductId(Long memberId, Long productId);
}
