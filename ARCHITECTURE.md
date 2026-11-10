# TaskFlow — Architecture

## 1. Visão geral

O TaskFlow é uma aplicação web full-stack para gerenciamento de tarefas e produtividade pessoal.

A aplicação utiliza arquitetura cliente-servidor, com frontend independente e backend responsável pelas regras de negócio, autenticação e persistência dos dados.

A comunicação entre frontend e backend ocorre através de uma API REST utilizando JSON.

## 2. Arquitetura geral

text

```
                 USER
                   |
                   v
          FRONTEND - REACT
          React + TypeScript
                   |
                   | HTTP / JSON
                   | REST API
                   v
          BACKEND - SPRING BOOT
                   |
          +--------+--------+
          |        |        |
          v        v        v
      Security  Services  Controllers
          |        |
          |        v
          |    Repositories
          |        |
          +--------+
                   |
                   v
              Hibernate
              Spring Data
                   |
                   v
               MySQL 8.4
                   |
              Flyway

```

## 3. Stack tecnológica

### Frontend

-   React.
-   TypeScript.
-   Vite.
-   Tailwind CSS.
-   shadcn/ui.
-   React Router.
-   TanStack Query.
-   Zustand.
-   React Hook Form.
-   Zod.
-   Recharts.

### Backend

-   Java 25.
-   Spring Boot.
-   Maven 3.9.16.
-   Spring Web.
-   Spring Data JPA.
-   Spring Security.
-   Spring Validation.
-   Hibernate.
-   Spring Actuator.

### Banco de dados

-   MySQL 8.4 LTS.
-   Flyway.
-   HikariCP.

### Segurança

-   Spring Security.
-   JWT Access Token.
-   Refresh Token com rotação.
-   Argon2id.
-   Controle de acesso por usuário.

### Testes

Backend:

-   JUnit 5.
-   Mockito.
-   Spring Boot Test.
-   Testcontainers.
-   JaCoCo.

Frontend:

-   Vitest.
-   React Testing Library.
-   Playwright.

### Infraestrutura

-   Docker.
-   Docker Compose.

## 4. Organização do backend

O backend utiliza arquitetura em camadas.

text

```
backend/
└── src/
    ├── main/
    │   ├── java/com/productivity/
    │   │   ├── config/
    │   │   ├── security/
    │   │   ├── controller/
    │   │   ├── service/
    │   │   ├── repository/
    │   │   ├── entity/
    │   │   ├── dto/
    │   │   ├── mapper/
    │   │   ├── exception/
    │   │   └── enums/
    │   │
    │   └── resources/
    │       ├── db/migration/
    │       └── application.yml/
    │
    └── test/

```

Responsabilidades:

-   Controllers: comunicação HTTP.
-   Services: regras de negócio.
-   Repositories: acesso aos dados.
-   Entities: representação persistente.
-   DTOs: contratos de entrada e saída.
-   Security: autenticação e autorização.
-   Config: configurações da aplicação.

## 5. Organização do frontend

O frontend utiliza uma arquitetura baseada em componentes e separação de responsabilidades.

text

```
frontend/
└── src/
    ├── assets/
    ├── components/
    ├── pages/
    ├── layouts/
    ├── hooks/
    ├── services/
    ├── stores/
    ├── schemas/
    ├── types/
    ├── utils/
    └── routes/

```

Responsabilidades:

-   Components: componentes reutilizáveis.
-   Pages: páginas da aplicação.
-   Layouts: estruturas de apresentação.
-   Hooks: lógica reutilizável.
-   Services: comunicação com a API.
-   Stores: estado global da interface.
-   Schemas: validação de formulários.
-   Types: tipagens TypeScript.
-   Routes: configuração de navegação.

## 6. Principais módulos

Módulo

Responsabilidade

Authentication

Cadastro, login e gerenciamento de sessão

Tasks

Gerenciamento de tarefas

Categories

Organização das tarefas

Dashboard

Indicadores de produtividade

Activity Logs

Histórico de atividades

Users

Gerenciamento de perfil

## 7. Banco de dados

O MySQL será responsável pela persistência dos dados.

Principais entidades:

-   User
-   Task
-   Category
-   RefreshToken
-   PasswordResetToken
-   ActivityLog

Características:

-   Identificadores BIGINT.
-   Relacionamentos com integridade referencial.
-   Isolamento de dados por usuário.
-   Soft Delete onde definido.
-   Migrações controladas pelo Flyway.
-   Datas persistidas de maneira consistente em UTC.

## 8. Segurança e isolamento

-   Autenticação através de JWT.
-   Refresh Token com rotação.
-   Senhas protegidas com Argon2id.
-   Endpoints privados protegidos pelo Spring Security.
-   Autorização validada no backend.
-   Cada usuário acessa somente seus próprios recursos.
-   Segredos armazenados em variáveis de ambiente ou mecanismos apropriados de gerenciamento de segredos.

## 9. Comunicação entre frontend e backend

A comunicação será realizada através de API REST.

Base URL de desenvolvimento:

`http://localhost:8080/api/v1`

Formato de comunicação:

-   HTTP.
-   JSON.
-   Content-Type: application/json.
-   Autenticação Bearer para Access Token.

Os contratos são definidos em `.ai/specs/api-contracts.md`.

## 10. Infraestrutura

O ambiente de desenvolvimento utilizará Docker Compose para facilitar a execução dos serviços.

Serviços previstos:

-   Frontend.
-   Backend.
-   MySQL.

Configurações específicas de desenvolvimento e produção deverão permanecer separadas.

## 11. Documentação complementar

As decisões arquiteturais detalhadas estão documentadas em:

-   `.ai/specs/architecture.md`
-   `.ai/specs/technology-stack.md`
-   `.ai/specs/database.md`
-   `.ai/specs/security.md`
-   `.ai/specs/api-contracts.md`

As regras de negócio estão em `.ai/specs/business-rules.md`.

As funcionalidades estão especificadas em `.ai/specs/features/`.

## 12. Princípios arquiteturais

-   Separação de responsabilidades.
-   Baixo acoplamento.
-   Alta coesão.
-   Segurança por padrão.
-   Código testável.
-   Evolução incremental.
-   Simplicidade antes de complexidade.
-   Consistência entre frontend, backend e banco de dados.