package com.kranker.walletservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class WalletServiceException extends RuntimeException {

  private final ErrorCode errorCode;
  private final Object[] messageArgs;

  protected WalletServiceException(ErrorCode errorCode, Object... messageArgs) {
    super(errorCode.formatMessage(messageArgs));
    this.errorCode = errorCode;
    this.messageArgs = messageArgs;
  }

  protected WalletServiceException(ErrorCode errorCode, Throwable cause, Object... messageArgs) {
    super(errorCode.formatMessage(messageArgs), cause);
    this.errorCode = errorCode;
    this.messageArgs = messageArgs;
  }

  public String getErrorCode() {
    return errorCode.getCode();
  }

  public HttpStatus getHttpStatus() {
    return errorCode.getHttpStatus();
  }
}