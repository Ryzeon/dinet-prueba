package me.ryzeon.dinet.orders.application.dto;

import me.ryzeon.dinet.orders.domain.validation.OrderLoadErrorCode;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 01:28</p>
 */
public record LineLoadError(int lineNumber, OrderLoadErrorCode code, String detail) {

    public LineLoadError {
        detail = detail == null ? "" : detail;
    }
}
