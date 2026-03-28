package me.ryzeon.dinet.orders.application.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Clock;
import java.util.List;
import me.ryzeon.dinet.orders.application.dto.OrderLoadSummary;
import me.ryzeon.dinet.orders.application.dto.OrderLoadWebResult;
import me.ryzeon.dinet.orders.application.support.Sha256Hasher;
import me.ryzeon.dinet.orders.port.in.ImportOrdersUseCase;
import me.ryzeon.dinet.orders.port.out.LoadIdempotencyPort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 14:22</p>
 */
@Service
public class OrderLoadWebService {

    public static final String IDEMPOTENT_REPLAY_HEADER = "X-Idempotent-Replayed";

    private final ImportOrdersUseCase importOrders;
    private final LoadIdempotencyPort idempotency;

    public OrderLoadWebService(ImportOrdersUseCase importOrders, LoadIdempotencyPort idempotency) {
        this.importOrders = importOrders;
        this.idempotency = idempotency;
    }

    public OrderLoadWebResult loadFromMultipart(MultipartFile file, String idempotencyKey, Clock clock)
            throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Multipart field 'file' is required and must not be empty");
        }
        String key = idempotencyKey == null ? "" : idempotencyKey.trim();
        if (key.isEmpty()) {
            throw new IllegalArgumentException("Header Idempotency-Key must not be blank");
        }

        byte[] content = file.getBytes();
        String hash = Sha256Hasher.hexDigest(content);

        if (idempotency.wasAlreadyProcessed(key, hash)) {
            return new OrderLoadWebResult(idempotentPlaceholderSummary(), true);
        }

        try (var in = new ByteArrayInputStream(content)) {
            OrderLoadSummary summary = importOrders.loadFromCsv(in, clock);
            idempotency.markProcessed(key, hash);
            return new OrderLoadWebResult(summary, false);
        }
    }

    private static OrderLoadSummary idempotentPlaceholderSummary() {
        return new OrderLoadSummary(0, 0, 0, List.of(), List.of());
    }
}
