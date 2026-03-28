package me.ryzeon.dinet.orders.application.csv;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import me.ryzeon.dinet.orders.domain.model.OrderLine;
import me.ryzeon.dinet.orders.domain.model.OrderStatus;
import me.ryzeon.dinet.orders.domain.validation.OrderLoadErrorCode;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 01:30</p>
 * <p>Parses one CSV data row into {@link OrderLine}. Expected columns:
 * {@code numeroPedido,clienteId,fechaEntrega,estado,zonaEntrega,requiereRefrigeracion}.</p>
 */
public final class CsvOrderLineParser {

    public static final int EXPECTED_COLUMNS = 6;

    private CsvOrderLineParser() {
    }

    public static ParsedCsvRow parseLine(int lineNumber, String line) {
        if (line == null || line.isBlank()) {
            return new ParsedCsvRow(
                    lineNumber, Optional.empty(), List.of(OrderLoadErrorCode.INVALID_ORDER_NUMBER));
        }

        var raw = line.split(",", -1);
        if (raw.length != EXPECTED_COLUMNS) {
            return new ParsedCsvRow(
                    lineNumber,
                    Optional.empty(),
                    List.of(OrderLoadErrorCode.INVALID_ORDER_NUMBER));
        }

        var cols = new String[EXPECTED_COLUMNS];

        for (int i = 0; i < EXPECTED_COLUMNS; i++) {
            cols[i] = raw[i].trim();
        }

        var errors = new ArrayList<OrderLoadErrorCode>();

        var orderNumber = cols[0];
        if (orderNumber.isEmpty()) {
            errors.add(OrderLoadErrorCode.INVALID_ORDER_NUMBER);
        }

        var customerId = cols[1];
        if (customerId.isEmpty()) {
            errors.add(OrderLoadErrorCode.CUSTOMER_NOT_FOUND);
        }

        LocalDate deliveryDate = null;
        if (cols[2].isEmpty()) {
            errors.add(OrderLoadErrorCode.INVALID_DELIVERY_DATE);
        } else {
            try {
                deliveryDate = LocalDate.parse(cols[2]);
            } catch (DateTimeParseException e) {
                errors.add(OrderLoadErrorCode.INVALID_DELIVERY_DATE);
            }
        }

        var status = OrderStatus.fromText(cols[3]);
        if (status.isEmpty()) {
            errors.add(OrderLoadErrorCode.INVALID_STATUS);
        }

        var zoneId = cols[4];
        if (zoneId.isEmpty()) {
            errors.add(OrderLoadErrorCode.INVALID_ZONE);
        }

        Boolean refrigeration = parseBoolean(cols[5]);
        if (refrigeration == null) {
            errors.add(OrderLoadErrorCode.INVALID_STATUS);
        }

        if (!errors.isEmpty()) {
            return new ParsedCsvRow(lineNumber, Optional.empty(), errors);
        }

        var orderLine = new OrderLine(
                orderNumber,
                customerId,
                deliveryDate,
                status.orElseThrow(),
                zoneId,
                refrigeration);

        return new ParsedCsvRow(lineNumber, Optional.of(orderLine), List.of());
    }

    private static Boolean parseBoolean(String raw) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        var v = raw.toLowerCase(Locale.ROOT);
        if ("true".equals(v)) {
            return true;
        }
        if ("false".equals(v)) {
            return false;
        }
        return null;
    }
}
