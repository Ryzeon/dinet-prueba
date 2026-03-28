package me.ryzeon.dinet.orders.application.dto;

import java.util.List;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 01:28</p>
 */
public record OrderLoadSummary(
        long totalProcessed,
        long saved,
        long withErrors,
        List<LineLoadError> lineErrors,
        List<OrderLoadErrorGroup> errorsByType) {

    public OrderLoadSummary {
        lineErrors = List.copyOf(lineErrors);
        errorsByType = List.copyOf(errorsByType);
    }
}
