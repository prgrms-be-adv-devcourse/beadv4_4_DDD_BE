package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.boundedcontext.product.domain.ProductMember;
import com.modeunsa.boundedcontext.product.out.ProductMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductUpdateMemberUseCase {

  private final ProductSupport productSupport;
  private final ProductMemberRepository productMemberRepository;

  public void updateMember(Long memberId, String realName, String email, String phoneNumber) {
    ProductMember member = productSupport.getProductMember(memberId);
    member.update(email, realName, phoneNumber);
    productMemberRepository.save(member);
  }
}
