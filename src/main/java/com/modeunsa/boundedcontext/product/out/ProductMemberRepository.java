package com.modeunsa.boundedcontext.product.out;

import com.modeunsa.boundedcontext.product.domain.ProductMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductMemberRepository extends JpaRepository<ProductMember, Long> {

  boolean existsById(Long memberId);
}
