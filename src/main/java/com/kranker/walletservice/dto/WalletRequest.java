package com.kranker.walletservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletRequest {

  @NotNull(message = "walletId cannot be null")
  @JsonProperty("walletId")
  private UUID walletId;

  @NotNull(message = "operationType cannot be null")
  @JsonProperty("operationType")
  private OperationType operationType;

  @NotNull(message = "amount cannot be null")
  @DecimalMin(value = "0.01", message = "amount must be greater than 0")
  @JsonProperty("amount")
  private BigDecimal amount;

  public enum OperationType {
    DEPOSIT, WITHDRAW
  }
}