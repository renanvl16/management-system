<!-- Use this file to provide workspace-specific custom instructions to Copilot. For more details, visit https://code.visualstudio.com/docs/copilot/copilot-customization#_use-a-githubcopilotinstructionsmd-file -->
- [x] Verify that the copilot-instructions.md file in the .github directory is created.

- [x] Clarify Project Requirements
	<!-- Java Spring Boot 3 microservices project for inventory management system with two core services (Store Service and Central Inventory Service) using hexagonal architecture, Java 21, Maven, Lombok, unit tests, Cucumber with TestContainers, JavaDoc, Swagger, Docker, and infrastructure components (Kafka, Redis, Prometheus, Grafana) -->

- [x] Scaffold the Project
	<!--
	Estrutura Maven criada para ambos os microserviços (Store Service e Central Inventory Service)
	Arquitetura hexagonal implementada com pastas domain, application e infrastructure
	Classes de domínio, portas, serviços e controladores criadas
	Configurações Maven com Spring Boot 3, Java 21, Lombok e dependências necessárias
	-->

- [x] Customize the Project
	<!--
	Implementação completa dos dois microserviços:
	- Store Service: domínio, aplicação, infraestrutura, controladores REST
	- Central Inventory Service: estrutura base criada
	- Arquitetura hexagonal implementada
	- Configurações Spring Boot, Kafka, Redis, PostgreSQL
	- Dockerfiles otimizados para ambos os serviços
	- Docker Compose com infraestrutura completa
	- Configurações Prometheus e Grafana
	- Documentação README.md e RUN.md completas
	-->

- [x] Install Required Extensions
	<!-- Não há extensões específicas necessárias para este projeto Java/Maven. -->

- [ ] Compile the Project
	<!--
	Verify that all previous steps have been completed.
	Install any missing dependencies.
	Run diagnostics and resolve any issues.
	Check for markdown files in project folder for relevant instructions on how to do this.
	-->

- [ ] Create and Run Task
	<!--
	Verify that all previous steps have been completed.
	Check https://code.visualstudio.com/docs/debugtest/tasks to determine if the project needs a task. If so, use the create_and_run_task to create and launch a task based on package.json, README.md, and project structure.
	Skip this step otherwise.
	 -->

- [ ] Launch the Project
	<!--
	Verify that all previous steps have been completed.
	Prompt user for debug mode, launch only if confirmed.
	 -->

- [x] Ensure Documentation is Complete
	<!--
	Documentação completa criada:
	- README.md com arquitetura, tecnologias, estrutura do projeto, APIs, monitoramento
	- RUN.md com instruções detalhadas passo a passo para execução
	- Dockerfiles para ambos os microserviços
	- Docker Compose com infraestrutura completa (Kafka, Redis, PostgreSQL, Prometheus, Grafana)
	- Configurações de desenvolvimento e produção
	- Exemplo de teste unitário abrangente
	- Estrutura completa de arquitetura hexagonal implementada
	-->
