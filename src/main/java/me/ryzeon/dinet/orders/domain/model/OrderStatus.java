package me.ryzeon.dinet.orders.domain.model;

import java.util.Arrays;
import java.util.Optional;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 01:07</p>
 */
public enum OrderStatus {
    PENDIENTE,
    CONFIRMADO,
    ENTREGADO;

    public static Optional<OrderStatus> fromText(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        var normalized = raw.trim().toUpperCase();
        return Arrays.stream(values()).filter(e -> e.name().equals(normalized)).findFirst();
    }
}
