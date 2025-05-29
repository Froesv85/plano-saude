🏋️ PlanoSaude - Gerencie sua Saúde com Facilidade
Bem-vindo ao PlanoSaude, um projeto desenvolvido por Santhiago Chapiewski e Vinicius Froes como parte da disciplina Programação WEB, ministrada pelo professor Leonardo Vitazik Neto. Esta aplicação web, construída com Spring Boot, permite gerenciar treinos, refeições, eventos (como consultas médicas) e o peso do usuário, oferecendo suporte tanto para o padrão MVC quanto MVVM. É uma solução prática para quem busca organizar sua rotina de saúde de forma eficiente!
🌟 O que o PlanoSaude faz?

Treinos: Registre, edite e remova treinos com descrição e data.
Refeições: Gerencie suas refeições diárias com facilidade.
Eventos: Agende e acompanhe eventos como consultas médicas, com validação de conflitos de horário.
Peso: Monitore seu peso (funcionalidade em desenvolvimento).
Dashboard: Veja rapidamente os treinos, refeições e eventos do dia.
Calendário: Visualize eventos agendados (em breve, com integração de um componente de calendário).

✅ Requisitos Atendidos
Front-end

MVC:
Configurado com Thymeleaf para renderização de templates.
Templates criados: dashboard.html, treino.html, alimentacao.html, evento.html, calendario.html.


MVVM:
Configurado com Swagger para documentação e teste de APIs.
Entidade Evento exposta via API REST.



Back-end

Beans de Persistência (100%):
Entidades: Treino, Refeicao, Evento, UsuarioPeso.


Data Access (100%):
Repositórios Spring Data JPA: TreinoRepository, RefeicaoRepository, EventoRepository, UsuarioPesoRepository.


Controllers:
MVC: PlanoController para gerenciar a interface web.
REST: EventoController, RefeicaoController, TreinoController para APIs.



🛠️ Tecnologias Utilizadas

Java 17
Spring Boot 3.4.5
Spring Data JPA
H2 Database (banco em memória para desenvolvimento)
Thymeleaf (MVC)
Swagger (MVVM)
Bootstrap
Lombok
SLF4J

📂 Estrutura do Projeto
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

🚀 Como Começar
Pré-requisitos

Java 17 ou superior
Maven 3.8+
Git

Instalação

Clone o repositório:
git clone https://github.com/seu-usuario/plano-saude.git
cd plano-saude


Configure o ambiente:

Verifique o Java: java -version
Verifique o Maven: mvn -version


Configure o banco de dados (H2 por padrão):Edite src/main/resources/application.properties se precisar de outro banco:
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update


Configure o Swagger (para MVVM):Adicione ao pom.xml:
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.6.0</version>
</dependency>


Execute a aplicação:
mvn clean install
mvn spring-boot:run


Acesse:

MVC: http://localhost:8080
MVVM (Swagger): http://localhost:8080/swagger-ui.html



🎯 Como Usar

Dashboard (/): Veja treinos, refeições e eventos do dia.
Treinos (/treino ou /api/treinos): Adicione ou gerencie treinos.
Refeições (/alimentacao ou /api/refeicoes): Registre suas refeições.
Eventos (/eventos ou /api/eventos): Agende consultas ou eventos.
Calendário (/calendario): Visualize eventos (em desenvolvimento).

Exemplo (MVC): Para adicionar um treino, vá até http://localhost:8080/treino, insira a descrição e salve.Exemplo (MVVM): Acesse http://localhost:8080/swagger-ui.html e use /api/eventos para criar eventos.
🗄️ Configuração de Banco de Dados
Para usar MySQL:

Adicione a dependência no pom.xml:<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>


Atualize application.properties:spring.datasource.url=jdbc:mysql://localhost:3306/planosaude
spring.datasource.username=seu-usuario
spring.datasource.password=sua-senha
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=update



🤝 Contribuindo
Quer ajudar? Siga estes passos:

Faça um fork do repositório.
Crie uma branch: git checkout -b sua-feature
Commit suas alterações: git commit -m "Adiciona sua feature"
Envie para o repositório: git push origin sua-feature
Abra um Pull Request.

Siga nosso Código de Conduta e use Conventional Commits.
⚠️ Problemas Conhecidos

Peso fixo no dashboard (85.0). Em breve, será dinâmico com UsuarioPeso.
Visualização de calendário pendente no front-end.
Suporte a múltiplos usuários requer Spring Security.

📜 Licença
Licenciado sob a MIT License.
📞 Contato
Desenvolvido por Santhiago Chapiewski e Vinicius Froes. Dúvidas ou sugestões? Abra uma issue no GitHub!
