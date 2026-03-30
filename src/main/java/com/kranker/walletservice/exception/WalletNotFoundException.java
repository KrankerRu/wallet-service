package com.kranker.walletservice.exception;

public class WalletNotFoundException extends WalletServiceException {

  public WalletNotFoundException(String walletId) {
    super(ErrorCode.WALLET_NOT_FOUND, walletId);
  }
}