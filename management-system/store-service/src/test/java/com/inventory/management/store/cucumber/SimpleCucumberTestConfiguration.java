package com.inventory.management.store.cucumber.simple;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Configuração simples para testes Cucumber sem TestContainers.
 * 
 * Esta configuração usa o banco em memória H2 para testes mais rápidos
 * sem dependências de containers externos.
 *
 * NOTA: A anotação @CucumberContextConfiguration foi removida para evitar conflitos.
 * Use SimpleCucumberTestRunner para testes com esta configuração.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class SimpleCucumberTestConfiguration {
    // Esta classe serve apenas para configurar o contexto Spring para os testes Cucumber
}
