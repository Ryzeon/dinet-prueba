package me.ryzeon.dinet.orders.application.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.*;

import me.ryzeon.dinet.config.OrderImportProperties;
import me.ryzeon.dinet.orders.application.csv.CsvOrderLineParser;
import me.ryzeon.dinet.orders.application.dto.LineLoadError;
import me.ryzeon.dinet.orders.application.dto.OrderLoadErrorGroup;
import me.ryzeon.dinet.orders.application.dto.OrderLoadSummary;
import me.ryzeon.dinet.orders.domain.model.LineCatalogContext;
import me.ryzeon.dinet.orders.domain.model.OrderLine;
import me.ryzeon.dinet.orders.domain.service.OrderLineValidator;
import me.ryzeon.dinet.orders.domain.validation.OrderLoadErrorCode;
import me.ryzeon.dinet.orders.port.in.ImportOrdersUseCase;
import me.ryzeon.dinet.orders.port.out.CustomerCatalogPort;
import me.ryzeon.dinet.orders.port.out.OrderPersistencePort;
import me.ryzeon.dinet.orders.port.out.ZoneCatalogPort;
import org.springframework.stereotype.Service;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 09:27</p>
 */
@Service
public class ImportOrdersService implements ImportOrdersUseCase {

    private static final String EXPECTED_HEADER =
            "numeropedido,clienteid,fechaentrega,estado,zonaentrega,requiererefrigeracion";

    private final OrderImportProperties importProperties;
    private final CustomerCatalogPort customers;
    private final ZoneCatalogPort zones;
    private final OrderPersistencePort persistence;

    public ImportOrdersService(
            OrderImportProperties importProperties,
            CustomerCatalogPort customers,
            ZoneCatalogPort zones,
            OrderPersistencePort persistence) {
        this.importProperties = importProperties;
        this.customers = customers;
        this.zones = zones;
        this.persistence = persistence;
    }

    @Override
    public OrderLoadSummary loadFromCsv(InputStream csvContent, Clock clock) {
        List<LineLoadError> lineErrors = new ArrayList<>();
        HashSet<String> committedOrderNumbers = new HashSet<>();
        long totalProcessed = 0;
        long saved = 0;

        OrderLineValidator validator = new OrderLineValidator(clock);
        int batchSize = importProperties.batchSize();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(csvContent, StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return buildSummary(0, 0, 0, lineErrors);
            }
            if (!headerLooksValid(headerLine)) {
                lineErrors.add(new LineLoadError(
                        1,
                        OrderLoadErrorCode.INVALID_ORDER_NUMBER,
                        "Cabecera CSV no es valida"));
                return buildSummary(0, 0, 1, lineErrors);
            }

            List<QueuedRow> batch = new ArrayList<>();
            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                totalProcessed++;

                var parsed = CsvOrderLineParser.parseLine(lineNumber, line);
                if (!parsed.success()) {
                    for (var code : parsed.parseErrors()) {
                        lineErrors.add(new LineLoadError(lineNumber, code, ""));
                    }
                    continue;
                }

                OrderLine orderLine = parsed.orderLine().orElseThrow();
                if (isDuplicateOrderNumber(committedOrderNumbers, batch, orderLine.orderNumber())) {
                    lineErrors.add(new LineLoadError(
                            lineNumber, OrderLoadErrorCode.DUPLICATE, "numeroPedido repetido en el archivo"));
                    continue;
                }

                batch.add(new QueuedRow(lineNumber, orderLine));

                if (batch.size() >= batchSize) {
                    saved += processBatch(batch, committedOrderNumbers, validator, lineErrors);
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                saved += processBatch(batch, committedOrderNumbers, validator, lineErrors);
            }
        } catch (Exception e) {
            lineErrors.add(new LineLoadError(
                    0,
                    OrderLoadErrorCode.INVALID_ORDER_NUMBER,
                    "Error leyendo CSV: " + e.getMessage()));
        }

        long withErrors =
                lineErrors.stream().mapToLong(LineLoadError::lineNumber).distinct().count();
        return buildSummary(totalProcessed, saved, withErrors, lineErrors);
    }

    private static boolean isDuplicateOrderNumber(Set<String> committed, List<QueuedRow> currentBatch, String orderNumber) {
        String cleanOrderNumber = orderNumber.trim();
        if (committed.contains(cleanOrderNumber)) {
            return true;
        }
        for (QueuedRow q : currentBatch) {
            if (q.line().orderNumber().trim().equals(cleanOrderNumber)) {
                return true;
            }
        }
        return false;
    }

    private long processBatch(
            List<QueuedRow> batch,
            Set<String> committedOrderNumbers,
            OrderLineValidator validator,
            List<LineLoadError> lineErrors) {

        HashSet<String> customerIds = new HashSet<>();
        HashSet<String> zoneIds = new HashSet<>();

        // Se copian los ids de cliente y zona para consultar los catálogos en batch, evitando consultas repetidas por líneas
        for (QueuedRow q : batch) {
            customerIds.add(q.line().customerId());
            zoneIds.add(q.line().deliveryZoneId());
        }

        // Se consultan los catálogos en batch para validar existencia de clientes y soporte de refrigeración por zonaq
        Set<String> existingCustomers = customers.existingIdsAmong(customerIds);
        Map<String, Boolean> zoneRefrigeration = zones.refrigerationSupportFor(zoneIds);

        List<OrderLine> toSave = new ArrayList<>();

        for (QueuedRow q : batch) {
            OrderLine orderLine = q.line();
            // Se validan las reglas de negocio
            boolean customerOk = existingCustomers.contains(orderLine.customerId());
            boolean zoneOk = zoneRefrigeration.containsKey(orderLine.deliveryZoneId());
            boolean refrigerationSupported = zoneOk && zoneRefrigeration.get(orderLine.deliveryZoneId());

            // Se construye el contexto de catálogo para esta línea, con la información ya consultada en batch
            LineCatalogContext catalogContext = LineCatalogContext.of(customerOk, zoneOk, refrigerationSupported);

            // Se colectan los errores de validación de esta línea, si los hubieraq
            List<OrderLoadErrorCode> errorCodes = validator.validate(orderLine, catalogContext);
            if (!errorCodes.isEmpty()) {
                for (OrderLoadErrorCode code : errorCodes) {
                    lineErrors.add(new LineLoadError(q.lineNumber(), code, ""));
                }
                continue;
            }

            committedOrderNumbers.add(orderLine.orderNumber().trim());
            toSave.add(orderLine);
        }

        if (!toSave.isEmpty()) {
            persistence.saveAllInBatch(toSave);
        }
        return toSave.size();
    }

    private static boolean headerLooksValid(String headerLine) {
        if (headerLine == null) {
            return false;
        }
        var normalized = headerLine.trim().toLowerCase().replace(" ", "");
        return normalized.equals(EXPECTED_HEADER);
    }

    private static OrderLoadSummary buildSummary(
            long totalProcessed, long saved, long withErrors, List<LineLoadError> lineErrors) {
        EnumMap<OrderLoadErrorCode, Long> counts = new EnumMap<>(OrderLoadErrorCode.class);
        for (LineLoadError err : lineErrors) {
            counts.merge(err.code(), 1L, Long::sum);
        }
        var groups = counts.entrySet().stream()
                .map(e -> new OrderLoadErrorGroup(e.getKey().contractCode(), e.getValue()))
                .sorted(Comparator.comparing(OrderLoadErrorGroup::contractCode))
                .toList();

        return new OrderLoadSummary(totalProcessed, saved, withErrors, List.copyOf(lineErrors), groups);
    }

    private record QueuedRow(int lineNumber, OrderLine line) {
    }
}
