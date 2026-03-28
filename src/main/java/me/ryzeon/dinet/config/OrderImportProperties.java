package me.ryzeon.dinet.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 00:48</p>
 */
@ConfigurationProperties(prefix = "dinet.orders.import")
@Validated
public record OrderImportProperties(@Min(500) @Max(1000) int batchSize) {}
