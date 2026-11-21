package com.induztek.equipos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI().info(new Info()
                .title("Microservicio de Equipos")
                .description("Gestión de los equipos eléctricos de los clientes de Induztek")
                .version("1.0.0"));
    }
}
