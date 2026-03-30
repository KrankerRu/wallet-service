package com.kranker.walletservice.concurrency;

import com.kranker.walletservice.dto.WalletRequest;
import com.kranker.walletservice.entity.Wallet;
import com.kranker.walletservice.repository.WalletRepository;
import com.kranker.walletservice.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ConcurrentWalletTest {

  @Autowired
  private WalletService walletService;

  @Autowired
  private WalletRepository walletRepository;

  private UUID walletId;
  private static final int THREAD_COUNT = 20;
  private static final int OPERATIONS_PER_THREAD = 10;

  @BeforeEach
  void setUp() {
    walletRepository.deleteAll();
    Wallet wallet = new Wallet();
    wallet.setBalance(BigDecimal.ZERO);
    wallet = walletRepository.save(wallet);
    walletId = wallet.getId();
  }

  @Test
  void shouldHandleConcurrentDeposits() throws InterruptedException {
    ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
    AtomicInteger successCount = new AtomicInteger(0);

    BigDecimal depositAmount = BigDecimal.valueOf(10.00);
    int expectedTotal = THREAD_COUNT * OPERATIONS_PER_THREAD;
    BigDecimal expectedBalance = BigDecimal.valueOf(expectedTotal * 10.00);

    for (int i = 0; i < THREAD_COUNT; i++) {
      executor.submit(() -> {
        for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
          WalletRequest request = new WalletRequest();
          request.setWalletId(walletId);
          request.setOperationType(WalletRequest.OperationType.DEPOSIT);
          request.setAmount(depositAmount);
          walletService.processTransaction(request);
          successCount.incrementAndGet();
        }
      });
    }

    executor.shutdown();
    boolean terminated = executor.awaitTermination(30, TimeUnit.SECONDS);

    assertThat(terminated)
        .withFailMessage("Timeout waiting for tasks to complete")
        .isTrue();

    Wallet finalWallet = walletRepository.findById(walletId).orElseThrow();

    assertThat(finalWallet.getBalance().compareTo(expectedBalance))
        .withFailMessage("Expected balance %s but got %s", expectedBalance, finalWallet.getBalance())
        .isEqualTo(0);
    assertThat(successCount.get()).isEqualTo(expectedTotal);
  }
}