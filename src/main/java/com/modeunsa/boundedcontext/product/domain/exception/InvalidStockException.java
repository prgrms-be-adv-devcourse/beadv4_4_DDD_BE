package com.modeunsa.boundedcontext.product.domain.exception;

public class InvalidStockException extends RuntimeException {

  public InvalidStockException(int current, int requested) {
    super("product out of stock: current=" + current + ", requested=" + requested);
  }
}
