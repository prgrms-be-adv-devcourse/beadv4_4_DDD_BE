package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.boundedcontext.product.domain.ProductPolicy;
import com.modeunsa.boundedcontext.product.in.dto.ProductOrderDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductValidateOrderUseCase {

  private final ProductSupport productSupport;
  private final ProductMapper productMapper;

  public List<ProductOrderDto> validateOrder(List<Long> productIds) {
    return productSupport.getProducts(productIds).stream()
        .map(
            product -> {
              ProductOrderDto dto = productMapper.toProductOrderDto(product);
              ProductOrderDto finalDto =
                  dto.setIsAvailable(
                      ProductPolicy.ORDERABLE_SALE_STATUES.contains(dto.saleStatus()));
              return finalDto;
            })
        .toList();
  }
}
