package com.kranker.walletservice.exception;

import java.math.BigDecimal;

public class InvalidAmountException extends WalletServiceException {

  public InvalidAmountException(BigDecimal amount) {
    super(ErrorCode.INVALID_AMOUNT, amount);
  }
}