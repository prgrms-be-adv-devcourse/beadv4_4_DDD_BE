package com.modeunsa.boundedcontext.product.out;

import com.modeunsa.boundedcontext.product.domain.ProductMemberSeller;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductMemberSellerRepository extends JpaRepository<ProductMemberSeller, Long> {

  Optional<ProductMemberSeller> findByMemberId(Long memberId);
}
