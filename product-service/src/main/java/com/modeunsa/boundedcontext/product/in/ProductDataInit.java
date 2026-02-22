package com.modeunsa.boundedcontext.product.in;

import static com.modeunsa.boundedcontext.product.domain.SaleStatus.SALE;

import com.modeunsa.boundedcontext.product.app.ProductFacade;
import com.modeunsa.boundedcontext.product.app.ProductSupport;
import com.modeunsa.boundedcontext.product.domain.ProductCategory;
import com.modeunsa.boundedcontext.product.domain.ProductMember;
import com.modeunsa.boundedcontext.product.in.dto.ProductCreateRequest;
import com.modeunsa.boundedcontext.product.in.dto.ProductDetailResponse;
import com.modeunsa.boundedcontext.product.in.dto.ProductUpdateRequest;
import com.modeunsa.boundedcontext.product.out.ProductRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;

@Profile("!test")
@ConditionalOnProperty(name = "app.data-init.enabled", havingValue = "true", matchIfMissing = true)
// @Configuration
public class ProductDataInit {
  private final ProductDataInit self;
  private final ProductRepository productRepository;
  private final ProductFacade productFacade;
  private final ProductSupport productSupport;
  private final RedisTemplate<String, String> redisTemplate;

  public ProductDataInit(
      @Lazy ProductDataInit self,
      ProductRepository productRepository,
      ProductFacade productFacade,
      ProductSupport productSupport,
      RedisTemplate<String, String> redisTemplate) {

    this.self = self;
    this.productRepository = productRepository;
    this.productFacade = productFacade;
    this.productSupport = productSupport;
    this.redisTemplate = redisTemplate;
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

    redisTemplate.opsForValue().set("inventory:available:" + 1, String.valueOf(100));

    redisTemplate.opsForValue().set("inventory:available:" + 2, String.valueOf(100));

    redisTemplate.opsForValue().set("inventory:available:" + 3, String.valueOf(100));

    redisTemplate.opsForValue().set("inventory:available:" + 4, String.valueOf(100));

    redisTemplate.opsForValue().set("inventory:available:" + 5, String.valueOf(100));

    redisTemplate.opsForValue().set("inventory:available:" + 6, String.valueOf(100));

    redisTemplate.opsForValue().set("inventory:available:" + 7, String.valueOf(100));

    redisTemplate.opsForValue().set("inventory:available:" + 8, String.valueOf(100));

    if (productRepository.count() > 0) {
      return;
    }
    ProductMember member1 = productSupport.getProductMember(1L); // 최판매

    ProductDetailResponse product1 =
        productFacade.createProduct(
            member1.getId(),
            new ProductCreateRequest(
                "코트",
                ProductCategory.OUTER,
                "롱코트입니다.",
                BigDecimal.valueOf(10_000),
                BigDecimal.valueOf(20_000),
                List.of(
                    "https://team01-storage.s3.ap-northeast-2.amazonaws.com/dev/product/1/coat1.jpg",
                    "https://team01-storage.s3.ap-northeast-2.amazonaws.com/dev/product/1/coat2.jpg",
                    "https://team01-storage.s3.ap-northeast-2.amazonaws.com/dev/product/1/coat3.jpg",
                    "https://team01-storage.s3.ap-northeast-2.amazonaws.com/dev/product/1/coat4.jpg",
                    "https://team01-storage.s3.ap-northeast-2.amazonaws.com/dev/product/1/coat5.jpg")));
    productFacade.updateProduct(
        7L,
        member1.getId(),
        1L,
        new ProductUpdateRequest(
            "코트",
            ProductCategory.OUTER,
            "롱코트입니다.",
            BigDecimal.valueOf(10_000),
            BigDecimal.valueOf(20_000),
            SALE,
            List.of(
                "https://team01-storage.s3.ap-northeast-2.amazonaws.com/dev/product/1/coat1.jpg",
                "https://team01-storage.s3.ap-northeast-2.amazonaws.com/dev/product/1/coat2.jpg",
                "https://team01-storage.s3.ap-northeast-2.amazonaws.com/dev/product/1/coat3.jpg",
                "https://team01-storage.s3.ap-northeast-2.amazonaws.com/dev/product/1/coat4.jpg",
                "https://team01-storage.s3.ap-northeast-2.amazonaws.com/dev/product/1/coat5.jpg")));

    ProductDetailResponse product2 =
        productFacade.createProduct(
            member1.getId(),
            new ProductCreateRequest(
                "패딩",
                ProductCategory.OUTER,
                "구스다운 패딩입니다.",
                BigDecimal.valueOf(10_000),
                BigDecimal.valueOf(20_000),
                List.of(
                    "https://team01-storage.s3.ap-northeast-2.amazonaws.com/dev/product/1/ad3e23cd-155c-4ae9-80e3-40bcdefb1036.jpg",
                    "https://team01-storage.s3.ap-northeast-2.amazonaws.com/dev/product/1/3457593b-9a40-486a-b506-68224177bb8a.jpg")));
    productFacade.updateProduct(
        7L,
        member1.getId(),
        2L,
        new ProductUpdateRequest(
            "패딩",
            ProductCategory.OUTER,
            "구스다운 패딩입니다.",
            BigDecimal.valueOf(10_000),
            BigDecimal.valueOf(20_000),
            SALE,
            List.of(
                "https://team01-storage.s3.ap-northeast-2.amazonaws.com/dev/product/1/ad3e23cd-155c-4ae9-80e3-40bcdefb1036.jpg",
                "https://team01-storage.s3.ap-northeast-2.amazonaws.com/dev/product/1/3457593b-9a40-486a-b506-68224177bb8a.jpg")));

    ProductDetailResponse product3 =
        productFacade.createProduct(
            member1.getId(),
            new ProductCreateRequest(
                "경량패딩",
                ProductCategory.OUTER,
                "가볍게 입을 수 있는 경량패딩입니다.",
                BigDecimal.valueOf(10_000),
                BigDecimal.valueOf(20_000),
                null));
    productFacade.updateProduct(
        7L,
        member1.getId(),
        3L,
        new ProductUpdateRequest(
            "경량패딩",
            ProductCategory.OUTER,
            "가볍게 입을 수 있는 경량패딩입니다.",
            BigDecimal.valueOf(10_000),
            BigDecimal.valueOf(20_000),
            SALE,
            null));

    ProductDetailResponse product4 =
        productFacade.createProduct(
            member1.getId(),
            new ProductCreateRequest(
                "맨투맨",
                ProductCategory.UPPER,
                "설명설명222",
                BigDecimal.valueOf(20_000),
                BigDecimal.valueOf(30_000),
                null));
    productFacade.updateProduct(
        7L,
        member1.getId(),
        4L,
        new ProductUpdateRequest(
            "맨투맨",
            ProductCategory.UPPER,
            "설명설명222",
            BigDecimal.valueOf(20_000),
            BigDecimal.valueOf(30_000),
            SALE,
            null));

    ProductDetailResponse product5 =
        productFacade.createProduct(
            member1.getId(),
            new ProductCreateRequest(
                "양말",
                ProductCategory.SHOES,
                "설명설명3",
                BigDecimal.valueOf(30_000),
                BigDecimal.valueOf(40_000),
                null));
    productFacade.updateProduct(
        7L,
        member1.getId(),
        5L,
        new ProductUpdateRequest(
            "양말",
            ProductCategory.SHOES,
            "설명설명3",
            BigDecimal.valueOf(30_000),
            BigDecimal.valueOf(40_000),
            SALE,
            null));

    ProductDetailResponse product6 =
        productFacade.createProduct(
            member1.getId(),
            new ProductCreateRequest(
                "바지",
                ProductCategory.LOWER,
                "설명설명4",
                BigDecimal.valueOf(40_000),
                BigDecimal.valueOf(50_000),
                null));
    productFacade.updateProduct(
        7L,
        member1.getId(),
        6L,
        new ProductUpdateRequest(
            "바지",
            ProductCategory.LOWER,
            "설명설명4",
            BigDecimal.valueOf(40_000),
            BigDecimal.valueOf(50_000),
            SALE,
            null));

    ProductDetailResponse product7 =
        productFacade.createProduct(
            member1.getId(),
            new ProductCreateRequest(
                "모자",
                ProductCategory.CAP,
                "설명설명4",
                BigDecimal.valueOf(50_000),
                BigDecimal.valueOf(50_000),
                null));
    productFacade.updateProduct(
        7L,
        member1.getId(),
        7L,
        new ProductUpdateRequest(
            "모자",
            ProductCategory.CAP,
            "설명설명4",
            BigDecimal.valueOf(50_000),
            BigDecimal.valueOf(50_000),
            SALE,
            null));

    ProductDetailResponse product8 =
        productFacade.createProduct(
            member1.getId(),
            new ProductCreateRequest(
                "신발",
                ProductCategory.SHOES,
                "설명설명4",
                BigDecimal.valueOf(60_000),
                BigDecimal.valueOf(70_000),
                null));
    productFacade.updateProduct(
        7L,
        member1.getId(),
        8L,
        new ProductUpdateRequest(
            "신발",
            ProductCategory.SHOES,
            "설명설명4",
            BigDecimal.valueOf(60_000),
            BigDecimal.valueOf(70_000),
            SALE,
            null));
  }
}
