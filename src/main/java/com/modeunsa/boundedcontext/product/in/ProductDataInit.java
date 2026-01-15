package com.modeunsa.boundedcontext.product.in;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.ProductCategory;
import com.modeunsa.boundedcontext.product.domain.ProductMemberSeller;
import com.modeunsa.boundedcontext.product.domain.ProductStatus;
import com.modeunsa.boundedcontext.product.domain.SaleStatus;
import com.modeunsa.boundedcontext.product.out.ProductMemberSellerRepository;
import com.modeunsa.boundedcontext.product.out.ProductRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
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

  public ProductDataInit(
      @Lazy ProductDataInit self,
      ProductRepository productRepository,
      ProductMemberSellerRepository productMemberSellerRepository) {
    this.self = self;
    this.productRepository = productRepository;
    this.productMemberSellerRepository = productMemberSellerRepository;
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
    ProductMemberSeller seller1 = productMemberSellerRepository.findById(1L).get();
    ProductMemberSeller seller2 = productMemberSellerRepository.findById(2L).get();

    Product product1 =
        Product.builder()
            .seller(seller1)
            .name("코트")
            .category(ProductCategory.OUTER)
            .price(BigDecimal.valueOf(10000))
            .salePrice(BigDecimal.valueOf(20000))
            .quantity(10)
            .saleStatus(SaleStatus.SALE)
            .description("코트 설명입니다.")
            .build();
    Product product2 =
        Product.builder()
            .seller(seller1)
            .name("맨투맨")
            .category(ProductCategory.OUTER)
            .price(BigDecimal.valueOf(14000))
            .salePrice(BigDecimal.valueOf(30000))
            .quantity(20)
            .description("맨투맨 설명입니다.")
            .saleStatus(SaleStatus.SALE)
            .productStatus(ProductStatus.COMPLETED)
            .build();
    Product product3 =
        Product.builder()
            .name("가디건")
            .seller(seller2)
            .category(ProductCategory.OUTER)
            .price(BigDecimal.valueOf(14000))
            .salePrice(BigDecimal.valueOf(30000))
            .quantity(20)
            .description("가디건 설명입니다.")
            .saleStatus(SaleStatus.SOLD_OUT)
            .build();

    Product product4 =
        Product.builder()
            .name("후드티")
            .seller(seller2)
            .category(ProductCategory.OUTER)
            .price(BigDecimal.valueOf(14000))
            .salePrice(BigDecimal.valueOf(30000))
            .quantity(20)
            .description("후드티 설명입니다.")
            .saleStatus(SaleStatus.NOT_SALE)
            .build();

    productRepository.save(product1);
    productRepository.save(product2);
    productRepository.save(product3);
    productRepository.save(product4);
  }
}
