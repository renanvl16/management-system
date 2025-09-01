package com.inventory.management.store.cucumber;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.*;

/**
 * Runner específico para testes integrados com TestContainers.
 * 
 * Este runner executa os testes Cucumber com uma configuração completa de infraestrutura:
 * - PostgreSQL via TestContainers
 * - Redis via TestContainers  
 * - Kafka via TestContainers
 * 
 * Para executar apenas os testes integrados, use:
 * mvn test -Dtest=IntegrationTestRunner
 * 
 * Ou execute esta classe diretamente na IDE.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/integration")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.inventory.management.store.cucumber.stepdefinitions,com.inventory.management.store.cucumber.hooks,com.inventory.management.store.cucumber")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty,html:target/cucumber-reports/integration-html,json:target/cucumber-reports/integration-json/cucumber.json")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@integration")
public class IntegrationTestRunner {
    // Esta classe é usada apenas como runner para os testes integrados com TestContainers
}
