---
name: database-development
description: Modela e implementa a camada de banco de dados e persistência do TaskFlow utilizando MySQL 8.4 LTS, Flyway, Spring Data JPA e Hibernate.
---

# Database Development

## 1. Objetivo

Implementar a modelagem relacional, migrations e camada de persistência do TaskFlow com foco em integridade referencial, isolamento multiusuário, consistência temporal e performance.

## 2. Referências obrigatórias

Antes de criar ou alterar entidades e migrations, consultar:

-   `.agents/specs/database.md`
-   `.agents/specs/business-rules.md`
-   `.agents/specs/architecture.md`
-   `.agents/specs/technology-stack.md`

## 3. Stack

-   MySQL 8.4 LTS.
-   Flyway.
-   Spring Data JPA.
-   Hibernate ORM.
-   HikariCP.

## 4. Convenções SQL e Flyway

-   Nomes de tabelas no plural e em `snake_case`.
-   Nomes de colunas em `snake_case`.
-   Chaves primárias `BIGINT AUTO_INCREMENT`.
-   Chaves estrangeiras explícitas (`FOREIGN KEY`) garantindo integridade referencial.
-   Migrations versionadas e sequenciais em `backend/src/main/resources/db/migration/`:
    -   `V1__create_users.sql`
    -   `V2__create_categories.sql`
    -   `V3__create_tasks.sql`
    -   `V4__create_activity_logs.sql`
    -   `V5__create_refresh_tokens.sql`
    -   `V6__create_password_reset_tokens.sql`
-   Nunca alterar migrations já aplicadas; novas alterações exigem novas migrations.
-   Validar migrations em banco MySQL limpo (via Docker e Testcontainers).

## 5. Regras de modelagem consolidadas

### Datas e fuso horário

-   Persistir todos os instantes (`created_at`, `updated_at`, `deleted_at`, `due_date`, `completed_at`, `expires_at`, `revoked_at`, `used_at`) como `DATETIME` em UTC (`Instant` no Java).
-   Manter o campo `users.timezone` (`VARCHAR(64) NOT NULL DEFAULT 'America/Sao_Paulo'`) para agrupamentos diários no Dashboard.

### Soft Delete e integridade

-   `users`, `tasks` e `categories` utilizam Soft Delete com coluna `deleted_at DATETIME NULL`.
-   Unicidade de nome de categoria por usuário aplica-se apenas a categorias ativas (`deleted_at IS NULL`), utilizando no MySQL 8.4 índice único funcional:
    `UNIQUE INDEX uk_categories_user_active_name (user_id, (CASE WHEN deleted_at IS NULL THEN LOWER(name) END))`
    aliado à validação explícita no Service.
-   A exclusão lógica de uma categoria deve desassociar (`category_id = NULL`) todas as tarefas vinculadas (ativas e excluídas logicamente) na mesma transação.

### Limites de campos

-   `users.name`: `VARCHAR(100)`
-   `users.email`: `VARCHAR(255)`
-   `users.password_hash`: `VARCHAR(255)`
-   `users.timezone`: `VARCHAR(64)`
-   `categories.name`: `VARCHAR(50)`
-   `categories.color`: `VARCHAR(7)` (`#RRGGBB`)
-   `tasks.title`: `VARCHAR(150)`
-   `tasks.description`: `TEXT` (validado na aplicação até 5000 caracteres)
-   `tasks.status` / `tasks.priority`: `VARCHAR(20)`

### Concorrência e performance

-   Utilizar estratégia simples Last-Write-Wins com atualização consistente de `updated_at`.
-   Filtrar sempre por `user_id` e `deleted_at IS NULL` nas consultas padrão.
-   Prevenir N+1 queries (usar `JOIN FETCH`, `@EntityGraph` ou consultas agregadas/projeções no Dashboard e listagens).
-   Executar testes de persistência contra MySQL real via Testcontainers, evitando depender de comportamentos específicos do H2.
