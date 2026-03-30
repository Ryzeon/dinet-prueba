package me.ryzeon.dinet.orders.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 01:28</p>
 */
public record OrderLoadErrorGroup(
        @JsonProperty("codigo") String contractCode, @JsonProperty("cantidad") long count) {

    public OrderLoadErrorGroup {
        if (count < 0) {
            throw new IllegalArgumentException("count must be >= 0");
        }
        contractCode = contractCode == null ? "" : contractCode;
    }
}
