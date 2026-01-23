package com.modeunsa.global.s3.exception;

public class S3FileNotFoundException extends S3BaseException {
  public S3FileNotFoundException(String operation, Throwable cause) {
    super("S3 file not found during " + operation, cause);
  }
}
