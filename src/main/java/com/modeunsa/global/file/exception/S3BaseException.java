package com.modeunsa.global.file.exception;

public class S3BaseException extends RuntimeException {
  public S3BaseException(String message, Throwable cause) {
    super(message, cause);
  }
}
