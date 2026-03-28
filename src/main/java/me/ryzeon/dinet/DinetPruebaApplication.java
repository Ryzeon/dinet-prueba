package me.ryzeon.dinet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 00:48</p>
 */
@SpringBootApplication(scanBasePackages = "me.ryzeon.dinet")
@ConfigurationPropertiesScan
public class DinetPruebaApplication {

    public static void main(String[] args) {
        SpringApplication.run(DinetPruebaApplication.class, args);
    }
}
