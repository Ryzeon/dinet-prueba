package me.ryzeon.dinet.orders.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import me.ryzeon.dinet.orders.domain.model.LineCatalogContext;
import me.ryzeon.dinet.orders.domain.model.OrderLine;
import me.ryzeon.dinet.orders.domain.model.OrderStatus;
import me.ryzeon.dinet.orders.domain.validation.OrderLoadErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 01:30</p>
 */
class OrderLineValidatorTest {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");

    private OrderLineValidator validator;

    @BeforeEach
    void setUp() {
        var fixed = ZonedDateTime.of(2026, 6, 15, 10, 0, 0, 0, LIMA).toInstant();
        Clock clock = Clock.fixed(fixed, LIMA);
        validator = new OrderLineValidator(clock);
    }

    @Test
    void validLine_noErrors() {
        var line = new OrderLine(
                "P001",
                "CLI-123",
                LocalDate.of(2026, 6, 20),
                OrderStatus.PENDIENTE,
                "ZONA1",
                false);
        var ctx = LineCatalogContext.of(true, true, true);

        assertThat(validator.validate(line, ctx)).isEmpty();
    }

    @Test
    void pastDeliveryDate_inLima_fails() {
        var line = new OrderLine(
                "P001",
                "CLI-123",
                LocalDate.of(2026, 6, 14),
                OrderStatus.PENDIENTE,
                "ZONA1",
                false);
        var ctx = LineCatalogContext.of(true, true, true);

        assertThat(validator.validate(line, ctx)).containsExactly(OrderLoadErrorCode.INVALID_DELIVERY_DATE);
    }

    @Test
    void deliveryDateToday_allowed() {
        var line = new OrderLine(
                "P001",
                "CLI-123",
                LocalDate.of(2026, 6, 15),
                OrderStatus.PENDIENTE,
                "ZONA1",
                false);
        var ctx = LineCatalogContext.of(true, true, true);

        assertThat(validator.validate(line, ctx)).isEmpty();
    }

    @Test
    void customerMissing() {
        var line = baseline();
        var ctx = LineCatalogContext.of(false, true, true);

        assertThat(validator.validate(line, ctx)).contains(OrderLoadErrorCode.CUSTOMER_NOT_FOUND);
    }

    @Test
    void zoneMissing() {
        var line = baseline();
        var ctx = LineCatalogContext.withoutZone(true);

        assertThat(validator.validate(line, ctx)).contains(OrderLoadErrorCode.INVALID_ZONE);
    }

    @Test
    void refrigerationNotSupportedByZone() {
        var line = new OrderLine(
                "P001",
                "CLI-123",
                LocalDate.of(2026, 6, 20),
                OrderStatus.PENDIENTE,
                "ZONA2",
                true);
        var ctx = LineCatalogContext.of(true, true, false);

        assertThat(validator.validate(line, ctx)).contains(OrderLoadErrorCode.COLD_CHAIN_NOT_SUPPORTED);
    }

    @Test
    void orderNumberWithHyphen_invalid() {
        var line = new OrderLine(
                "P-001",
                "CLI-123",
                LocalDate.of(2026, 6, 20),
                OrderStatus.PENDIENTE,
                "ZONA1",
                false);
        var ctx = LineCatalogContext.of(true, true, true);

        assertThat(validator.validate(line, ctx)).contains(OrderLoadErrorCode.INVALID_ORDER_NUMBER);
    }

    @Test
    void nullStatus_invalid() {
        var line = new OrderLine(
                "P001",
                "CLI-123",
                LocalDate.of(2026, 6, 20),
                null,
                "ZONA1",
                false);
        var ctx = LineCatalogContext.of(true, true, true);

        assertThat(validator.validate(line, ctx)).contains(OrderLoadErrorCode.INVALID_STATUS);
    }

    @Test
    void duplicateInFile_detected() {
        Set<String> seen = new HashSet<>(Set.of("P001"));
        assertThat(validator.isDuplicateInFile(seen, "P001")).isTrue();
        assertThat(validator.isDuplicateInFile(seen, "P002")).isFalse();
    }

    private static OrderLine baseline() {
        return new OrderLine(
                "P001",
                "CLI-123",
                LocalDate.of(2026, 6, 20),
                OrderStatus.PENDIENTE,
                "ZONA1",
                false);
    }
}
