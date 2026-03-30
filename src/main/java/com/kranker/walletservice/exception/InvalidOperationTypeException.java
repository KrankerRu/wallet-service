package com.kranker.walletservice.exception;

public class InvalidOperationTypeException extends WalletServiceException {

  public InvalidOperationTypeException(String operationType) {
    super(ErrorCode.INVALID_OPERATION_TYPE, operationType);
  }
}