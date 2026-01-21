package com.modeunsa.boundedcontext.product;

import com.modeunsa.boundedcontext.product.domain.ProductMember;
import com.modeunsa.boundedcontext.product.out.ProductMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductCreateMemberUseCase {

  private final ProductMemberRepository productMemberRepository;

  public void createMember(Long memberId, String email, String name, String phoneNumber) {
    ProductMember member = ProductMember.create(memberId, email, name, phoneNumber);
    productMemberRepository.save(member);
  }
}
