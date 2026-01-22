package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.boundedcontext.product.domain.ProductMemberSeller;
import com.modeunsa.boundedcontext.product.out.ProductMemberSellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductCreateSellerUseCase {

  private final ProductMemberSellerRepository productMemberSellerRepository;

  public void createMemberSeller(Long sellerId, String businessName, String representativeName) {
    ProductMemberSeller seller =
        ProductMemberSeller.create(sellerId, businessName, representativeName);
    productMemberSellerRepository.save(seller);
  }
}
