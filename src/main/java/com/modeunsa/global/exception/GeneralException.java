package com.modeunsa.global.exception;

import com.modeunsa.global.status.ErrorStatus;
import lombok.Getter;

@Getter
public class GeneralException extends RuntimeException {

  private final ErrorStatus errorStatus;
  private final Object data;

  public GeneralException(String message) {
    super(message);
    this.errorStatus = ErrorStatus.INTERNAL_SERVER_ERROR;
    this.data = null;
  }

  public GeneralException(String message, Throwable cause) {
    super(message, cause);
    this.errorStatus = ErrorStatus.INTERNAL_SERVER_ERROR;
    this.data = null;
  }

  public GeneralException(Throwable cause) {
    super(cause.getMessage(), cause);
    this.errorStatus = ErrorStatus.INTERNAL_SERVER_ERROR;
    this.data = null;
  }

  public GeneralException(ErrorStatus errorStatus) {
    super(errorStatus.getMessage());
    this.errorStatus = errorStatus;
    this.data = null;
  }

  public GeneralException(ErrorStatus errorStatus, Object data) {
    super(errorStatus.getMessage());
    this.errorStatus = errorStatus;
    this.data = data;
  }
}
