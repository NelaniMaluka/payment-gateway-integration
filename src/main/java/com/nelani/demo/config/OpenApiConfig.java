package com.nelani.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Payment Gateway API")
                                                .version("v1")
                                                .description("""
                                                                API documentation for a demo Payment Gateway backend.

                                                                This service demonstrates:
                                                                - Multi-provider payment routing Stripe, PayPal, etc.)
                                                                - Asynchronous payment processing
                                                                - Webhook-based payment confirmation
                                                                - Clean architecture using Spring Boot
                                                                """)
                                                .contact(new Contact()
                                                                .name("Nelani Maluka")
                                                                .email("malukanelani@gmail.com"))
                                                .license(new License()
                                                                .name("MIT")
                                                                .url("https://opensource.org/licenses/MIT")))
                                .servers(List.of(
                                                new Server()
                                                                .url("http://localhost:8080")
                                                                .description("Local Development Server"),
                                                new Server()
                                                                .url("Not-Live-Yet")
                                                                .description("Production Server")))
                                .components(new Components());
        }
}
