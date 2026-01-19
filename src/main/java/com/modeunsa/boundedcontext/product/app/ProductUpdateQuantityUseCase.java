package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.boundedcontext.product.domain.Product;
import com.modeunsa.boundedcontext.product.domain.exception.InvalidStockException;
import com.modeunsa.boundedcontext.product.out.ProductRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.order.dto.OrderDto;
import com.modeunsa.shared.order.dto.OrderItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductUpdateQuantityUseCase {

  private final ProductSupport productSupport;
  private final ProductRepository productRepository;

  public void updateQuantity(OrderDto orderDto) {
    for (OrderItemDto item : orderDto.getOrderItems()) {
      this.decreaseProductQuantity(item);
    }
  }

  private void decreaseProductQuantity(OrderItemDto itemDto) {
    if (itemDto.getProductId() == null) {
      throw new GeneralException(ErrorStatus.PRODUCT_NOT_FOUND);
    }
    Product product = productSupport.getProduct(itemDto.getProductId());
    try {
      product.decreaseQuantity(itemDto.getQuantity());
    } catch (InvalidStockException e) {
      throw new GeneralException(ErrorStatus.PRODUCT_OUT_OF_STOCK);
    }
    productRepository.save(product);
  }
}
