package com.modeunsa.boundedcontext.order.out;

import com.modeunsa.boundedcontext.order.domain.CartItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderCartItemRepository extends JpaRepository<CartItem, Long> {
  List<CartItem> findAllByMemberId(long memberId);

  void deleteByMemberId(Long memberId);

  Optional<CartItem> findByMemberIdAndProductId(Long memberId, Long productId);
}
