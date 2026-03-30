package com.kranker.walletservice.service;

import com.kranker.walletservice.dto.WalletRequest;
import com.kranker.walletservice.dto.WalletResponse;
import com.kranker.walletservice.entity.Wallet;
import com.kranker.walletservice.exception.InsufficientFundsException;
import com.kranker.walletservice.exception.InvalidAmountException;
import com.kranker.walletservice.exception.InvalidOperationTypeException;
import com.kranker.walletservice.exception.WalletNotFoundException;
import com.kranker.walletservice.repository.WalletRepository;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

  private final WalletRepository walletRepository;

  @Transactional
  @Retryable(
      retryFor = {OptimisticLockingFailureException.class},
      backoff = @Backoff(delay = 100, multiplier = 2)
  )
  public void processTransaction(WalletRequest request) {
    log.debug("Processing transaction for wallet: {}, type: {}, amount: {}",
        request.getWalletId(), request.getOperationType(), request.getAmount());

    if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
      throw new InvalidAmountException(request.getAmount());
    }

    Wallet wallet = walletRepository.findByIdWithPessimisticLock(request.getWalletId())
        .orElseThrow(() -> new WalletNotFoundException(request.getWalletId().toString()));

    BigDecimal currentBalance = wallet.getBalance();
    BigDecimal newBalance;

    if (request.getOperationType() == WalletRequest.OperationType.DEPOSIT) {
      newBalance = currentBalance.add(request.getAmount());
      wallet.setBalance(newBalance);

    } else if (request.getOperationType() == WalletRequest.OperationType.WITHDRAW) {
      if (currentBalance.compareTo(request.getAmount()) < 0) {
        throw new InsufficientFundsException(
            wallet.getId().toString(), currentBalance, request.getAmount()
        );
      }
      newBalance = currentBalance.subtract(request.getAmount());
      wallet.setBalance(newBalance);

    } else {
      throw new InvalidOperationTypeException(
          request.getOperationType() != null ? request.getOperationType().toString() : "null"
      );
    }
      walletRepository.save(wallet);
  }

  @Transactional(readOnly = true)
  public WalletResponse getBalance(UUID walletId) {
    log.debug("Getting balance for wallet: {}", walletId);

    Wallet wallet = walletRepository.findById(walletId)
        .orElseThrow(() -> new WalletNotFoundException(walletId.toString()));

    return WalletResponse.builder()
        .walletId(wallet.getId())
        .balance(wallet.getBalance())
        .build();
  }

  @Transactional
  public Wallet createWallet() {
    log.debug("Creating new wallet");
    Wallet wallet = new Wallet();
    wallet.setBalance(BigDecimal.ZERO);
    return walletRepository.save(wallet);
  }
}