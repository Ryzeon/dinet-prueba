package me.ryzeon.dinet.orders.application.csv;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import me.ryzeon.dinet.orders.domain.model.OrderStatus;
import me.ryzeon.dinet.orders.domain.validation.OrderLoadErrorCode;
import org.junit.jupiter.api.Test;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 01:30</p>
 */
class CsvOrderLineParserTest {

    @Test
    void validRow_parsesToOrderLine() {
        var row = CsvOrderLineParser.parseLine(
                2, "P001,CLI-123,2026-08-10,PENDIENTE,ZONA1,true");

        assertThat(row.success()).isTrue();
        var line = row.orderLine().orElseThrow();
        assertThat(line.orderNumber()).isEqualTo("P001");
        assertThat(line.customerId()).isEqualTo("CLI-123");
        assertThat(line.deliveryDate()).isEqualTo(LocalDate.of(2026, 8, 10));
        assertThat(line.status()).isEqualTo(OrderStatus.PENDIENTE);
        assertThat(line.deliveryZoneId()).isEqualTo("ZONA1");
        assertThat(line.requiresRefrigeration()).isTrue();
    }

    @Test
    void estadoCaseInsensitive() {
        var row = CsvOrderLineParser.parseLine(2, "P001,CLI-123,2026-08-10,confirmado,ZONA1,false");
        assertThat(row.success()).isTrue();
        assertThat(row.orderLine().orElseThrow().status()).isEqualTo(OrderStatus.CONFIRMADO);
    }

    @Test
    void wrongColumnCount_fails() {
        var row = CsvOrderLineParser.parseLine(3, "a,b,c");
        assertThat(row.success()).isFalse();
        assertThat(row.parseErrors()).containsExactly(OrderLoadErrorCode.INVALID_ORDER_NUMBER);
    }

    @Test
    void invalidBoolean_fails() {
        var row = CsvOrderLineParser.parseLine(2, "P001,CLI-123,2026-08-10,PENDIENTE,ZONA1,si");
        assertThat(row.success()).isFalse();
        assertThat(row.parseErrors()).contains(OrderLoadErrorCode.INVALID_STATUS);
    }

    @Test
    void invalidDate_fails() {
        var row = CsvOrderLineParser.parseLine(2, "P001,CLI-123,not-a-date,PENDIENTE,ZONA1,false");
        assertThat(row.success()).isFalse();
        assertThat(row.parseErrors()).contains(OrderLoadErrorCode.INVALID_DELIVERY_DATE);
    }
}
