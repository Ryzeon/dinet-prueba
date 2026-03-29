package me.ryzeon.dinet.orders.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.time.Clock;
import java.util.UUID;

import me.ryzeon.dinet.orders.application.dto.OrderLoadSummary;
import me.ryzeon.dinet.orders.application.service.OrderLoadWebService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 15:12</p>
 */
@Tag(name = "Pedidos")
@RestController
public class OrderLoadController {

    private final OrderLoadWebService orderLoadWebService;
    private final Clock clock;

    public OrderLoadController(OrderLoadWebService orderLoadWebService, Clock clock) {
        this.orderLoadWebService = orderLoadWebService;
        this.clock = clock;
    }

    @Operation(summary = "Cargar pedidos desde CSV (multipart, campo file)")
    @ApiResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = OrderLoadSummary.class)))
    @PostMapping(value = "/pedidos/cargar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OrderLoadSummary> cargar(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            HttpServletRequest request)
            throws IOException {

        var result = orderLoadWebService.loadFromMultipart(file, idempotencyKey, clock);
        Object cid = request.getAttribute(CorrelationIdFilter.REQUEST_ATTRIBUTE);
        String correlationId;
        if (cid instanceof String cidStr && !cidStr.isBlank()) {
            correlationId = cidStr;
        } else {
            correlationId = UUID.randomUUID().toString();
        }

        ResponseEntity.BodyBuilder builder = ResponseEntity.ok().header(CorrelationIdFilter.HEADER_NAME, correlationId);
        if (result.idempotentReplay()) {
            // Para darle a entender al front que es una replication de la misma request
            builder = builder.header(OrderLoadWebService.IDEMPOTENT_REPLAY_HEADER, "true");
        }
        return builder.body(result.summary());
    }
}
