package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.boundedcontext.product.domain.ProductPolicy;
import com.modeunsa.shared.product.dto.ProductOrderDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductValidateOrderUseCase {

  private final ProductSupport productSupport;
  private final ProductMapper productMapper;

  public List<ProductOrderDto> validateOrder(List<Long> productIds) {
    return productSupport.getProducts(productIds).stream()
        .map(
            product -> {
              ProductOrderDto dto = productMapper.toProductOrderDto(product);
              dto.setIsAvailable(ProductPolicy.ORDERABLE_SALE_STATUES.contains(dto.saleStatus()));
              return dto;
            })
        .toList();
  }
}
