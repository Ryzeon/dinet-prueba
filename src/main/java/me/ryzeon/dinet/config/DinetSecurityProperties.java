package me.ryzeon.dinet.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 16:45</p>
 */
@ConfigurationProperties(prefix = "dinet.security.jwt")
@Validated
public record DinetSecurityProperties(
        @NotBlank @Size(min = 32, message = "JWT must be min 32 long.") String secret) {
}
