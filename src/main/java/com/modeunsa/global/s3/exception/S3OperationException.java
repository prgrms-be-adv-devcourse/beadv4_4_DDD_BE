package com.modeunsa.global.s3.exception;

public class S3OperationException extends S3BaseException {
  public S3OperationException(String operation, Throwable cause) {
    super("S3 operation during " + operation, cause);
  }
}
