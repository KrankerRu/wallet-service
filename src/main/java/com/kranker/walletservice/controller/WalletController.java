package com.kranker.walletservice.controller;

import com.kranker.walletservice.dto.WalletRequest;
import com.kranker.walletservice.dto.WalletResponse;
import com.kranker.walletservice.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class WalletController {

  private final WalletService walletService;

  @PostMapping("/wallet")
  public ResponseEntity<Void> processTransaction(@Valid @RequestBody WalletRequest request) {
    walletService.processTransaction(request);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/wallets/{walletId}")
  public ResponseEntity<WalletResponse> getBalance(@PathVariable UUID walletId) {
    WalletResponse response = walletService.getBalance(walletId);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/wallets")
  public ResponseEntity<WalletResponse> createWallet() {
    var wallet = walletService.createWallet();
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(WalletResponse.builder()
            .walletId(wallet.getId())
            .balance(wallet.getBalance())
            .build());
  }
}