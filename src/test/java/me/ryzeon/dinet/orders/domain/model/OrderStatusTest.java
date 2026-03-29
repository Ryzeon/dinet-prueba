package me.ryzeon.dinet.orders.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 01:28</p>
 */
class OrderStatusTest {

    @Test
    void fromText_isCaseInsensitive() {
        assertThat(OrderStatus.fromText("pendiente")).contains(OrderStatus.PENDIENTE);
        assertThat(OrderStatus.fromText("CONFIRMADO")).contains(OrderStatus.CONFIRMADO);
        assertThat(OrderStatus.fromText("Entregado")).contains(OrderStatus.ENTREGADO);
    }

    @Test
    void fromText_unknown_isEmpty() {
        assertThat(OrderStatus.fromText("X")).isEmpty();
        assertThat(OrderStatus.fromText("")).isEmpty();
        assertThat(OrderStatus.fromText(null)).isEmpty();
    }
}
