package me.ryzeon.dinet.orders.domain.model;

import java.time.LocalDate;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 01:05</p>
 */
public record OrderLine(
        String orderNumber,
        String customerId,
        LocalDate deliveryDate,
        OrderStatus status,
        String deliveryZoneId,
        boolean requiresRefrigeration) {}
