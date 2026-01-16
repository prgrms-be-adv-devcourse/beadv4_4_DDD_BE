package com.modeunsa.boundedcontext.product.in;

import com.modeunsa.boundedcontext.product.domain.ProductMemberSeller;
import com.modeunsa.boundedcontext.product.out.ProductMemberSellerRepository;
import jakarta.transaction.Transactional;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;

@Configuration
public class ProductMemberDataInit {
  private final ProductMemberDataInit self;
  private final ProductMemberSellerRepository productMemberSellerRepository;

  public ProductMemberDataInit(
      @Lazy ProductMemberDataInit self,
      ProductMemberSellerRepository productMemberSellerRepository) {
    this.self = self;
    this.productMemberSellerRepository = productMemberSellerRepository;
  }

  @Bean
  @Order(1)
  public ApplicationRunner sellerDataInitRunner() {
    return args -> {
      self.makeBaseSellers();
    };
  }

  @Transactional
  public void makeBaseSellers() {
    ProductMemberSeller seller1 =
        ProductMemberSeller.builder().businessName("나이키").representativeName("나이키대표").build();

    ProductMemberSeller seller2 =
        ProductMemberSeller.builder().businessName("아디다스").representativeName("아디다스대표").build();

    productMemberSellerRepository.save(seller1);
    productMemberSellerRepository.save(seller2);
  }
}
