package com.modeunsa.boundedcontext.product.in;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.ProductCategory;
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

  public ProductDataInit(@Lazy ProductDataInit self, ProductRepository productRepository) {
    this.self = self;
    this.productRepository = productRepository;
  }

  @Bean
  @Order(1)
  public ApplicationRunner dataInitRunner() {
    return args -> {
      self.makeBaseProducts();
    };
  }

  @Transactional
  public void makeBaseProducts() {
    Product product1 =
        Product.builder()
            .name("코트")
            .category(ProductCategory.OUTER)
            .price(BigDecimal.valueOf(10000))
            .salePrice(BigDecimal.valueOf(20000))
            .qty(10)
            .description("코트 설명입니다.")
            .build();
    Product product2 =
        Product.builder()
            .name("맨투맨")
            .category(ProductCategory.UPPER)
            .price(BigDecimal.valueOf(14000))
            .salePrice(BigDecimal.valueOf(30000))
            .qty(20)
            .description("맨투맨 설명입니다.")
            .build();

    productRepository.save(product1);
    productRepository.save(product2);
  }
}
