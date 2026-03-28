package me.ryzeon.dinet.orders.adapter.in.web.error;

import java.util.List;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 13:29</p>
 */
public record ApiErrorResponse(String code, String message, List<ApiErrorDetail> details, String correlationId) {

    public ApiErrorResponse {
        // null safety
        code = code == null ? "" : code;
        message = message == null ? "" : message;
        details = details == null ? List.of() : List.copyOf(details);
        correlationId = correlationId == null ? "" : correlationId;
    }
}
