package com.grupo8.turnos_app.modules.deposit.mapper;

import com.grupo8.turnos_app.modules.deposit.dto.DepositResponse;
import com.grupo8.turnos_app.modules.deposit.entity.Deposit;

public class DepositMapper {
  public static DepositResponse toResponse(Deposit deposit) {
    if (deposit == null)
      return null;
    return DepositResponse.builder()
        .id(deposit.getId())
        .amount(deposit.getAmount())
        .status(deposit.getStatus())
        .paidAt(deposit.getPaidAt())
        .build();
  }
}