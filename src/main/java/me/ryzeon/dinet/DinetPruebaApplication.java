package me.ryzeon.dinet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DinetPruebaApplication {

    public static void main(String[] args) {
        SpringApplication.run(DinetPruebaApplication.class, args);
    }

}
