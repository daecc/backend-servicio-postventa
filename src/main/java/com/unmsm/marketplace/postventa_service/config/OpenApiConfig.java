package com.unmsm.marketplace.postventa_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Microservicio de Postventa - Marketplace")
                .version("1.0.0")
                .description("API para gestion de reclamos, tickets, saldo cliente y logistica inversa")
                .contact(new Contact()
                    .name("Grupo Ordenes")));
    }
}