package com.modeunsa.boundedcontext.product.in;

import com.modeunsa.boundedcontext.product.app.ProductFacade;
import com.modeunsa.boundedcontext.product.domain.ProductCategory;
import com.modeunsa.boundedcontext.product.domain.ProductMemberSeller;
import com.modeunsa.boundedcontext.product.out.ProductMemberSellerRepository;
import com.modeunsa.boundedcontext.product.out.ProductRepository;
import com.modeunsa.shared.product.dto.ProductCreateRequest;
import com.modeunsa.shared.product.dto.ProductResponse;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;

@Configuration
public class ProductDataInit {
  private final ProductDataInit self;
  private final ProductRepository productRepository;
  private final ProductMemberSellerRepository productMemberSellerRepository;
  private final ProductFacade productFacade;

  public ProductDataInit(
      @Lazy ProductDataInit self,
      ProductRepository productRepository,
      ProductMemberSellerRepository productMemberSellerRepository,
      ProductFacade productFacade) {

    this.self = self;
    this.productRepository = productRepository;
    this.productMemberSellerRepository = productMemberSellerRepository;
    this.productFacade = productFacade;
  }

  @Bean
  @Order(2)
  public ApplicationRunner productDataInitRunner() {
    return args -> {
      self.makeBaseProducts();
    };
  }

  @Transactional
  public void makeBaseProducts() {
    if (productRepository.count() > 0) {
      return;
    }
    ProductMemberSeller seller1 = productMemberSellerRepository.findById(1L).get();

    ProductResponse product1 =
        productFacade.createProduct(
            seller1.getId(),
            new ProductCreateRequest(
                "코트",
                ProductCategory.OUTER,
                "롱코트입니다.",
                BigDecimal.valueOf(10_000),
                BigDecimal.valueOf(20_000),
                10,
                List.of(
                    "https://team01-storage.s3.ap-northeast-2.amazonaws.com/dev/product/1/coat1.jpg",
                    "https://team01-storage.s3.ap-northeast-2.amazonaws.com/dev/product/1/coat2.jpg",
                    "https://team01-storage.s3.ap-northeast-2.amazonaws.com/dev/product/1/coat3.jpg",
                    "https://team01-storage.s3.ap-northeast-2.amazonaws.com/dev/product/1/coat4.jpg",
                    "https://team01-storage.s3.ap-northeast-2.amazonaws.com/dev/product/1/coat5.jpg")));

    ProductResponse product2 =
        productFacade.createProduct(
            seller1.getId(),
            new ProductCreateRequest(
                "패딩",
                ProductCategory.OUTER,
                "구스다운 패딩입니다.",
                BigDecimal.valueOf(10_000),
                BigDecimal.valueOf(20_000),
                100,
                List.of(
                    "https://team01-storage.s3.ap-northeast-2.amazonaws.com/dev/product/1/ad3e23cd-155c-4ae9-80e3-40bcdefb1036.jpg",
                    "https://team01-storage.s3.ap-northeast-2.amazonaws.com/dev/product/1/3457593b-9a40-486a-b506-68224177bb8a.jpg")));

    ProductResponse product3 =
        productFacade.createProduct(
            seller1.getId(),
            new ProductCreateRequest(
                "경량패딩",
                ProductCategory.OUTER,
                "가볍게 입을 수 있는 경량패딩입니다.",
                BigDecimal.valueOf(10_000),
                BigDecimal.valueOf(20_000),
                20,
                null));

    ProductResponse product4 =
        productFacade.createProduct(
            seller1.getId(),
            new ProductCreateRequest(
                "맨투맨",
                ProductCategory.UPPER,
                "설명설명222",
                BigDecimal.valueOf(20_000),
                BigDecimal.valueOf(30_000),
                100,
                null));

    ProductResponse product5 =
        productFacade.createProduct(
            seller1.getId(),
            new ProductCreateRequest(
                "양말",
                ProductCategory.SHOES,
                "설명설명3",
                BigDecimal.valueOf(30_000),
                BigDecimal.valueOf(40_000),
                50,
                null));

    ProductResponse product6 =
        productFacade.createProduct(
            seller1.getId(),
            new ProductCreateRequest(
                "바지",
                ProductCategory.LOWER,
                "설명설명4",
                BigDecimal.valueOf(40_000),
                BigDecimal.valueOf(50_000),
                120,
                null));

    ProductResponse product7 =
        productFacade.createProduct(
            seller1.getId(),
            new ProductCreateRequest(
                "모자",
                ProductCategory.CAP,
                "설명설명4",
                BigDecimal.valueOf(50_000),
                BigDecimal.valueOf(50_000),
                100,
                null));

    ProductResponse product8 =
        productFacade.createProduct(
            seller1.getId(),
            new ProductCreateRequest(
                "신발",
                ProductCategory.SHOES,
                "설명설명4",
                BigDecimal.valueOf(60_000),
                BigDecimal.valueOf(70_000),
                5,
                null));
  }
}
