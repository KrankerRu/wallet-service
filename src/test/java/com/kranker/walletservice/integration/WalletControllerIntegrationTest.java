package com.kranker.walletservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kranker.walletservice.dto.WalletRequest;
import com.kranker.walletservice.entity.Wallet;
import com.kranker.walletservice.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WalletControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private WalletRepository walletRepository;

  private UUID testWalletId;

  @BeforeEach
  void setUp() {
    walletRepository.deleteAll();

    Wallet wallet = new Wallet();
    wallet.setBalance(BigDecimal.valueOf(100.00));
    wallet = walletRepository.save(wallet);
    testWalletId = wallet.getId();
  }

  @Test
  void shouldDepositMoney() throws Exception {
    WalletRequest request = new WalletRequest();
    request.setWalletId(testWalletId);
    request.setOperationType(WalletRequest.OperationType.DEPOSIT);
    request.setAmount(BigDecimal.valueOf(50.00));

    mockMvc.perform(post("/api/v1/wallet")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    Wallet updatedWallet = walletRepository.findById(testWalletId).orElseThrow();
    assertThat(updatedWallet.getBalance().compareTo(BigDecimal.valueOf(150.00))).isEqualTo(0);
  }

  @Test
  void shouldWithdrawMoney() throws Exception {
    WalletRequest request = new WalletRequest();
    request.setWalletId(testWalletId);
    request.setOperationType(WalletRequest.OperationType.WITHDRAW);
    request.setAmount(BigDecimal.valueOf(30.00));

    mockMvc.perform(post("/api/v1/wallet")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    Wallet updatedWallet = walletRepository.findById(testWalletId).orElseThrow();
    assertThat(updatedWallet.getBalance().compareTo(BigDecimal.valueOf(70.00))).isEqualTo(0);
  }

  @Test
  void shouldReturn400WhenWithdrawingMoreThanBalance() throws Exception {
    WalletRequest request = new WalletRequest();
    request.setWalletId(testWalletId);
    request.setOperationType(WalletRequest.OperationType.WITHDRAW);
    request.setAmount(BigDecimal.valueOf(200.00));

    mockMvc.perform(post("/api/v1/wallet")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Insufficient funds"));
  }

  @Test
  void shouldReturn404WhenWalletNotFound() throws Exception {
    WalletRequest request = new WalletRequest();
    request.setWalletId(UUID.randomUUID());
    request.setOperationType(WalletRequest.OperationType.DEPOSIT);
    request.setAmount(BigDecimal.valueOf(50.00));

    mockMvc.perform(post("/api/v1/wallet")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Wallet not found"));
  }

  @Test
  void shouldReturn400ForInvalidJson() throws Exception {
    String invalidJson = "{\"walletId\": \"not-a-uuid\", \"operationType\": \"DEPOSIT\"}";

    mockMvc.perform(post("/api/v1/wallet")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidJson))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturnBalance() throws Exception {
    mockMvc.perform(get("/api/v1/wallets/{walletId}", testWalletId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.walletId").value(testWalletId.toString()))
        .andExpect(jsonPath("$.balance").value(100.00));
  }

  @Test
  void shouldReturn404ForNonExistentWallet() throws Exception {
    UUID nonExistentId = UUID.randomUUID();

    mockMvc.perform(get("/api/v1/wallets/{walletId}", nonExistentId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Wallet not found"));
  }

  @Test
  void shouldReturn400ForNegativeAmount() throws Exception {
    WalletRequest request = new WalletRequest();
    request.setWalletId(testWalletId);
    request.setOperationType(WalletRequest.OperationType.DEPOSIT);
    request.setAmount(BigDecimal.valueOf(-10.00));

    mockMvc.perform(post("/api/v1/wallet")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("amount: amount must be greater than 0"));
  }

  @Test
  void shouldReturn400ForZeroAmount() throws Exception {
    WalletRequest request = new WalletRequest();
    request.setWalletId(testWalletId);
    request.setOperationType(WalletRequest.OperationType.DEPOSIT);
    request.setAmount(BigDecimal.ZERO);

    mockMvc.perform(post("/api/v1/wallet")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }
}