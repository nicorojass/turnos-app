package com.grupo8.turnos_app.common.enums;

public enum DepositStatus {
    PENDING,
    PAID,
    REFUNDED, // status for appt cancelation < 24hs
    FORFEITED // status for cancelation after 24hs
}