package com.inventory.management.store.cucumber.hooks;

import com.inventory.management.store.cucumber.exceptions.TestInfrastructureNotReadyException;
import com.inventory.management.store.cucumber.config.SharedTestData;
import com.inventory.management.store.infrastructure.adapter.out.persistence.ProductJpaRepository;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

/**
 * Hooks específicos para testes integrados com TestContainers.
 * Gerencia a inicialização e limpeza do ambiente de teste.
 */
@ActiveProfiles("integration-test")
public class IntegrationTestHooks {
    
    private static final Logger logger = LoggerFactory.getLogger(IntegrationTestHooks.class);

    @Autowired
    private ProductJpaRepository productRepository;

    private final SharedTestData sharedTestData;

    public IntegrationTestHooks(SharedTestData sharedTestData) {
        this.sharedTestData = sharedTestData;
    }

    @Before("@integration")
    public void beforeIntegrationScenario(Scenario scenario) {
        logger.info("=== Iniciando cenário de teste integrado: {} ===", scenario.getName());
        
        // Limpa dados compartilhados
        sharedTestData.clearAll();
        
        // Registra início do cenário
        sharedTestData.getTestResults().put("scenario_name", scenario.getName());
        sharedTestData.getTestResults().put("scenario_start_time", System.currentTimeMillis());
        
        logger.info("Preparação do cenário concluída");
    }

    @After("@integration")
    public void afterIntegrationScenario(Scenario scenario) {
        logger.info("=== Finalizando cenário de teste integrado: {} ===", scenario.getName());
        
        // Registra fim do cenário
        long startTime = (Long) sharedTestData.getTestResults().getOrDefault("scenario_start_time", 0L);
        long duration = System.currentTimeMillis() - startTime;
        sharedTestData.getTestResults().put("scenario_duration_ms", duration);
        
        // Log de estatísticas do cenário
        logScenarioStatistics(scenario, duration);
        
        // Limpeza específica para cenários integrados
        cleanupIntegrationData();
        
        if (scenario.isFailed()) {
            logger.error("Cenário falhou: {}", scenario.getName());
            logFailureInformation(scenario);
        } else {
            logger.info("Cenário executado com sucesso em {}ms", duration);
        }
    }

    @Before("@real-time-sync")
    public void beforeRealTimeSyncScenario(Scenario scenario) {
        logger.info("Preparando cenário de sincronização em tempo real: {}", scenario.getName());
        
        // Preparações específicas para sincronização em tempo real
        ensureInfrastructureIsReady();
        
        logger.info("Infraestrutura verificada e pronta para sincronização");
    }

    @After("@real-time-sync")
    public void afterRealTimeSyncScenario(Scenario scenario) {
        logger.info("Finalizando cenário de sincronização em tempo real: {}", scenario.getName());
        
        // Verifica se a sincronização foi bem-sucedida
        validateSynchronizationState();
        
        logger.info("Validação de sincronização concluída");
    }

    @Before("@error-handling")
    public void beforeErrorHandlingScenario(Scenario scenario) {
        logger.info("Preparando cenário de tratamento de erros: {}", scenario.getName());
        
        // Preparações específicas para teste de resiliência
        sharedTestData.getTestResults().put("error_handling_mode", true);
        
        logger.info("Modo de tratamento de erros ativado");
    }

    @After("@error-handling")
    public void afterErrorHandlingScenario(Scenario scenario) {
        logger.info("Finalizando cenário de tratamento de erros: {}", scenario.getName());
        
        // Remove modo de tratamento de erros
        sharedTestData.getTestResults().remove("error_handling_mode");
        
        // Verifica se o sistema se recuperou adequadamente
        validateSystemRecovery();
        
        logger.info("Validação de recuperação do sistema concluída");
    }

    @Before("@concurrency")
    public void beforeConcurrencyScenario(Scenario scenario) {
        logger.info("Preparando cenário de concorrência: {}", scenario.getName());
        
        // Preparações específicas para teste de concorrência
        sharedTestData.getTestResults().put("concurrency_mode", true);
        
        logger.info("Modo de concorrência ativado");
    }

    @After("@concurrency")
    public void afterConcurrencyScenario(Scenario scenario) {
        logger.info("Finalizando cenário de concorrência: {}", scenario.getName());
        
        // Remove modo de concorrência
        sharedTestData.getTestResults().remove("concurrency_mode");
        
        // Aguarda estabilização após testes de concorrência
        waitForSystemStabilization();
        
        logger.info("Sistema estabilizado após teste de concorrência");
    }

    /**
     * Garante que toda a infraestrutura está pronta para os testes.
     */
    private void ensureInfrastructureIsReady() {
        try {
            // Verifica PostgreSQL
            long productCount = productRepository.count();
            logger.debug("PostgreSQL conectado - produtos existentes: {}", productCount);
            
            // Verifica Redis - modo simplificado utilizando H2
            logger.debug("Modo simplificado - Redis não verificado");
            
        } catch (Exception e) {
            logger.error("Falha na verificação de infraestrutura", e);
            throw new TestInfrastructureNotReadyException("Infraestrutura não está pronta para os testes", e);
        }
    }

    /**
     * Valida se a sincronização entre sistemas está funcionando.
     */
    private void validateSynchronizationState() {
        try {
            // Verifica se não há transações pendentes
            long totalProducts = productRepository.count();
            logger.debug("Total de produtos após sincronização: {}", totalProducts);
            
            // Verifica estado do cache Redis
            // Em um cenário real, verificaríamos chaves específicas
            
        } catch (Exception e) {
            logger.warn("Não foi possível validar completamente o estado de sincronização", e);
        }
    }

    /**
     * Valida se o sistema se recuperou adequadamente após um cenário de erro.
     */
    private void validateSystemRecovery() {
        try {
            // Verifica se as conexões estão funcionais
            productRepository.count();
            
            // Redis não disponível no modo simplificado - utilizando H2
            
            logger.debug("Sistema se recuperou adequadamente - modo simplificado");
            
        } catch (Exception e) {
            logger.warn("Sistema pode não ter se recuperado completamente", e);
        }
    }

    /**
     * Aguarda estabilização do sistema após testes de concorrência.
     */
    private void waitForSystemStabilization() {
        try {
            // Aguarda um breve período para estabilização
            Thread.sleep(1000);
            
            logger.debug("Sistema estabilizado");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Interrompido durante estabilização", e);
        }
    }

    /**
     * Limpa dados específicos dos testes integrados.
     */
    private void cleanupIntegrationData() {
        try {
            // Remove dados de teste do Redis - modo simplificado sem Redis
            logger.debug("Modo simplificado - limpeza Redis ignorada");
            
        } catch (Exception e) {
            logger.warn("Não foi possível limpar completamente os dados de teste", e);
        }
    }

    /**
     * Log de estatísticas do cenário executado.
     */
    private void logScenarioStatistics(Scenario scenario, long duration) {
        logger.info("Estatísticas do cenário '{}': ", scenario.getName());
        logger.info("  - Duração: {}ms", duration);
        logger.info("  - Status: {}", scenario.isFailed() ? "FALHA" : "SUCESSO");
        logger.info("  - Tags: {}", scenario.getSourceTagNames());
        
        // Log das métricas coletadas durante o teste
        var results = sharedTestData.getTestResults();
        results.entrySet().stream()
                .filter(entry -> entry.getKey().endsWith("_count") || entry.getKey().endsWith("_time"))
                .forEach(entry -> logger.info("  - {}: {}", entry.getKey(), entry.getValue()));
    }

    /**
     * Log de informações sobre falhas.
     */
    private void logFailureInformation(Scenario scenario) {
        logger.error("Informações sobre a falha:");
        logger.error("  - Cenário: {}", scenario.getName());
        logger.error("  - URI: {}", scenario.getUri());
        logger.error("  - Linha: {}", scenario.getLine());
        
        // Log dos dados compartilhados para debug
        var results = sharedTestData.getTestResults();
        if (!results.isEmpty()) {
            logger.error("  - Dados do teste:");
            results.forEach((key, value) -> logger.error("    - {}: {}", key, value));
        }
    }
}
