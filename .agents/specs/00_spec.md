# Stack 
Frontend

# React + TypeScript

- React + Vite
- TypeScript
- Tailwind CSS
- shadcn/ui
- React Router
- TanStack Query
- Zustand
- React Hook Form + Zod
- Recharts (gráficos)

Backend

# Java 25 + Spring Boot

- Java 25 (LTS)
- Spring Boot
- Maven 3.9.16
- Spring Web
- Spring Data JPA
- Spring Security
- Spring Validation
- Spring Actuator
- Hibernate
- Lombok (opcional)

Banco de dados

# MySQL 8.4 LTS

- MySQL 8.4
- Spring Data JPA
- Hibernate ORM
- Flyway (migrações)
- HikariCP (pool de conexões)

Autenticação e segurança

- Spring Security
- JWT (Access Token, HS256)
- Refresh Token com rotação
- Argon2id para senhas
- Controle de acesso por usuário
- CORS configurado

Testes

Backend

- JUnit 5
- Mockito
- Spring Boot Test
- Testcontainers
- JaCoCo (cobertura)

Frontend

- Vitest
- React Testing Library
- Playwright (testes E2E)

DevOps e infraestrutura
- Docker
- Docker Compose

Documentação e monitoramento
- Swagger / OpenAPI
- Spring Boot Actuator
- Logs estruturados

# Funcionalidades aprimoradas

| Módulo | Funcionalidades |
| --- | --- |
| Autenticação | Cadastro, login, logout, refresh token e recuperação de senha |
| Tarefas | CRUD, descrição, prazo, conclusão e edição |
| Categorias | Criar, editar, excluir e personalizar categorias |
| Prioridades | Baixa, média, alta e urgente |
| Status | Pendente, em andamento, concluída e cancelada |
| Filtros | Status, prioridade, categoria e período |
| Dashboard | Total de tarefas, concluídas, pendentes e atrasadas |
| Produtividade | Gráficos de conclusão e histórico de atividades |
| Busca | Pesquisa de tarefas por título e descrição |
| Perfil | Dados do usuário e alteração de senha |
| Responsividade | Layout adaptável para desktop, tablet e mobile |

# Arquitetura

Arquitetura em camadas no backend, com separação clara das responsabilidades.

Frontend — React + TypeScript

Components · Pages · Hooks · Services

REST API / JSON

Backend — Spring Boot

Controllers · Services · Repositories · DTOs

Persistence Layer

Spring Data JPA · Hibernate · Flyway

MySQL 8.4

Users · Tasks · Categories · Activity Logs

### Estrutura de pastas do backend

text

```text
backend/
├── src/main/java/com/taskflow/
│   ├── config/
│   ├── security/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   ├── dto/
│   ├── mapper/
│   ├── exception/
│   └── enums/
├── src/main/resources/
│   ├── db/migration/
│   └── application.yml
└── src/test/
```

### Estrutura de pastas do frontend

text

```text
frontend/
├── src/
│   ├── assets/
│   ├── components/
│   ├── pages/
│   ├── layouts/
│   ├── hooks/
│   ├── services/
│   ├── stores/
│   ├── schemas/
│   ├── types/
│   ├── utils/
│   └── routes/
├── public/
└── tests/
```

# Diferenciais

Para deixar o projeto mais interessante tecnicamente e também como projeto de portfólio:

1. Multiusuário: cada usuário só acessa suas próprias tarefas.
2. Paginação no backend: evita carregar milhares de tarefas de uma vez.
3. Soft Delete: permite recuperar tarefas excluídas.
4. Histórico de atividades: registra alterações e conclusões.
5. Notificações: alertas de tarefas próximas do vencimento.
6. Dark Mode: suporte a tema claro e escuro.
7. Auditoria: registro de criação e atualização de registros.