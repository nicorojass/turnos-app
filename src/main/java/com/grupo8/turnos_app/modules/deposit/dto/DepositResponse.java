package com.grupo8.turnos_app.modules.deposit.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.grupo8.turnos_app.common.enums.DepositStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositResponse {
    private Long id;
    private BigDecimal amount;
    private DepositStatus status;
    private LocalDateTime paidAt;
}