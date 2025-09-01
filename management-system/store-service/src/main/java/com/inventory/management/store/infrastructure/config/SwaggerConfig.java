package com.inventory.management.store.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuração do Swagger/OpenAPI para documentação da API.
 * Define as informações da API e configurações de documentação.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
public class SwaggerConfig {
    
    @Value("${server.port:8080}")
    private String serverPort;
    
    /**
     * Configura a documentação OpenAPI.
     * 
     * @return configuração OpenAPI
     */
    @Bean
    public OpenAPI storeServiceOpenAPI() {
        Server server = new Server();
        server.setUrl("http://localhost:" + serverPort);
        server.setDescription("Store Service - Ambiente Local");
        
        Contact contact = new Contact();
        contact.setEmail("suporte@inventory.com");
        contact.setName("Equipe de Inventário");
        
        License license = new License()
                .name("MIT License")
                .url("https://choosealicense.com/licenses/mit/");
        
        Info info = new Info()
                .title("Store Service API")
                .version("1.0.0")
                .contact(contact)
                .description("API para gerenciamento de inventário de lojas. " +
                           "Oferece funcionalidades de busca, reserva, confirmação e cancelamento de produtos.")
                .license(license);
        
        return new OpenAPI()
                .info(info)
                .servers(List.of(server));
    }
}
