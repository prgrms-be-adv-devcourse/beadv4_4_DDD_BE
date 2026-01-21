package com.modeunsa.boundedcontext.product.in;

import com.modeunsa.boundedcontext.product.domain.ProductMember;
import com.modeunsa.boundedcontext.product.domain.ProductMemberSeller;
import com.modeunsa.boundedcontext.product.out.ProductMemberRepository;
import com.modeunsa.boundedcontext.product.out.ProductMemberSellerRepository;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;

@Log4j2
@Configuration
public class ProductMemberDataInit {
  private final ProductMemberDataInit self;
  private final ProductMemberSellerRepository productMemberSellerRepository;
  private final ProductMemberRepository productMemberRepository;

  public ProductMemberDataInit(
      @Lazy ProductMemberDataInit self,
      ProductMemberSellerRepository productMemberSellerRepository,
      ProductMemberRepository productMemberRepository) {

    this.self = self;
    this.productMemberSellerRepository = productMemberSellerRepository;
    this.productMemberRepository = productMemberRepository;
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
    if (productMemberRepository.count() > 0 || productMemberSellerRepository.count() > 0) {
      return;
    }
    ProductMember member1 =
        ProductMember.builder()
            .email("123@abc.com")
            .phoneNumber("01020002000")
            .realName("member1")
            .build();
    productMemberRepository.save(member1);
    ProductMember member2 =
        ProductMember.builder()
            .email("345@abc.com")
            .phoneNumber("01030002000")
            .realName("member2")
            .build();
    productMemberRepository.save(member2);
    ProductMemberSeller seller1 =
        ProductMemberSeller.builder().businessName("나이키").representativeName("나이키대표").build();
    productMemberSellerRepository.save(seller1);
    ProductMemberSeller seller2 =
        ProductMemberSeller.builder().businessName("아디다스").representativeName("아디다스대표").build();
    productMemberSellerRepository.save(seller2);
  }
}
