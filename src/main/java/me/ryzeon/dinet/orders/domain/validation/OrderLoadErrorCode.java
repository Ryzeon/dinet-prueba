package me.ryzeon.dinet.orders.domain.validation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 01:12</p>
 */
public enum OrderLoadErrorCode {

    INVALID_ORDER_NUMBER("NUMERO_PEDIDO_INVALIDO"),
    CUSTOMER_NOT_FOUND("CLIENTE_NO_ENCONTRADO"),
    INVALID_ZONE("ZONA_INVALIDA"),
    INVALID_DELIVERY_DATE("FECHA_INVALIDA"),
    INVALID_STATUS("ESTADO_INVALIDO"),
    DUPLICATE("DUPLICADO"),
    COLD_CHAIN_NOT_SUPPORTED("CADENA_FRIO_NO_SOPORTADA");

    private final String contractCode;

    OrderLoadErrorCode(String contractCode) {
        this.contractCode = contractCode;
    }

    @JsonValue
    public String contractCode() {
        return contractCode;
    }

    @JsonCreator
    public static OrderLoadErrorCode fromContract(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Order load error code is required");
        }
        for (OrderLoadErrorCode code : values()) {
            if (code.contractCode.equals(value)) {
                return code;
            }
        }
        throw new IllegalArgumentException("Unknown order load error code: " + value);
    }
}
