package com.kranker.walletservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

  WALLET_NOT_FOUND("WALLET_001", "Wallet not found", HttpStatus.NOT_FOUND),
  INSUFFICIENT_FUNDS("WALLET_002", "Insufficient funds", HttpStatus.BAD_REQUEST),
  INVALID_AMOUNT("WALLET_003", "Invalid amount", HttpStatus.BAD_REQUEST),
  INVALID_OPERATION_TYPE("WALLET_004", "Invalid operation type", HttpStatus.BAD_REQUEST),
  OPTIMISTIC_LOCK("WALLET_005", "Concurrent modification, please retry", HttpStatus.CONFLICT),
  WALLET_ALREADY_EXISTS("WALLET_006", "Wallet already exists", HttpStatus.CONFLICT);

  private final String code;
  private final String defaultMessage;
  private final HttpStatus httpStatus;

  ErrorCode(String code, String defaultMessage, HttpStatus httpStatus) {
    this.code = code;
    this.defaultMessage = defaultMessage;
    this.httpStatus = httpStatus;
  }

  public String formatMessage(Object... args) {
    return String.format(defaultMessage, args);
  }
}