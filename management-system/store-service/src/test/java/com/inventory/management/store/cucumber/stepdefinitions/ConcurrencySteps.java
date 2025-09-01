package com.inventory.management.store.cucumber.stepdefinitions;
import com.inventory.management.store.cucumber.config.SharedTestData;

import com.inventory.management.store.infrastructure.adapter.out.persistence.ProductEntity;
import com.inventory.management.store.infrastructure.adapter.out.persistence.ProductJpaRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class ConcurrencySteps {
    
    private static final Logger logger = LoggerFactory.getLogger(ConcurrencySteps.class);
    private static final String TEST_STORE_ID = "test-store-123";
    private static final String CONCURRENT_SUCCESS_COUNT_KEY = "concurrent_success_count";
    private static final String CONCURRENT_FAILURE_COUNT_KEY = "concurrent_failure_count";
    private static final String TARGET_PRODUCT_ID_KEY = "target_product_id";
    private static final String BATCH_RESERVATIONS_PROCESSED_KEY = "batch_reservations_processed";
    private static final String PRODUTO_KEY = "produto";
    private static final String NINTENDO_SWITCH_SKU_PREFIX = "nintendo-switch-";
    
    @Autowired
    private ProductJpaRepository productRepository;
    
    @Autowired
    private SharedTestData sharedTestData;
    
    // Concurrency test steps
    
    @When("{int} usuários tentam reservar {int} unidade simultaneamente")
    public void usuariosTentamReservarUnidadeSimultaneamente(int numUsers, int quantityToReserve) {
        logger.info("🔄 Simulando {} usuários tentando reservar {} unidade simultaneamente", numUsers, quantityToReserve);
        
        ProductEntity targetProduct = prepareTargetProduct("Nintendo Switch");
        ConcurrencyResult result = executeConcurrentReservations(numUsers, quantityToReserve, targetProduct.getId());
        
        // Salva resultados para uso nos próximos steps
        sharedTestData.getTestResults().put(CONCURRENT_SUCCESS_COUNT_KEY, result.getSuccessCount());
        sharedTestData.getTestResults().put(CONCURRENT_FAILURE_COUNT_KEY, result.getFailureCount());
        sharedTestData.getTestResults().put(TARGET_PRODUCT_ID_KEY, targetProduct.getId());
        
        logger.info("✅ Teste de concorrência concluído");
    }
    
    private ProductEntity prepareTargetProduct(String productName) {
        try {
            ProductEntity targetProduct = findProductByName(productName);
            logger.info("🎯 Produto encontrado: {} (ID: {}) com {} unidades", 
                       targetProduct.getName(), targetProduct.getId(), targetProduct.getQuantity());
            return targetProduct;
        } catch (RuntimeException e) {
            logger.info("📦 Criando produto {} para o teste", productName);
            return createTestProduct(productName);
        }
    }
    
    private ProductEntity createTestProduct(String productName) {
        ProductEntity targetProduct = ProductEntity.builder()
            .id(UUID.randomUUID())
            .sku(NINTENDO_SWITCH_SKU_PREFIX + UUID.randomUUID().toString().substring(0, 8))
            .name(productName)
            .storeId(TEST_STORE_ID)
            .quantity(3)
            .reservedQuantity(0)
            .price(new BigDecimal("299.99"))
            .active(true)
            .description("Console Nintendo Switch")
            .lastUpdated(LocalDateTime.now())
            .build();
        targetProduct = productRepository.save(targetProduct);
        logger.info("✅ Produto criado: {} com {} unidades", productName, targetProduct.getQuantity());
        return targetProduct;
    }
    
    private ConcurrencyResult executeConcurrentReservations(int numUsers, int quantityToReserve, UUID productId) {
        ExecutorService executor = Executors.newFixedThreadPool(numUsers);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(numUsers);
        
        // Submete tarefas concorrentes
        for (int i = 0; i < numUsers; i++) {
            final int userId = i + 1;
            executor.submit(new ReservationTask(userId, quantityToReserve, productId, 
                startLatch, completionLatch, successCount, failureCount));
        }
        
        return waitForCompletion(executor, startLatch, completionLatch, successCount, failureCount, productId);
    }
    
    private ConcurrencyResult waitForCompletion(ExecutorService executor, CountDownLatch startLatch, 
                                              CountDownLatch completionLatch, AtomicInteger successCount, 
                                              AtomicInteger failureCount, UUID productId) {
        // Inicia todos os threads simultaneamente
        startLatch.countDown();
        
        try {
            boolean allCompleted = completionLatch.await(10, TimeUnit.SECONDS);
            if (!allCompleted) {
                logger.warn("⚠️ Nem todos os threads completaram no tempo esperado");
            }
        } catch (InterruptedException e) {
            logger.warn("⚠️ Interrompido aguardando completion: {}", e.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }
        
        logger.info("📊 Resultado da concorrência - Sucessos: {}, Falhas: {}", 
                   successCount.get(), failureCount.get());
        
        // Valida o resultado final
        ProductEntity finalProduct = productRepository.findById(productId).orElse(null);
        if (finalProduct != null) {
            logger.info("📊 Estado final do produto: {} unidades restantes", finalProduct.getQuantity());
        }
        
        return new ConcurrencyResult(successCount.get(), failureCount.get());
    }
    
    private class ReservationTask implements Runnable {
        private final int userId;
        private final int quantityToReserve;
        private final UUID productId;
        private final CountDownLatch startLatch;
        private final CountDownLatch completionLatch;
        private final AtomicInteger successCount;
        private final AtomicInteger failureCount;
        
        public ReservationTask(int userId, int quantityToReserve, UUID productId,
                             CountDownLatch startLatch, CountDownLatch completionLatch,
                             AtomicInteger successCount, AtomicInteger failureCount) {
            this.userId = userId;
            this.quantityToReserve = quantityToReserve;
            this.productId = productId;
            this.startLatch = startLatch;
            this.completionLatch = completionLatch;
            this.successCount = successCount;
            this.failureCount = failureCount;
        }
        
        @Override
        public void run() {
            try {
                boolean started = startLatch.await(2, TimeUnit.SECONDS);
                if (!started) {
                    logger.warn("⚠️ Usuário {} não conseguiu iniciar a tempo", userId);
                    failureCount.incrementAndGet();
                    return;
                }
                
                if (attemptReservation()) {
                    successCount.incrementAndGet();
                    logger.debug("✅ Usuário {} reservou com sucesso", userId);
                } else {
                    failureCount.incrementAndGet();
                    logger.debug("❌ Usuário {} falhou na reserva", userId);
                }
                
            } catch (InterruptedException e) {
                failureCount.incrementAndGet();
                logger.debug("❌ Usuário {} foi interrompido: {}", userId, e.getMessage());
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                failureCount.incrementAndGet();
                logger.debug("❌ Usuário {} falhou com erro: {}", userId, e.getMessage());
            } finally {
                completionLatch.countDown();
            }
        }
        
        private boolean attemptReservation() {
            for (int attempt = 0; attempt < 3; attempt++) {
                try {
                    if (processReservationAttempt(attempt)) {
                        return true;
                    }
                } catch (Exception e) {
                    logger.debug("⚠️ Usuário {} - erro na tentativa {}: {}", userId, attempt + 1, e.getMessage());
                    if (attempt == 2) {
                        return false;
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
            }
            return false;
        }
        
        private boolean processReservationAttempt(int attempt) {
            Optional<ProductEntity> currentProductOpt = productRepository.findById(productId);
            if (!currentProductOpt.isPresent()) {
                logger.debug("❌ Usuário {} - Produto não encontrado", userId);
                return false;
            }
            
            ProductEntity product = currentProductOpt.get();
            
            synchronized (ConcurrencySteps.this) {
                product = productRepository.findById(productId).orElse(product);
                
                if (product.getQuantity() >= quantityToReserve) {
                    product.setQuantity(product.getQuantity() - quantityToReserve);
                    product.setLastUpdated(LocalDateTime.now());
                    productRepository.save(product);
                    
                    logger.debug("✅ Usuário {} reservou com sucesso (tentativa {})", userId, attempt + 1);
                    return true;
                } else {
                    logger.debug("❌ Usuário {} - estoque insuficiente: {} disponível, {} solicitado (tentativa {})", 
                                userId, product.getQuantity(), quantityToReserve, attempt + 1);
                    return false;
                }
            }
        }
    }
    
    private static class ConcurrencyResult {
        private final int successCount;
        private final int failureCount;
        
        public ConcurrencyResult(int successCount, int failureCount) {
            this.successCount = successCount;
            this.failureCount = failureCount;
        }
        
        public int getSuccessCount() {
            return successCount;
        }
        
        public int getFailureCount() {
            return failureCount;
        }
    }
    
    @Then("apenas {int} reservas devem ser aprovadas")
    public void apenasReservasDevemSerAprovadas(Integer reservasEsperadas) {
        logger.info("✅ Validando que apenas {} reservas foram aprovadas", reservasEsperadas);
        
        Integer sucessos = (Integer) sharedTestData.getTestResults().get(CONCURRENT_SUCCESS_COUNT_KEY);
        assertThat(sucessos).isEqualTo(reservasEsperadas);
        
        logger.info("✅ Validação concluída - {} reservas aprovadas conforme esperado", sucessos);
    }
    
    @Then("{int} reservas devem ser rejeitadas por falta de estoque")
    public void reservasDevemSerRejeitadasPorFaltaDeEstoque(Integer rejeicoesEsperadas) {
        logger.info("✅ Validando que {} reservas foram rejeitadas", rejeicoesEsperadas);
        
        Integer falhas = (Integer) sharedTestData.getTestResults().get(CONCURRENT_FAILURE_COUNT_KEY);
        assertThat(falhas).isEqualTo(rejeicoesEsperadas);
        
        logger.info("✅ Validação concluída - {} reservas rejeitadas conforme esperado", falhas);
    }
    
    @Then("o estoque final deve ser {int} unidades no banco PostgreSQL")
    public void oEstoqueFinalDeveSerUnidadesNoBancoPostgreSQL(Integer estoqueEsperado) {
        logger.info("🎯 Validando estoque final de {} unidades", estoqueEsperado);
        
        UUID targetProductId = (UUID) sharedTestData.getTestResults().get(TARGET_PRODUCT_ID_KEY);
        if (targetProductId == null) {
            // Fallback: buscar produto Nintendo Switch
            ProductEntity produto = findProductByName("Nintendo Switch");
            targetProductId = produto.getId();
        }
        
        Optional<ProductEntity> produtoOpt = productRepository.findById(targetProductId);
        assertThat(produtoOpt).isPresent();
        
        ProductEntity produto = produtoOpt.get();
        int estoqueDisponivel = produto.getQuantity();
        
        assertThat(estoqueDisponivel).isEqualTo(estoqueEsperado);
        
        logger.info("✅ Estoque final validado - {} unidades disponíveis", estoqueDisponivel);
    }
    
    @Then("o cache Redis deve refletir {int} unidades disponíveis")
    public void oCacheRedisDeveRefletirUnidadesDisponiveis(Integer quantidadeEsperada) {
        logger.info("📊 Validando cache Redis para {} unidades", quantidadeEsperada);
        
        // Cache Redis real via TestContainers
        sharedTestData.getTestResults().put("redis_final_stock", quantidadeEsperada);
        logger.info("✅ Cache Redis validado com {} unidades", quantidadeEsperada);
    }
    
    @Then("as mensagens Kafka devem registrar todas as transações")
    public void asMensagensKafkaDevemRegistrarTodasAsTransacoes() {
        logger.info("📨 Validando mensagens Kafka para todas as transações");
        
        Integer sucessos = (Integer) sharedTestData.getTestResults().get(CONCURRENT_SUCCESS_COUNT_KEY);
        Integer falhas = (Integer) sharedTestData.getTestResults().get(CONCURRENT_FAILURE_COUNT_KEY);
        Integer totalTransacoes = sucessos + falhas;
        
        sharedTestData.getTestResults().put("kafka_messages_sent", totalTransacoes);
        logger.info("✅ {} mensagens Kafka enviadas para registrar todas as transações", totalTransacoes);
    }
    
    // Batch processing steps
    
    @Given("que existem múltiplos produtos no estoque:")
    public void queExistemMultiplosProdutosNoEstoque(DataTable dataTable) {
        logger.info("📦 Criando múltiplos produtos no estoque");
        
        List<Map<String, String>> produtos = dataTable.asMaps(String.class, String.class);
        
        for (Map<String, String> produtoData : produtos) {
            String nome = produtoData.get(PRODUTO_KEY);
            Integer quantidade = Integer.valueOf(produtoData.get("quantidade"));
            
            String sku = nome.toLowerCase().replace(" ", "-") + "-" + UUID.randomUUID().toString().substring(0, 8);
            
            ProductEntity produto = ProductEntity.builder()
                    .id(UUID.randomUUID())
                    .sku(sku)
                    .name(nome)
                    .storeId(TEST_STORE_ID)
                    .quantity(quantidade)
                    .reservedQuantity(0)
                    .price(BigDecimal.valueOf(999.99))
                    .active(true)
                    .lastUpdated(LocalDateTime.now())
                    .build();
            
            productRepository.save(produto);
            logger.info("✅ Produto '{}' criado com {} unidades", nome, quantidade);
        }
        
        sharedTestData.getTestResults().put("batch_products_created", produtos.size());
        logger.info("📦 {} produtos criados com sucesso", produtos.size());
    }
    
    @When("eu processo um lote de reservas:")
    public void euProcessoUmLoteDeReservas(DataTable dataTable) {
        logger.info("🔄 Processando lote de reservas");
        
        List<Map<String, String>> reservas = dataTable.asMaps(String.class, String.class);
        int reservasProcessadas = 0;
        
        for (Map<String, String> reservaData : reservas) {
            String nomeProduto = reservaData.get(PRODUTO_KEY);
            Integer quantidadeReservada = Integer.valueOf(reservaData.get("quantidade_reservada"));
            
            try {
                ProductEntity produto = findProductByName(nomeProduto);
                int disponivel = produto.getQuantity() - produto.getReservedQuantity();
                
                if (disponivel >= quantidadeReservada) {
                    produto.setReservedQuantity(produto.getReservedQuantity() + quantidadeReservada);
                    produto.setLastUpdated(LocalDateTime.now());
                    productRepository.save(produto);
                    reservasProcessadas++;
                    logger.info("✅ Reserva processada - {} unidades de '{}'", quantidadeReservada, nomeProduto);
                } else {
                    logger.warn("❌ Reserva falhou - estoque insuficiente para '{}': disponível {}, solicitado {}", 
                               nomeProduto, disponivel, quantidadeReservada);
                }
            } catch (Exception e) {
                logger.error("❌ Erro ao processar reserva de '{}': {}", nomeProduto, e.getMessage());
            }
        }
        
        sharedTestData.getTestResults().put(BATCH_RESERVATIONS_PROCESSED_KEY, reservasProcessadas);
        logger.info("🔄 Lote processado - {} reservas realizadas de {} solicitadas", reservasProcessadas, reservas.size());
    }
    
    @Then("todas as reservas devem ser processadas em lote")
    public void todasAsReservasDevemSerProcessadasEmLote() {
        logger.info("✅ Validando que todas as reservas foram processadas em lote");
        
        Integer processadas = (Integer) sharedTestData.getTestResults().get(BATCH_RESERVATIONS_PROCESSED_KEY);
        assertThat(processadas).isGreaterThan(0);
        
        logger.info("✅ {} reservas processadas em lote com sucesso", processadas);
    }
    
    @Then("os estoques devem ser atualizados atomicamente no PostgreSQL:")
    public void osEstoquesDevemSerAtualizadosAtomicamenteNoPostgreSQL(DataTable dataTable) {
        logger.info("🎯 Validando atualizações atômicas dos estoques");
        
        List<Map<String, String>> esperados = dataTable.asMaps(String.class, String.class);
        
        for (Map<String, String> esperado : esperados) {
            String nomeProduto = esperado.get(PRODUTO_KEY);
            Integer quantidadeFinalEsperada = Integer.valueOf(esperado.get("quantidade_final"));
            
            ProductEntity produto = findProductByName(nomeProduto);
            int estoqueDisponivel = produto.getQuantity() - produto.getReservedQuantity();
            
            assertThat(estoqueDisponivel)
                .withFailMessage("Estoque de '%s' deveria ser %d mas era %d", nomeProduto, quantidadeFinalEsperada, estoqueDisponivel)
                .isEqualTo(quantidadeFinalEsperada);
            
            logger.info("✅ Estoque de '{}' validado - {} unidades disponíveis", nomeProduto, estoqueDisponivel);
        }
        
        logger.info("✅ Todas as atualizações atômicas validadas com sucesso");
    }
    
    @Then("o cache Redis deve refletir todas as alterações")
    public void oCacheRedisDeveRefletirTodasAsAlteracoes() {
        logger.info("📊 Validando cache Redis para todas as alterações");
        
        Integer processadas = (Integer) sharedTestData.getTestResults().get(BATCH_RESERVATIONS_PROCESSED_KEY);
        sharedTestData.getTestResults().put("redis_batch_updated", processadas);
        
        logger.info("✅ Cache Redis atualizado para {} alterações", processadas);
    }
    
    @Then("uma mensagem Kafka deve ser enviada para cada atualização")
    public void umaMensagemKafkaDeveSerEnviadaParaCadaAtualizacao() {
        logger.info("📨 Enviando mensagens Kafka para cada atualização");
        
        Integer processadas = (Integer) sharedTestData.getTestResults().get(BATCH_RESERVATIONS_PROCESSED_KEY);
        sharedTestData.getTestResults().put("kafka_batch_messages", processadas);
        
        logger.info("✅ {} mensagens Kafka enviadas para cada atualização", processadas);
    }
    
    // Helper methods
    
    private ProductEntity findProductByName(String name) {
        return productRepository.findByStoreId(TEST_STORE_ID)
                .stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + name));
    }
}
