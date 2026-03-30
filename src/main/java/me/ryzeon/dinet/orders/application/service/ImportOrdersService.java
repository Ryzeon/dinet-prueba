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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 09:27</p>
 */
@Service
public class ImportOrdersService implements ImportOrdersUseCase {

    private static final Logger log = LoggerFactory.getLogger(ImportOrdersService.class);

    private static final int READER_BUFFER_CHARS = 16384;

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

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(csvContent, StandardCharsets.UTF_8), READER_BUFFER_CHARS)) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return buildSummary(0, 0, 0, lineErrors);
            }
            if (!headerLooksValid(headerLine)) {
                lineErrors.add(new LineLoadError(
                        1,
                        OrderLoadErrorCode.INVALID_ORDER_NUMBER,
                        "Cabecera CSV no es válida"));
                return buildSummary(0, 0, 1, lineErrors);
            }

            List<QueuedRow> batch = new ArrayList<>(batchSize);
            HashSet<String> pendingOrderNumbersInBatch = new HashSet<>();
            int batchIndex = 0;
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
                String orderKey = orderLine.orderNumber().trim();
                if (committedOrderNumbers.contains(orderKey) || pendingOrderNumbersInBatch.contains(orderKey)) {
                    lineErrors.add(new LineLoadError(
                            lineNumber,
                            OrderLoadErrorCode.DUPLICATE,
                            "Número de pedido duplicado en el archivo"));
                    continue;
                }

                batch.add(new QueuedRow(lineNumber, orderLine));
                pendingOrderNumbersInBatch.add(orderKey);

                if (batch.size() >= batchSize) {
                    batchIndex++;
                    saved += processBatch(batchIndex, batch, committedOrderNumbers, validator, lineErrors);
                    batch.clear();
                    pendingOrderNumbersInBatch.clear();
                }
            }

            if (!batch.isEmpty()) {
                batchIndex++;
                saved += processBatch(batchIndex, batch, committedOrderNumbers, validator, lineErrors);
            }
        } catch (Exception e) {
            lineErrors.add(new LineLoadError(
                    0,
                    OrderLoadErrorCode.INVALID_ORDER_NUMBER,
                    "Error al leer el CSV: " + e.getMessage()));
        }

        long withErrors = countDistinctErrorLines(lineErrors);
        return buildSummary(totalProcessed, saved, withErrors, lineErrors);
    }

    private static long countDistinctErrorLines(List<LineLoadError> lineErrors) {
        HashSet<Integer> lines = new HashSet<>();
        for (LineLoadError e : lineErrors) {
            lines.add(e.lineNumber());
        }
        return lines.size();
    }

    private long processBatch(
            int batchIndex,
            List<QueuedRow> batch,
            Set<String> committedOrderNumbers,
            OrderLineValidator validator,
            List<LineLoadError> lineErrors) {

        long startNanos = System.nanoTime();
        int queuedRows = batch.size();
        log.info(
                "import batch starting: batchIndex={}, queuedRows={}, configuredBatchSize={}",
                batchIndex,
                queuedRows,
                importProperties.batchSize());

        HashSet<String> customerIds = new HashSet<>();
        HashSet<String> zoneIds = new HashSet<>();

        // Se copian los ids de cliente y zona para consultar los catálogos en batch, evitando consultas repetidas por líneas
        for (QueuedRow q : batch) {
            customerIds.add(q.line().customerId());
            zoneIds.add(q.line().deliveryZoneId());
        }

        // catálogos en batch: clientes que existen y si la zona soporta frio
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

            // Se colectan los errores de validación de esta línea, si los hubiera
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
        long durationMs = (System.nanoTime() - startNanos) / 1_000_000L;
        int persisted = toSave.size();
        log.info(
                "import batch finished: batchIndex={}, queuedRows={}, persistedRows={}, durationMs={}",
                batchIndex,
                queuedRows,
                persisted,
                durationMs);

        return persisted;
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
