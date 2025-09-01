package com.inventory.management.store.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para SwaggerConfig.
 * Valida todas as configurações do Swagger/OpenAPI.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SwaggerConfig Tests")
@TestPropertySource(properties = {
    "server.port=8080"
})
class SwaggerConfigTest {

    // Field name constants
    private static final String SERVER_PORT_FIELD = "serverPort";

    // Default value constants
    private static final String DEFAULT_SERVER_PORT = "8080";

    // Expected values constants
    private static final String EXPECTED_API_TITLE = "Store Service API";
    private static final String EXPECTED_API_VERSION = "1.0.0";
    private static final String EXPECTED_CONTACT_EMAIL = "suporte@inventory.com";
    private static final String EXPECTED_CONTACT_NAME = "Equipe de Inventário";
    private static final String EXPECTED_LICENSE_NAME = "MIT License";
    private static final String EXPECTED_LICENSE_URL = "https://choosealicense.com/licenses/mit/";
    private static final String EXPECTED_DESCRIPTION_PART = "API para gerenciamento de inventário de lojas";
    private static final String EXPECTED_SERVER_DESCRIPTION = "Store Service - Ambiente Local";
    private static final String SERVER_URL_PREFIX = "http://localhost:";
    private static final String HTTPS_PREFIX = "https://";
    private static final String HTTP_PREFIX = "http://";

    private SwaggerConfig swaggerConfig;

    @BeforeEach
    void setUp() {
        swaggerConfig = new SwaggerConfig();
        
        // Set default values using reflection
        ReflectionTestUtils.setField(swaggerConfig, SERVER_PORT_FIELD, DEFAULT_SERVER_PORT);
    }

    @Nested
    @DisplayName("OpenAPI Configuration Tests")
    class OpenAPIConfigurationTests {

        @Test
        @DisplayName("Should create OpenAPI bean")
        void shouldCreateOpenAPIBean() {
            // When
            OpenAPI openAPI = swaggerConfig.storeServiceOpenAPI();

            // Then
            assertNotNull(openAPI);
            assertInstanceOf(OpenAPI.class, openAPI);
        }

        @Test
        @DisplayName("Should configure API info correctly")
        void shouldConfigureAPIInfoCorrectly() {
            // When
            OpenAPI openAPI = swaggerConfig.storeServiceOpenAPI();

            // Then
            assertNotNull(openAPI);
            Info info = openAPI.getInfo();
            assertNotNull(info);
            assertEquals(EXPECTED_API_TITLE, info.getTitle());
            assertEquals(EXPECTED_API_VERSION, info.getVersion());
            assertTrue(info.getDescription().contains(EXPECTED_DESCRIPTION_PART));
        }

        @Test
        @DisplayName("Should configure contact information correctly")
        void shouldConfigureContactInformationCorrectly() {
            // When
            OpenAPI openAPI = swaggerConfig.storeServiceOpenAPI();

            // Then
            assertNotNull(openAPI);
            Info info = openAPI.getInfo();
            Contact contact = info.getContact();
            assertNotNull(contact);
            assertEquals(EXPECTED_CONTACT_EMAIL, contact.getEmail());
            assertEquals(EXPECTED_CONTACT_NAME, contact.getName());
        }

        @Test
        @DisplayName("Should configure license information correctly")
        void shouldConfigureLicenseInformationCorrectly() {
            // When
            OpenAPI openAPI = swaggerConfig.storeServiceOpenAPI();

            // Then
            assertNotNull(openAPI);
            Info info = openAPI.getInfo();
            License license = info.getLicense();
            assertNotNull(license);
            assertEquals(EXPECTED_LICENSE_NAME, license.getName());
            assertEquals(EXPECTED_LICENSE_URL, license.getUrl());
        }

        @Test
        @DisplayName("Should configure server with default port")
        void shouldConfigureServerWithDefaultPort() {
            // When
            OpenAPI openAPI = swaggerConfig.storeServiceOpenAPI();

            // Then
            assertNotNull(openAPI);
            List<Server> servers = openAPI.getServers();
            assertNotNull(servers);
            assertFalse(servers.isEmpty());

            Server server = servers.get(0);
            assertEquals(SERVER_URL_PREFIX + DEFAULT_SERVER_PORT, server.getUrl());
            assertEquals(EXPECTED_SERVER_DESCRIPTION, server.getDescription());
        }

        @Test
        @DisplayName("Should configure server with custom port")
        void shouldConfigureServerWithCustomPort() {
            // Given
            String customPort = "9090";
            ReflectionTestUtils.setField(swaggerConfig, SERVER_PORT_FIELD, customPort);

            // When
            OpenAPI openAPI = swaggerConfig.storeServiceOpenAPI();

            // Then
            assertNotNull(openAPI);
            List<Server> servers = openAPI.getServers();
            assertNotNull(servers);
            assertFalse(servers.isEmpty());

            Server server = servers.get(0);
            assertEquals(SERVER_URL_PREFIX + customPort, server.getUrl());
            assertEquals(EXPECTED_SERVER_DESCRIPTION, server.getDescription());
        }

        @Test
        @DisplayName("Should have only one server configured")
        void shouldHaveOnlyOneServerConfigured() {
            // When
            OpenAPI openAPI = swaggerConfig.storeServiceOpenAPI();

            // Then
            assertNotNull(openAPI);
            List<Server> servers = openAPI.getServers();
            assertNotNull(servers);
            assertEquals(1, servers.size());
        }

        @Test
        @DisplayName("Should have complete API description")
        void shouldHaveCompleteAPIDescription() {
            // When
            OpenAPI openAPI = swaggerConfig.storeServiceOpenAPI();

            // Then
            assertNotNull(openAPI);
            Info info = openAPI.getInfo();
            String description = info.getDescription();
            
            assertNotNull(description);
            assertFalse(description.trim().isEmpty());
            assertTrue(description.contains(EXPECTED_DESCRIPTION_PART));
            assertTrue(description.contains("busca"));
            assertTrue(description.contains("reserva"));
            assertTrue(description.contains("confirmação"));
            assertTrue(description.contains("cancelamento"));
            assertTrue(description.contains("produtos"));
        }
    }

    @Nested
    @DisplayName("Configuration Annotations Tests")
    class ConfigurationAnnotationsTests {

        @Test
        @DisplayName("Should have @Configuration annotation")
        void shouldHaveConfigurationAnnotation() {
            // When
            boolean hasConfigurationAnnotation = SwaggerConfig.class.isAnnotationPresent(
                org.springframework.context.annotation.Configuration.class);

            // Then
            assertTrue(hasConfigurationAnnotation);
        }
    }

    @Nested
    @DisplayName("Bean Definition Tests")
    class BeanDefinitionTests {

        @Test
        @DisplayName("Should have storeServiceOpenAPI bean method")
        void shouldHaveStoreServiceOpenAPIBeanMethod() throws NoSuchMethodException {
            // When
            var method = SwaggerConfig.class.getDeclaredMethod("storeServiceOpenAPI");

            // Then
            assertNotNull(method);
            assertTrue(method.isAnnotationPresent(org.springframework.context.annotation.Bean.class));
        }

        @Test
        @DisplayName("Bean method should return OpenAPI type")
        void beanMethodShouldReturnOpenAPIType() throws NoSuchMethodException {
            // When
            var method = SwaggerConfig.class.getDeclaredMethod("storeServiceOpenAPI");

            // Then
            assertEquals(OpenAPI.class, method.getReturnType());
        }
    }

    @Nested
    @DisplayName("Server Configuration Edge Cases")
    class ServerConfigurationEdgeCasesTests {

        @Test
        @DisplayName("Should handle null server port gracefully")
        void shouldHandleNullServerPortGracefully() {
            // Given
            ReflectionTestUtils.setField(swaggerConfig, SERVER_PORT_FIELD, null);

            // When
            OpenAPI openAPI = swaggerConfig.storeServiceOpenAPI();

            // Then
            assertNotNull(openAPI);
            List<Server> servers = openAPI.getServers();
            assertNotNull(servers);
            assertFalse(servers.isEmpty());

            Server server = servers.get(0);
            assertEquals(SERVER_URL_PREFIX + "null", server.getUrl());
        }

        @Test
        @DisplayName("Should handle empty server port")
        void shouldHandleEmptyServerPort() {
            // Given
            ReflectionTestUtils.setField(swaggerConfig, SERVER_PORT_FIELD, "");

            // When
            OpenAPI openAPI = swaggerConfig.storeServiceOpenAPI();

            // Then
            assertNotNull(openAPI);
            List<Server> servers = openAPI.getServers();
            assertNotNull(servers);
            assertFalse(servers.isEmpty());

            Server server = servers.get(0);
            assertEquals(SERVER_URL_PREFIX, server.getUrl());
        }

        @Test
        @DisplayName("Should handle numeric server port as string")
        void shouldHandleNumericServerPortAsString() {
            // Given
            String numericPort = "8443";
            ReflectionTestUtils.setField(swaggerConfig, SERVER_PORT_FIELD, numericPort);

            // When
            OpenAPI openAPI = swaggerConfig.storeServiceOpenAPI();

            // Then
            assertNotNull(openAPI);
            List<Server> servers = openAPI.getServers();
            Server server = servers.get(0);
            assertEquals(SERVER_URL_PREFIX + numericPort, server.getUrl());
        }

        @Test
        @DisplayName("Should handle very high port numbers")
        void shouldHandleVeryHighPortNumbers() {
            // Given
            String highPort = "65535";
            ReflectionTestUtils.setField(swaggerConfig, SERVER_PORT_FIELD, highPort);

            // When
            OpenAPI openAPI = swaggerConfig.storeServiceOpenAPI();

            // Then
            assertNotNull(openAPI);
            List<Server> servers = openAPI.getServers();
            Server server = servers.get(0);
            assertEquals(SERVER_URL_PREFIX + highPort, server.getUrl());
        }
    }

    @Nested
    @DisplayName("Object Validation Tests")
    class ObjectValidationTests {

        @Test
        @DisplayName("Should create valid Contact object")
        void shouldCreateValidContactObject() {
            // When
            OpenAPI openAPI = swaggerConfig.storeServiceOpenAPI();

            // Then
            Contact contact = openAPI.getInfo().getContact();
            assertNotNull(contact);
            assertNotNull(contact.getEmail());
            assertNotNull(contact.getName());
            assertFalse(contact.getEmail().trim().isEmpty());
            assertFalse(contact.getName().trim().isEmpty());
        }

        @Test
        @DisplayName("Should create valid License object")
        void shouldCreateValidLicenseObject() {
            // When
            OpenAPI openAPI = swaggerConfig.storeServiceOpenAPI();

            // Then
            License license = openAPI.getInfo().getLicense();
            assertNotNull(license);
            assertNotNull(license.getName());
            assertNotNull(license.getUrl());
            assertFalse(license.getName().trim().isEmpty());
            assertFalse(license.getUrl().trim().isEmpty());
            assertTrue(license.getUrl().startsWith(HTTPS_PREFIX));
        }

        @Test
        @DisplayName("Should create valid Server object")
        void shouldCreateValidServerObject() {
            // When
            OpenAPI openAPI = swaggerConfig.storeServiceOpenAPI();

            // Then
            Server server = openAPI.getServers().get(0);
            assertNotNull(server);
            assertNotNull(server.getUrl());
            assertNotNull(server.getDescription());
            assertFalse(server.getUrl().trim().isEmpty());
            assertFalse(server.getDescription().trim().isEmpty());
            assertTrue(server.getUrl().startsWith(HTTP_PREFIX));
        }

        @Test
        @DisplayName("Should create valid Info object with all required fields")
        void shouldCreateValidInfoObjectWithAllRequiredFields() {
            // When
            OpenAPI openAPI = swaggerConfig.storeServiceOpenAPI();

            // Then
            Info info = openAPI.getInfo();
            assertNotNull(info);
            assertNotNull(info.getTitle());
            assertNotNull(info.getVersion());
            assertNotNull(info.getDescription());
            assertNotNull(info.getContact());
            assertNotNull(info.getLicense());
            
            assertFalse(info.getTitle().trim().isEmpty());
            assertFalse(info.getVersion().trim().isEmpty());
            assertFalse(info.getDescription().trim().isEmpty());
        }
    }

    @Nested
    @DisplayName("Configuration Consistency Tests")
    class ConfigurationConsistencyTests {

        @Test
        @DisplayName("Should maintain consistency across multiple calls")
        void shouldMaintainConsistencyAcrossMultipleCalls() {
            // When
            OpenAPI openAPI1 = swaggerConfig.storeServiceOpenAPI();
            OpenAPI openAPI2 = swaggerConfig.storeServiceOpenAPI();

            // Then - Should create new instances but with same configuration
            assertNotSame(openAPI1, openAPI2);
            assertEquals(openAPI1.getInfo().getTitle(), openAPI2.getInfo().getTitle());
            assertEquals(openAPI1.getInfo().getVersion(), openAPI2.getInfo().getVersion());
            assertEquals(openAPI1.getServers().get(0).getUrl(), openAPI2.getServers().get(0).getUrl());
        }

        @Test
        @DisplayName("Should be thread safe")
        void shouldBeThreadSafe() {
            // Given
            int threadCount = 10;
            Thread[] threads = new Thread[threadCount];
            OpenAPI[] results = new OpenAPI[threadCount];
            Exception[] exceptions = new Exception[threadCount];

            // When
            for (int i = 0; i < threadCount; i++) {
                final int threadIndex = i;
                threads[i] = new Thread(() -> {
                    try {
                        results[threadIndex] = swaggerConfig.storeServiceOpenAPI();
                    } catch (Exception e) {
                        exceptions[threadIndex] = e;
                    }
                });
                threads[i].start();
            }

            // Wait for all threads to complete
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    fail("Thread was interrupted");
                }
            }

            // Then
            for (Exception exception : exceptions) {
                assertNull(exception, "Thread should not have thrown exception: " + 
                    (exception != null ? exception.getMessage() : ""));
            }

            for (OpenAPI result : results) {
                assertNotNull(result);
                assertEquals(EXPECTED_API_TITLE, result.getInfo().getTitle());
                assertEquals(EXPECTED_API_VERSION, result.getInfo().getVersion());
            }
        }
    }
}
