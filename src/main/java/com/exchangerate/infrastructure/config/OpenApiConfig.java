package com.exchangerate.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI exchangeRateOpenAPI() {
        Server localServer = new Server()
                .url("http://localhost:" + serverPort)
                .description("Local development environment");

        Contact contact = new Contact()
                .name("Exchange Rate API Team")
                .email("api-support@exchangerate.com")
                .url("https://github.com/exchangerate/api");

        License license = new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");

        Info info = new Info()
                .title("Exchange Rate API")
                .version("1.0.0")
                .contact(contact)
                .description("RESTful API providing real-time exchange rate queries and currency conversion services. " +
                           "Supports exchange rate queries for multiple currency pairs, real-time conversion calculations, and exchange rate historical data management.")
                .termsOfService("https://exchangerate.com/terms")
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer));
    }
}