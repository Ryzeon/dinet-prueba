package me.ryzeon.dinet.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 30/03/26 @ 00:01</p>
 */
@Configuration
public class SwaggerUiRedirectConfig implements WebMvcConfigurer {

    private static final String SWAGGER_UI_BASE = "/swagger-ui";

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController(SWAGGER_UI_BASE, SWAGGER_UI_BASE + "/index.html");
    }
}
