package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.boundedcontext.product.out.ProductMemberSellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductSupport {

  private final ProductMemberSellerRepository productMemberSellerRepository;

  public boolean existsBySellerId(Long sellerId) {
    return productMemberSellerRepository.existsById(sellerId);
  }
}
