package me.ryzeon.dinet.orders.port.out;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 13:31</p>
 * <p>Outbound port for {@code cargas_idempotencias}
 */
public interface LoadIdempotencyPort {

    boolean wasAlreadyProcessed(String idempotencyKey, String fileSha256Hex);

    void markProcessed(String idempotencyKey, String fileSha256Hex);
}
