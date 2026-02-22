package com.modeunsa.global.exception.file;

public class S3BaseException extends RuntimeException {
  public S3BaseException(String message, Throwable cause) {
    super(message, cause);
  }
}
