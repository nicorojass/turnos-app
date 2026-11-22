package com.grupo8.turnos_app.common.enums;

public enum DepositStatus {
    PENDING,
    PAID,
    REFUNDED,  // early cancellation (>= 24hs): deposit returned to client
    FORFEITED, // late cancellation (< 24hs): deposit kept by business
    CANCELED   // booking canceled before payment was made
}