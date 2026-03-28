package me.ryzeon.dinet.orders.application.dto;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 01:28</p>
 */
public record OrderLoadErrorGroup(String contractCode, long count) {

    public OrderLoadErrorGroup {
        if (count < 0) {
            throw new IllegalArgumentException("count must be >= 0");
        }
        contractCode = contractCode == null ? "" : contractCode;
    }
}
