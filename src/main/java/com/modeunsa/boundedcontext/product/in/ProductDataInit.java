package com.modeunsa.boundedcontext.product.in;

import com.modeunsa.boundedcontext.product.app.ProductCreateProductUseCase;
import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.ProductCategory;
import com.modeunsa.boundedcontext.product.domain.ProductMemberSeller;
import com.modeunsa.boundedcontext.product.out.ProductMemberRepository;
import com.modeunsa.boundedcontext.product.out.ProductMemberSellerRepository;
import com.modeunsa.boundedcontext.product.out.ProductRepository;
import com.modeunsa.shared.product.dto.ProductCreateRequest;
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
  private final ProductCreateProductUseCase productCreateProductUseCase;
  private final ProductMemberRepository productMemberRepository;

  public ProductDataInit(
      @Lazy ProductDataInit self,
      ProductRepository productRepository,
      ProductMemberSellerRepository productMemberSellerRepository,
      ProductCreateProductUseCase productCreateProductUseCase,
      ProductMemberRepository productMemberRepository) {

    this.self = self;
    this.productRepository = productRepository;
    this.productMemberSellerRepository = productMemberSellerRepository;
    this.productCreateProductUseCase = productCreateProductUseCase;
    this.productMemberRepository = productMemberRepository;
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
    ProductMemberSeller seller2 = productMemberSellerRepository.findById(2L).get();

    Product product1 =
        productCreateProductUseCase.createProduct(
            seller1.getId(),
            new ProductCreateRequest(
                "코트",
                ProductCategory.OUTER,
                "설명설명",
                BigDecimal.valueOf(10_000),
                BigDecimal.valueOf(20_000),
                10,
                List.of("img1", "img2")));

    Product product2 =
        productCreateProductUseCase.createProduct(
            seller1.getId(),
            new ProductCreateRequest(
                "맨투맨",
                ProductCategory.UPPER,
                "설명설명222",
                BigDecimal.valueOf(20_000),
                BigDecimal.valueOf(30_000),
                100,
                List.of("img1", "img2")));

    Product product3 =
        productCreateProductUseCase.createProduct(
            seller1.getId(),
            new ProductCreateRequest(
                "양말",
                ProductCategory.SHOES,
                "설명설명3",
                BigDecimal.valueOf(10_000),
                BigDecimal.valueOf(20_000),
                50,
                List.of("img1", "img2")));

    Product product4 =
        productCreateProductUseCase.createProduct(
            seller1.getId(),
            new ProductCreateRequest(
                "패딩",
                ProductCategory.OUTER,
                "설명설명4",
                BigDecimal.valueOf(10_000),
                BigDecimal.valueOf(20_000),
                120,
                List.of("img1", "img2")));

    Product product5 =
        productCreateProductUseCase.createProduct(
            seller2.getId(),
            new ProductCreateRequest(
                "모자",
                ProductCategory.CAP,
                "설명설명4",
                BigDecimal.valueOf(10_000),
                BigDecimal.valueOf(20_000),
                100,
                List.of("img1", "img2")));

    Product product6 =
        productCreateProductUseCase.createProduct(
            seller2.getId(),
            new ProductCreateRequest(
                "신발",
                ProductCategory.SHOES,
                "설명설명4",
                BigDecimal.valueOf(10_000),
                BigDecimal.valueOf(20_000),
                5,
                List.of("img1", "img2")));

    productRepository.save(product1);
    productRepository.save(product2);
    productRepository.save(product3);
    productRepository.save(product4);
    productRepository.save(product5);
    productRepository.save(product6);
  }
}
