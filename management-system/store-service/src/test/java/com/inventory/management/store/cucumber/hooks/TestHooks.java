package com.inventory.management.store.cucumber.hooks;

import com.inventory.management.store.cucumber.config.SharedTestData;
import com.inventory.management.store.infrastructure.adapter.out.persistence.ProductJpaRepository;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Hooks para configuração e limpeza de testes Cucumber.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TestHooks {
    
    private static final Logger logger = LoggerFactory.getLogger(TestHooks.class);

    @Autowired
    private ProductJpaRepository productRepository;

    @Autowired
    private SharedTestData sharedTestData;

    @Before
    public void setUp() {
        logger.info("🧪 Iniciando cenário de teste Cucumber");
        // Limpa os dados do banco antes de cada cenário para evitar conflitos de chave única
        productRepository.deleteAll();
        // Limpa os dados compartilhados entre step definitions
        sharedTestData.reset();
        logger.info("🧹 Banco de dados limpo antes do cenário");
    }

    @After
    public void tearDown() {
        logger.info("🧹 Finalizando cenário de teste Cucumber");
        // Limpeza após cada cenário
        productRepository.deleteAll();
        // Limpa os dados compartilhados entre step definitions
        sharedTestData.reset();
        logger.info("🧹 Banco de dados limpo após o cenário");
    }

    @Before("@inventory")
    public void setUpInventoryTests() {
        logger.info("📦 Preparando teste de inventário");
        // Configurações específicas para testes de inventário
    }

    @Before("@reservas")
    public void setUpReservationTests() {
        logger.info("🔒 Preparando teste de reservas");
        // Configurações específicas para testes de reservas
    }

    @Before("@cancelamento-reservas")
    public void setUpCancellationTests() {
        logger.info("❌ Preparando teste de cancelamento");
        // Configurações específicas para testes de cancelamento
    }

    @After("@inventory")
    public void tearDownInventoryTests() {
        logger.info("📦 Limpeza após teste de inventário");
        // Limpeza específica para testes de inventário
    }

    @After("@reservas")
    public void tearDownReservationTests() {
        logger.info("🔒 Limpeza após teste de reservas");
        // Limpeza específica para testes de reservas
    }

    @After("@cancelamento-reservas")
    public void tearDownCancellationTests() {
        logger.info("❌ Limpeza após teste de cancelamento");
        // Limpeza específica para testes de cancelamento
    }
}
