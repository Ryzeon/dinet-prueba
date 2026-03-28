package me.ryzeon.dinet.orders.domain.validation;

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

    public String contractCode() {
        return contractCode;
    }
}
