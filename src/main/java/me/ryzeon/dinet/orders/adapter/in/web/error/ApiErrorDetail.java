package me.ryzeon.dinet.orders.adapter.in.web.error;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 13:30</p>
 */
public record ApiErrorDetail(String field, String message) {

    public ApiErrorDetail {
        // null safety
        field = field == null ? "" : field;
        message = message == null ? "" : message;
    }
}
