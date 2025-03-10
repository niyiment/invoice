package com.niyiment.invoice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;


@Configuration
public class ApplicationConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Invoice Management API")
                        .version("1.0")
                        .description("API documentation for managing invoices.")
                        .contact(new Contact()
                                .name("Support Team")
                                .email("info@example.com")
                                .url("https://example.com"))
                )
                .servers(List.of(
                        new Server().url("http://localhost:8000").description("Local Server")
                ));
    }

}
