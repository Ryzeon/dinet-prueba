package me.ryzeon.dinet.orders.application.dto;

import java.util.List;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 01:28</p>
 */
public record OrderLoadSummary(
        long totalProcesados,
        long guardados,
        long conError,

        List<LineLoadError> erroresPorLinea,
        List<OrderLoadErrorGroup> erroresPorTipo
) {}
