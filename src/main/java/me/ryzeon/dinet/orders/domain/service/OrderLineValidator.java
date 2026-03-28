package me.ryzeon.dinet.orders.domain.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import me.ryzeon.dinet.orders.domain.model.LineCatalogContext;
import me.ryzeon.dinet.orders.domain.model.OrderLine;
import me.ryzeon.dinet.orders.domain.validation.OrderLoadErrorCode;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 01:16</p>
 * <p>Pure domain rules for one order line.</p>
 * <p><strong>Order number format:</strong> the business requirement states {@code numeroPedido} must be
 * alphanumeric.
 */
public final class OrderLineValidator {

    private static final ZoneId DELIVERY_ZONE_ID = ZoneId.of("America/Lima");
    private static final Pattern ALPHANUMERIC_ORDER_NUMBER = Pattern.compile("^[A-Za-z0-9]+$");

    private final Clock clock;

    public OrderLineValidator(Clock clock) {
        this.clock = clock;
    }

    public List<OrderLoadErrorCode> validate(OrderLine line, LineCatalogContext catalog) {
        var errors = new ArrayList<OrderLoadErrorCode>();

        if (line.orderNumber() == null
                || line.orderNumber().isBlank()
                || !ALPHANUMERIC_ORDER_NUMBER.matcher(line.orderNumber().trim()).matches()) {
            errors.add(OrderLoadErrorCode.INVALID_ORDER_NUMBER);
        }

        if (line.customerId() == null || line.customerId().isBlank()) {
            errors.add(OrderLoadErrorCode.CUSTOMER_NOT_FOUND);
        } else if (!catalog.customerExists()) {
            errors.add(OrderLoadErrorCode.CUSTOMER_NOT_FOUND);
        }

        if (line.deliveryZoneId() == null || line.deliveryZoneId().isBlank()) {
            errors.add(OrderLoadErrorCode.INVALID_ZONE);
        } else if (catalog.zoneRefrigerationSupport().isEmpty()) {
            errors.add(OrderLoadErrorCode.INVALID_ZONE);
        }

        if (line.status() == null) {
            errors.add(OrderLoadErrorCode.INVALID_STATUS);
        }

        if (line.deliveryDate() == null) {
            errors.add(OrderLoadErrorCode.INVALID_DELIVERY_DATE);
        } else {
            var todayLima = LocalDate.now(clock.withZone(DELIVERY_ZONE_ID));
            if (line.deliveryDate().isBefore(todayLima)) {
                errors.add(OrderLoadErrorCode.INVALID_DELIVERY_DATE);
            }
        }

        catalog.zoneRefrigerationSupport()
                .ifPresent(supported -> {
                    if (line.requiresRefrigeration() && !supported) {
                        errors.add(OrderLoadErrorCode.COLD_CHAIN_NOT_SUPPORTED);
                    }
                });

        return List.copyOf(errors);
    }

    public boolean isDuplicateInFile(Set<String> seenOrderNumbers, String orderNumber) {
        if (orderNumber == null || orderNumber.isBlank()) {
            return false;
        }
        return seenOrderNumbers.contains(orderNumber.trim());
    }
}
