package com.inventory.management.store.cucumber;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.*;

/**
 * Classe de execução dos testes Cucumber com configuração simples.
 *
 * Usa a configuração CucumberSpringConfiguration para contexto Spring
 * com perfil de teste "test" que utiliza H2 em memória.
 *
 * Para executar:
 * - mvn test -Dtest=CucumberTestRunner
 * - Ou executar diretamente na IDE
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.inventory.management.store.cucumber")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty,html:target/cucumber-reports/cucumber-html,json:target/cucumber-reports/cucumber.json,junit:target/cucumber-reports/cucumber.xml")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "not @ignore")
public class CucumberTestRunner {
    // Esta classe é usada apenas como runner para os testes Cucumber
    // O contexto Spring é configurado pela CucumberSpringConfiguration
}
