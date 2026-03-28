package me.ryzeon.dinet.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "dinet.pedidos.carga")
@Validated
public record PedidosCargaProperties(
        @Min(500) @Max(1000) int batchSize
) {
}
