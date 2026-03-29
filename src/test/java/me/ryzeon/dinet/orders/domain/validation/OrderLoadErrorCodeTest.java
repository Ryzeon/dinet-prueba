package me.ryzeon.dinet.orders.domain.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 29/03/26 @ 00:12</p>
 */
class OrderLoadErrorCodeTest {

    @Test
    void contractCode_matchesEnunciado() {
        assertThat(OrderLoadErrorCode.CUSTOMER_NOT_FOUND.contractCode()).isEqualTo("CLIENTE_NO_ENCONTRADO");
        assertThat(OrderLoadErrorCode.INVALID_ZONE.contractCode()).isEqualTo("ZONA_INVALIDA");
    }

    @Test
    void fromContract_roundTrip() {
        for (OrderLoadErrorCode code : OrderLoadErrorCode.values()) {
            assertThat(OrderLoadErrorCode.fromContract(code.contractCode())).isSameAs(code);
        }
    }

    @Test
    void fromContract_blankOrUnknown_throws() {
        assertThatThrownBy(() -> OrderLoadErrorCode.fromContract(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OrderLoadErrorCode.fromContract(" "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OrderLoadErrorCode.fromContract("NO_EXISTE"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
