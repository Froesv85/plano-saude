PlanoSaude
PlanoSaude é uma aplicação web desenvolvida pelos alunos Santhiago Chapiewski e Vinicius Froes para atender aos requisitos da matéria Programação WEB, ministrada pelo professor Leonardo Vitazik Neto. A aplicação foi construída com Spring Boot e permite aos usuários gerenciar treinos, refeições e eventos (como consultas médicas), utilizando uma arquitetura que suporta tanto o padrão MVC (com Thymeleaf para renderização de templates) quanto MVVM (com Swagger para expor APIs). O back-end utiliza Spring Data JPA para persistência de dados e atende 100% dos requisitos de beans de persistência e acesso a dados.
Funcionalidades

Gerenciamento de Treinos: Adicione, edite e exclua treinos, com descrição e data.
Gerenciamento de Refeições: Registre, edite e exclua refeições, com descrição e data.
Gerenciamento de Eventos: Crie, edite e exclua eventos (ex.: consultas médicas), com título, data de início, data de término e descrição, com validação de conflitos de horário.
Gerenciamento de Peso: Registre e acompanhe o peso do usuário (funcionalidade em desenvolvimento).
Dashboard: Visualize treinos, refeições e eventos do dia atual.
Calendário: Exiba eventos em uma visualização de calendário (a ser implementada no front-end).
Validações: Garante que campos obrigatórios (como descrição e data) sejam preenchidos.

Requisitos Atendidos
Front-end

Opção MVC:
Configurado Thymeleaf como motor de templates.
Criados templates HTML (dashboard.html, treino.html, alimentacao.html, evento.html, calendario.html) para renderização do lado do servidor.


Opção MVVM:
Configurado Swagger para documentação e teste de APIs.
Exposta a entidade Evento via API REST (através do EventoController).



Back-end

100% Beans de Persistência:
Implementadas quatro entidades: Treino, Refeicao, Evento e UsuarioPeso, todas anotadas com @Entity e utilizando JPA.


100% Data Access:
Utilizado Spring Data JPA para acesso a dados, com repositórios (TreinoRepository, RefeicaoRepository, EventoRepository, UsuarioPesoRepository) que suportam operações CRUD e consultas personalizadas (ex.: findByDataBetween, findByStartBetween).


Controller:
Desenvolvidos os controllers PlanoController (MVC) para gerenciar treinos, refeições e eventos, e controllers específicos EventoController, RefeicaoController e TreinoController (REST) para expor entidades via API, compatíveis com a opção MVVM.



Tecnologias Utilizadas

Java 17: Linguagem principal.
Spring Boot 3.4.5: Framework para desenvolvimento da aplicação.
Spring Data JPA: Para persistência de dados.
H2 Database: Banco de dados em memória para desenvolvimento (pode ser substituído por MySQL, PostgreSQL, etc.).
Thymeleaf: Para renderização de templates no padrão MVC.
Swagger: Para documentação e teste de APIs no padrão MVVM.
Bootstrap: Para estilização do front-end.
Lombok: Para reduzir boilerplate no código Java.
SLF4J: Para logging.

Estrutura do Projeto
plano-saude/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/froes/planosaude/
│   │   │       ├── PlanoSaudeApplication.java
│   │   │       ├── config/
│   │   │       │   ├── AppConfig.java
│   │   │       │   ├── SwaggerConfig.java
│   │   │       ├── controller/
│   │   │       │   ├── EventoController.java
│   │   │       │   ├── Package-info.java
│   │   │       │   ├── PlanoController.java
│   │   │       │   ├── RefeicaoController.java
│   │   │       │   ├── TreinoController.java
│   │   │       ├── model/
│   │   │       │   ├── Evento.java
│   │   │       │   ├── Package-info.java
│   │   │       │   ├── Refeicao.java
│   │   │       │   ├── Treino.java
│   │   │       │   ├── UsuarioPeso.java
│   │   │       ├── repository/
│   │   │       │   ├── EventoRepository.java
│   │   │       │   ├── Package-info.java
│   │   │       │   ├── RefeicaoRepository.java
│   │   │       │   ├── TreinoRepository.java
│   │   │       │   ├── UsuarioPesoRepository.java
│   │   │       ├── service/
│   │   │       │   ├── Package-info.java
│   │   │       │   ├── PlanoService.java
│   │   │       ├── config/
│   │   │       │   ├── Package-info.java
│   │   │       │   └── package-info.java
│   │   ├── resources/
│   │   │   ├── static/
│   │   │   ├── templates/
│   │   │   │   ├── alimentacao.html
│   │   │   │   ├── calendario.html
│   │   │   │   ├── dashboard.html
│   │   │   │   ├── evento.html
│   │   │   │   ├── treino.html
│   │   │   ├── application.properties
│   ├── test/
├── pom.xml
├── README.md

Pré-requisitos

Java 17 ou superior
Maven 3.8+
Git (para clonar o repositório)
Opcional: Banco de dados (ex.: MySQL, PostgreSQL) para produção

Instalação

Clone o repositório:
git clone https://github.com/seu-usuario/plano-saude.git
cd plano-saude


Configure o ambiente:

Certifique-se de que o Java 17 está instalado:java -version


Verifique se o Maven está instalado:mvn -version




Configure o banco de dados:

Por padrão, a aplicação usa o banco H2 em memória. Para configurar outro banco, edite o arquivo src/main/resources/application.properties:spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update




Configure o Swagger (para MVVM):

Adicione a dependência no pom.xml:<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.6.0</version>
</dependency>


Acesse a interface do Swagger em http://localhost:8080/swagger-ui.html.


Compile e execute a aplicação:
mvn clean install
mvn spring-boot:run


Acesse a aplicação:

Para MVC: Abra o navegador em http://localhost:8080.
Para MVVM: Acesse a API via Swagger em http://localhost:8080/swagger-ui.html.



Uso

Dashboard (/): Visualize treinos, refeições e eventos do dia.
Treinos (/treino): Adicione, edite ou exclua treinos (via MVC ou API REST em /api/treinos).
Refeições (/alimentacao): Adicione, edite ou exclua refeições (via MVC ou API REST em /api/refeicoes).
Eventos (/eventos): Adicione, edite ou exclua eventos (via MVC ou API REST em /api/eventos).
Calendário (/calendario): Veja eventos agendados (requer implementação de um componente de calendário no front-end).

Exemplo de como adicionar um treino (MVC):

Acesse http://localhost:8080/treino.
Preencha a descrição do treino (ex.: "Treino de Força").
Clique em "Salvar". A data é automaticamente definida como o momento atual.
O treino aparece na lista de treinos do dia.

Exemplo de como acessar a API (MVVM):

Acesse http://localhost:8080/swagger-ui.html.
Use os endpoints /api/eventos, /api/treinos ou /api/refeicoes para listar ou criar registros.

Configuração do Banco de Dados
Para usar um banco de dados diferente (ex.: MySQL):

Adicione a dependência no pom.xml:<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>


Atualize o application.properties:spring.datasource.url=jdbc:mysql://localhost:3306/planosaude
spring.datasource.username=seu-usuario
spring.datasource.password=sua-senha
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=update



Contribuindo
Contribuições são bem-vindas! Siga os passos abaixo:

Faça um fork do repositório.
Crie uma branch para sua feature:git checkout -b minha-feature


Commit suas alterações:git commit -m "Adiciona minha feature"


Envie para o repositório remoto:git push origin minha-feature


Abra um Pull Request no GitHub.

Por favor, siga o Código de Conduta e use o padrão de commits do Conventional Commits.
Problemas Conhecidos

O peso atual no dashboard é fixo (85.0). Planeja-se implementar um serviço para gerenciar o peso dinamicamente via UsuarioPeso.
A visualização de calendário ainda não está totalmente implementada no front-end.
Suporte a múltiplos usuários requer integração com Spring Security.

Licença
Este projeto está licenciado sob a MIT License.
Contato
Desenvolvido por Santhiago Chapiewski e Vinicius Froes. Para dúvidas ou sugestões, abra uma issue no GitHub.
