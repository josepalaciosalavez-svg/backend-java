package com.liverpool.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SwaggerConfig {

        @Value("${server.port:8080}")
        private String serverPort;

        @Value("${server.servlet.context-path:/api}")
        private String contextPath;

        @Value("${app.swagger.dev-url:}")
        private String devUrl;

        @Value("${app.swagger.dev-description:Desarrollo local}")
        private String devDescription;

        @Value("${app.swagger.prod-url:}")
        private String prodUrl;

        @Value("${app.swagger.prod-description:Producción}")
        private String prodDescription;

        @Bean
        public OpenAPI openAPI() {
                final String securitySchemeName = "bearerAuth";

                return new OpenAPI()
                                .info(apiInfo())
                                .servers(buildServers())
                                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                                .components(new Components()
                                                .addSecuritySchemes(securitySchemeName,
                                                                new SecurityScheme()
                                                                                .name(securitySchemeName)
                                                                                .type(SecurityScheme.Type.HTTP)
                                                                                .scheme("bearer")
                                                                                .bearerFormat("JWT")
                                                                                .description("Ingresa el token JWT con el prefijo Bearer. Ejemplo: Bearer eyJhbGciOi...")));
        }

        private List<Server> buildServers() {
                List<Server> servers = new ArrayList<>();

                // Servidor de desarrollo: usa la variable de entorno o construye el default con
                // puerto y contextPath
                String resolvedDevUrl = StringUtils.hasText(devUrl)
                                ? devUrl
                                : "http://localhost:" + serverPort + contextPath;

                servers.add(
                                new Server()
                                                .url(resolvedDevUrl)
                                                .description(devDescription));

                // Servidor de producción — solo aparece si API_BASE_URL está configurada
                if (StringUtils.hasText(prodUrl)) {
                        servers.add(
                                        new Server()
                                                        .url(prodUrl)
                                                        .description(prodDescription));
                }

                return servers;
        }

        private Info apiInfo() {
                return new Info()
                                .title("Liverpool Backend API")
                                .description("API REST para la gestión de clientes, datos de entrega y pedidos")
                                .version("1.0.0")
                                .contact(new Contact()
                                                .name("Equipo Liverpool")
                                                .email("dev@liverpool.com"))
                                .license(new License()
                                                .name("Uso interno")
                                                .url("https://liverpool.com"));
        }
}
