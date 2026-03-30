package com.kranker.walletservice.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends WalletServiceException {

  public InsufficientFundsException(String walletId, BigDecimal balance, BigDecimal requestedAmount) {
    super(ErrorCode.INSUFFICIENT_FUNDS, walletId, balance, requestedAmount);
  }
}