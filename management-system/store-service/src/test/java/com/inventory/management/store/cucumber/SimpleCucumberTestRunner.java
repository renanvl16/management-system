package com.inventory.management.store.cucumber;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.*;

/**
 * Runner simples para testes Cucumber sem TestContainers.
 * 
 * Esta versão executa os testes com configuração mínima:
 * - Banco H2 em memória
 * - Cache simples (sem Redis)
 * - Sem Kafka
 * 
 * Para executar:
 * mvn test -Dtest=SimpleCucumberTestRunner
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.inventory.management.store.cucumber.stepdefinitions,com.inventory.management.store.cucumber.simple")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty,html:target/cucumber-reports/simple-html,json:target/cucumber-reports/simple-json/cucumber.json,junit:target/cucumber-reports/simple-junit.xml")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "not @ignore and not @integration")
public class SimpleCucumberTestRunner {
    // Esta classe é usada apenas como runner para os testes Cucumber simples
}
