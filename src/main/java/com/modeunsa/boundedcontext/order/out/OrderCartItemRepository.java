package com.modeunsa.boundedcontext.order.out;

import com.modeunsa.boundedcontext.order.domain.CartItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderCartItemRepository extends JpaRepository<CartItem, Long> {
  List<CartItem> findAllByMemberId(long memberId);

  void deleteByMemberId(Long memberId);

  Optional<CartItem> findByMemberIdAndProductId(Long memberId, Long productId);

  @Query(
      """
         select c.productId
          from CartItem c
          where c.memberId = :memberId
          order by c.createdAt desc
      """)
  List<Long> getRecentCartItems(@Param("memberId") Long memberId, Pageable pageable);
}
