package com.inventory.management.store.cucumber;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.*;

/**
 * Runner específico para testes integrados com TestContainers.
 * 
 * Este runner utiliza a configuração RealContainersTestConfiguration para
 * subir containers reais durante a execução dos testes Cucumber.
 * 
 * Containers iniciados automaticamente:
 * - PostgreSQL (postgres:15-alpine) na porta dinâmica
 * - Redis (redis:7.2-alpine) na porta dinâmica  
 * - Kafka (confluentinc/cp-kafka:7.4.0) na porta dinâmica
 * 
 * Para executar:
 * mvn test -Dtest=RealContainersTestRunner
 * 
 * Ou via Maven com perfil específico:
 * mvn test -Dspring.profiles.active=integration-test -Dtest=RealContainersTestRunner
 * 
 * Tempo estimado de execução: 2-5 minutos
 * Memória requerida: ~2-4GB
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.inventory.management.store.cucumber")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, 
    value = "pretty," +
            "html:target/cucumber-reports/real-containers-html," +
            "json:target/cucumber-reports/real-containers-json/cucumber.json," +
            "junit:target/cucumber-reports/real-containers-junit.xml")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "not @ignore")
public class RealContainersTestRunner {
    
    // Esta classe é usada apenas como runner para os testes Cucumber
    // Os containers TestContainers são gerenciados pela RealContainersTestConfiguration
    
    private RealContainersTestRunner() {
        // Construtor privado para classe runner
    }
}
