package me.ryzeon.dinet.orders.application.dto;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 15:05</p>
 */
public record OrderLoadWebResult(OrderLoadSummary summary, boolean idempotentReplay) {}
