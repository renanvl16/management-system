package com.inventory.management.store.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Configuração principal para testes Cucumber com Spring Boot.
 * Esta classe configura o contexto Spring para os testes BDD.
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CucumberSpringConfiguration {
    // Esta classe é usada apenas para configuração
    // Todos os beans e configurações do Spring Boot serão carregados automaticamente
}
