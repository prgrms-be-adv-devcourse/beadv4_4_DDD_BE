package com.modeunsa.boundedcontext.product;

import com.modeunsa.boundedcontext.product.app.ProductSupport;
import com.modeunsa.boundedcontext.product.domain.ProductMember;
import com.modeunsa.boundedcontext.product.out.ProductMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductCreateMemberUseCase {

  private final ProductSupport productSupport;
  private final ProductMemberRepository productMemberRepository;

  public void createMember(Long memberId, String email, String name, String phoneNumber) {
    if (productSupport.existsByMemberId(memberId)) {
      return;
    }
    ProductMember member = ProductMember.create(memberId, email, name, phoneNumber);
    productMemberRepository.save(member);
  }
}
