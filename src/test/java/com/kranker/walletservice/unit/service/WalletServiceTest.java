package com.kranker.walletservice.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kranker.walletservice.dto.WalletRequest;
import com.kranker.walletservice.dto.WalletResponse;
import com.kranker.walletservice.entity.Wallet;
import com.kranker.walletservice.exception.InsufficientFundsException;
import com.kranker.walletservice.exception.InvalidAmountException;
import com.kranker.walletservice.exception.InvalidOperationTypeException;
import com.kranker.walletservice.exception.WalletNotFoundException;
import com.kranker.walletservice.repository.WalletRepository;
import com.kranker.walletservice.service.WalletService;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class WalletServiceTest {

  @Mock
  private WalletRepository walletRepository;

  @InjectMocks
  private WalletService walletService;

  private UUID walletId;
  private Wallet wallet;

  @BeforeEach
  void setUp() {
    walletId = UUID.randomUUID();
    wallet = new Wallet();
    wallet.setId(walletId);
    wallet.setBalance(BigDecimal.valueOf(100.00));
    wallet.setVersion(0L);
  }

  @Test
  void shouldDepositMoney() {
    WalletRequest request = new WalletRequest();
    request.setWalletId(walletId);
    request.setOperationType(WalletRequest.OperationType.DEPOSIT);
    request.setAmount(BigDecimal.valueOf(50.00));

    when(walletRepository.findByIdWithPessimisticLock(walletId)).thenReturn(Optional.of(wallet));
    when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

    walletService.processTransaction(request);

    assertThat(wallet.getBalance()).isEqualTo(BigDecimal.valueOf(150.00));
    verify(walletRepository).save(wallet);
  }

  @Test
  void shouldWithdrawMoney() {
    WalletRequest request = new WalletRequest();
    request.setWalletId(walletId);
    request.setOperationType(WalletRequest.OperationType.WITHDRAW);
    request.setAmount(BigDecimal.valueOf(30.00));

    when(walletRepository.findByIdWithPessimisticLock(walletId)).thenReturn(Optional.of(wallet));
    when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

    walletService.processTransaction(request);

    assertThat(wallet.getBalance()).isEqualTo(BigDecimal.valueOf(70.00));
    verify(walletRepository).save(wallet);
  }

  @Test
  void shouldThrowExceptionWhenWithdrawingMoreThanBalance() {
    WalletRequest request = new WalletRequest();
    request.setWalletId(walletId);
    request.setOperationType(WalletRequest.OperationType.WITHDRAW);
    request.setAmount(BigDecimal.valueOf(200.00));

    when(walletRepository.findByIdWithPessimisticLock(walletId)).thenReturn(Optional.of(wallet));

    InsufficientFundsException exception = assertThrows(
        InsufficientFundsException.class,
        () -> walletService.processTransaction(request)
    );

    assertThat(exception.getMessage()).contains("Insufficient funds");
    verify(walletRepository, never()).save(any());
  }

  @Test
  void shouldThrowExceptionWhenWalletNotFound() {
    WalletRequest request = new WalletRequest();
    request.setWalletId(walletId);
    request.setOperationType(WalletRequest.OperationType.DEPOSIT);
    request.setAmount(BigDecimal.valueOf(50.00));

    when(walletRepository.findByIdWithPessimisticLock(walletId)).thenReturn(Optional.empty());

    WalletNotFoundException exception = assertThrows(
        WalletNotFoundException.class,
        () -> walletService.processTransaction(request)
    );

    assertThat(exception.getMessage()).contains("Wallet not found");
    verify(walletRepository, never()).save(any());
  }

  @Test
  void shouldThrowExceptionWhenAmountIsNegative() {
    WalletRequest request = new WalletRequest();
    request.setWalletId(walletId);
    request.setOperationType(WalletRequest.OperationType.DEPOSIT);
    request.setAmount(BigDecimal.valueOf(-10.00));

    InvalidAmountException exception = assertThrows(
        InvalidAmountException.class,
        () -> walletService.processTransaction(request)
    );

    assertThat(exception.getMessage()).contains("Invalid amount");
    verify(walletRepository, never()).findByIdWithPessimisticLock(any());
    verify(walletRepository, never()).save(any());
  }

  @Test
  void shouldThrowExceptionWhenAmountIsZero() {
    WalletRequest request = new WalletRequest();
    request.setWalletId(walletId);
    request.setOperationType(WalletRequest.OperationType.DEPOSIT);
    request.setAmount(BigDecimal.ZERO);

    InvalidAmountException exception = assertThrows(
        InvalidAmountException.class,
        () -> walletService.processTransaction(request)
    );

    assertThat(exception.getMessage()).contains("Invalid amount");
    verify(walletRepository, never()).findByIdWithPessimisticLock(any());
    verify(walletRepository, never()).save(any());
  }

  @Test
  void shouldThrowExceptionForInvalidOperationType() {
    WalletRequest request = new WalletRequest();
    request.setWalletId(walletId);
    request.setOperationType(null);
    request.setAmount(BigDecimal.valueOf(50.00));

    when(walletRepository.findByIdWithPessimisticLock(walletId)).thenReturn(Optional.of(wallet));

    InvalidOperationTypeException exception = assertThrows(
        InvalidOperationTypeException.class,
        () -> walletService.processTransaction(request)
    );

    assertThat(exception.getMessage()).contains("Invalid operation type");
    verify(walletRepository, never()).save(any());
  }

  @Test
  void shouldGetBalance() {
    when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

    WalletResponse response = walletService.getBalance(walletId);

    assertThat(response.getWalletId()).isEqualTo(walletId);
    assertThat(response.getBalance()).isEqualTo(BigDecimal.valueOf(100.00));
  }

  @Test
  void shouldThrowExceptionWhenGettingBalanceOfNonExistentWallet() {
    when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

    WalletNotFoundException exception = assertThrows(
        WalletNotFoundException.class,
        () -> walletService.getBalance(walletId)
    );

    assertThat(exception.getMessage()).contains("Wallet not found");
  }

}