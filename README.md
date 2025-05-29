# PlanoSaude

PlanoSaude é uma aplicação web desenvolvida para gerenciar treinos, refeições, eventos e peso do usuário. Construída com Spring Boot, a aplicação suporta os padrões MVC (usando Thymeleaf) e MVVM (via APIs com Swagger).  
Foi desenvolvida por **Santhiago Chapiewski** e **Vinicius Froes**, com o intuito de atender aos pré-requisitos da matéria **Programação WEB**, ministrada pelo professor **Leonardo Vitazik Neto**.  

## CENTRO UNIVERSITÁRIO – CATÓLICA DE SANTA CATARINA  
## JARAGUÁ DO SUL - SANTA CATARINA

## Requisitos

Criar uma aplicação web para gerenciar treinos, refeições e eventos (como consultas médicas), com suporte a padrões MVC e MVVM, incluindo:  
- **Front-end (MVC)**: Templates Thymeleaf para renderização de páginas (ex.: dashboard, treinos, eventos).  
- **Front-end (MVVM)**: APIs REST documentadas com Swagger, com pelo menos uma entidade exposta (Evento).  
- **Back-end**:  
  - 100% beans de persistência (mínimo duas entidades: Treino, Refeicao, Evento, UsuarioPeso).  
  - 100% acesso a dados com Spring Data JPA.  
  - Pelo menos um controller desenvolvido (PlanoController para MVC, controllers REST para MVVM).  
- **Validações**: Garantir que campos obrigatórios sejam preenchidos (ex.: descrição, data).  
- **Funcionalidades**: Adicionar, editar e remover treinos, refeições e eventos; visualizar dashboard com informações do dia.

## Funcionalidades

- Adicionar, editar e remover treinos, refeições e eventos.  
- Validação de conflitos de horário para eventos.  
- Dashboard com visão geral de treinos, refeições e eventos do dia.  
- APIs REST para gerenciar treinos, refeições e eventos (MVVM).  
- Suporte a monitoramento de peso (em desenvolvimento).  

## Tecnologias

- Java 17  
- Spring Boot 3.4.5  
- Spring Data JPA  
- H2 Database (desenvolvimento)  
- Thymeleaf (MVC)  
- Swagger (MVVM)  
- Bootstrap  
- Lombok  
- SLF4J  

## Instalação

### Pré-requisitos
- Java 17 ou superior  
- Maven 3.8+  
- Git  

### Passos
1. Clone o repositório:  
   ```
   git clone https://github.com/seu-usuario/plano-saude.git
   cd plano-saude
   ```
2. Configure o ambiente:  
   - Verifique o Java: `java -version`  
   - Verifique o Maven: `mvn -version`  
3. Configure o banco de dados (H2 por padrão):  
   Edite `src/main/resources/application.properties` com o seguinte código:  
   ```properties
   spring.datasource.url=jdbc:h2:mem:testdb
   spring.datasource.driverClassName=org.h2.Driver
   spring.datasource.username=sa
   spring.datasource.password=
   spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
   spring.jpa.hibernate.ddl-auto=update
   ```
4. Para usar MySQL, adicione ao `pom.xml`:  
   ```xml
   <dependency>
       <groupId>mysql</groupId>
       <artifactId>mysql-connector-java</artifactId>
       <version>8.0.33</version>
   </dependency>
   ```
   E atualize `application.properties` com:  
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/plano_saude
   spring.datasource.username=seu-usuario
   spring.datasource.password=sua-senha
   spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
   spring.jpa.hibernate.ddl-auto=update
   ```
5. Configure o Swagger (MVVM):  
   Adicione ao `pom.xml`:  
   ```xml
   <dependency>
       <groupId>org.springdoc</groupId>
       <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
       <version>2.6.0</version>
   </dependency>
   ```
6. Execute a aplicação:  
   ```
   mvn clean install
   mvn spring-boot:run
   ```
7. Acesse:  
   - MVC: `http://localhost:8080`  
   - Swagger (MVVM): `http://localhost:8080/swagger-ui.html`

## Uso

1. Acesse o dashboard em `http://localhost:8080` para ver treinos, refeições e eventos do dia.  
2. Adicione um treino em `/treino`, uma refeição em `/alimentacao`, ou um evento em `/eventos`.  
3. Use o Swagger em `/swagger-ui.html` para gerenciar dados via API (ex.: `/api/eventos`).  
4. Edite ou remova registros diretamente nas respectivas páginas ou via API.

## Estrutura do Projeto

- `PlanoSaudeApplication.java`: Arquivo principal que inicializa a aplicação.  
- `config/`: Diretório de configurações.  
  - `AppConfig.java`: Configurações gerais da aplicação.  
  - `SwaggerConfig.java`: Configurações do Swagger para APIs.  
- `controller/`: Diretório de controllers.  
  - `PlanoController.java`: Controller MVC para interface web.  
  - `EventoController.java`: Controller REST para gerenciar eventos.  
  - `RefeicaoController.java`: Controller REST para gerenciar refeições.  
  - `TreinoController.java`: Controller REST para gerenciar treinos.  
- `model/`: Diretório de entidades.  
  - `Evento.java`: Entidade para eventos.  
  - `Refeicao.java`: Entidade para refeições.  
  - `Treino.java`: Entidade para treinos.  
  - `UsuarioPeso.java`: Entidade para monitoramento de peso.  
- `repository/`: Diretório de repositórios JPA.  
  - `EventoRepository.java`: Repositório para eventos.  
  - `RefeicaoRepository.java`: Repositório para refeições.  
  - `TreinoRepository.java`: Repositório para treinos.  
  - `UsuarioPesoRepository.java`: Repositório para peso.  
- `service/`: Diretório de serviços.  
  - `PlanoService.java`: Lógica de negócios da aplicação.  
- `resources/templates/`: Diretório de templates Thymeleaf.  
  - `dashboard.html`: Dashboard principal.  
  - `treino.html`: Página de treinos.  
  - `alimentacao.html`: Página de refeições.  
  - `evento.html`: Página de eventos.  
  - `calendario.html`: Página de calendário (em desenvolvimento).  

## Contribuição

Contribuições são bem-vindas! Sinta-se à vontade para abrir issues e pull requests.

## Licença

Distribuído sob a [MIT License](LICENSE).

## Contato

Desenvolvido por **Santhiago Chapiewski** e **Vinicius Froes**.  
Última atualização: **29 de maio de 2025, 17:12 (BRT)**.  
Dúvidas ou sugestões? Abra uma issue no GitHub!
