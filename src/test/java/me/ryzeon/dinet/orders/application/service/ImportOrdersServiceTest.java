package me.ryzeon.dinet.orders.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import me.ryzeon.dinet.config.OrderImportProperties;
import me.ryzeon.dinet.orders.application.dto.OrderLoadSummary;
import me.ryzeon.dinet.orders.domain.model.OrderLine;
import me.ryzeon.dinet.orders.domain.validation.OrderLoadErrorCode;
import me.ryzeon.dinet.orders.port.out.CustomerCatalogPort;
import me.ryzeon.dinet.orders.port.out.OrderPersistencePort;
import me.ryzeon.dinet.orders.port.out.ZoneCatalogPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 09:40</p>
 */
@ExtendWith(MockitoExtension.class)
class ImportOrdersServiceTest {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");

    @Mock
    private OrderImportProperties importProperties;

    @Mock
    private CustomerCatalogPort customers;

    @Mock
    private ZoneCatalogPort zones;

    @Mock
    private OrderPersistencePort persistence;

    private ImportOrdersService service;

    @BeforeEach
    void setUp() {
        when(importProperties.batchSize()).thenReturn(500);
        service = new ImportOrdersService(importProperties, customers, zones, persistence);
    }

    @Test
    void validRows_catalogHits_saves() {
        when(customers.existingIdsAmong(any())).thenAnswer(inv -> Set.copyOf(inv.getArgument(0)));
        when(zones.refrigerationSupportFor(any()))
                .thenAnswer(inv -> {
                    Set<String> ids = inv.getArgument(0);
                    return ids.stream().collect(java.util.stream.Collectors.toMap(id -> id, id -> true));
                });

        String csv =
                """
                        numeroPedido,clienteId,fechaEntrega,estado,zonaEntrega,requiereRefrigeracion
                        P001,CLI-123,2026-08-10,PENDIENTE,ZONA1,false
                        P002,CLI-123,2026-08-11,CONFIRMADO,ZONA1,true
                        """;
        Clock clock = Clock.fixed(Instant.parse("2026-06-01T12:00:00Z"), LIMA);

        OrderLoadSummary summary = service.loadFromCsv(
                new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)), clock);

        assertThat(summary.totalProcesados()).isEqualTo(2);
        assertThat(summary.guardados()).isEqualTo(2);
        assertThat(summary.conError()).isEqualTo(0);
        assertThat(summary.erroresPorLinea()).isEmpty();

        ArgumentCaptor<List<OrderLine>> captor = ArgumentCaptor.forClass(List.class);
        verify(persistence).saveAllInBatch(captor.capture());
        assertThat(captor.getValue()).hasSize(2);
    }

    @Test
    void duplicateSecondRow_rejected() {
        when(customers.existingIdsAmong(any())).thenAnswer(inv -> Set.copyOf(inv.getArgument(0)));
        when(zones.refrigerationSupportFor(any())).thenReturn(Map.of("ZONA1", true));

        String csv =
                """
                        numeroPedido,clienteId,fechaEntrega,estado,zonaEntrega,requiereRefrigeracion
                        P001,CLI-123,2026-08-10,PENDIENTE,ZONA1,false
                        P001,CLI-123,2026-08-11,CONFIRMADO,ZONA1,false
                        """;
        Clock clock = Clock.fixed(Instant.parse("2026-06-01T12:00:00Z"), LIMA);

        OrderLoadSummary summary = service.loadFromCsv(
                new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)), clock);

        assertThat(summary.totalProcesados()).isEqualTo(2);
        assertThat(summary.guardados()).isEqualTo(1);
        assertThat(summary.erroresPorLinea())
                .anyMatch(e -> e.lineNumber() == 3 && e.code() == OrderLoadErrorCode.DUPLICATE);
    }

    @Test
    void stubCatalog_empty_noSaves() {
        when(customers.existingIdsAmong(any())).thenReturn(Set.of());
        when(zones.refrigerationSupportFor(any())).thenReturn(Map.of());

        String csv =
                """
                        numeroPedido,clienteId,fechaEntrega,estado,zonaEntrega,requiereRefrigeracion
                        P001,CLI-123,2026-08-10,PENDIENTE,ZONA1,false
                        """;
        Clock clock = Clock.fixed(Instant.parse("2026-06-01T12:00:00Z"), LIMA);

        OrderLoadSummary summary = service.loadFromCsv(
                new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)), clock);

        assertThat(summary.guardados()).isEqualTo(0);
        assertThat(summary.erroresPorLinea()).isNotEmpty();
    }

    @Test
    void invalidHeader_aborts() {
        String csv = "wrong,header\nP001,CLI-123,2026-08-10,PENDIENTE,ZONA1,false\n";
        Clock clock = Clock.fixed(Instant.parse("2026-06-01T12:00:00Z"), LIMA);

        OrderLoadSummary summary = service.loadFromCsv(
                new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)), clock);

        assertThat(summary.totalProcesados()).isZero();
        assertThat(summary.guardados()).isZero();
        assertThat(summary.erroresPorLinea()).hasSize(1);
    }

    @Test
    void batching_twoChunks_invokesCatalogTwice() {
        when(importProperties.batchSize()).thenReturn(1);
        when(customers.existingIdsAmong(any())).thenAnswer(inv -> Set.copyOf(inv.getArgument(0)));
        when(zones.refrigerationSupportFor(any()))
                .thenAnswer(inv -> {
                    Set<String> ids = inv.getArgument(0);
                    return ids.stream().collect(Collectors.toMap(id -> id, id -> true));
                });

        String csv =
                """
                        numeroPedido,clienteId,fechaEntrega,estado,zonaEntrega,requiereRefrigeracion
                        P001,CLI-123,2026-08-10,PENDIENTE,ZONA1,false
                        P002,CLI-123,2026-08-11,PENDIENTE,ZONA1,false
                        """;
        Clock clock = Clock.fixed(Instant.parse("2026-06-01T12:00:00Z"), LIMA);

        service.loadFromCsv(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)), clock);

        verify(customers, Mockito.times(2)).existingIdsAmong(any());
        verify(zones, Mockito.times(2)).refrigerationSupportFor(any());
    }
}
