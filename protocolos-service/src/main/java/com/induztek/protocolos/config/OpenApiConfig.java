package com.induztek.protocolos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI().info(new Info()
                .title("Microservicio de Protocolos")
                .description("Registro y formalización de protocolos de prueba eléctrica en terreno")
                .version("1.0.0"));
    }
}
