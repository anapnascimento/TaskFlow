# Database Specification

## 1. Objetivo

Definir a estrutura conceitual e as regras de persistência do TaskFlow.

## 2. Tecnologia

-   MySQL 8.4 LTS.
-   Spring Data JPA.
-   Hibernate ORM.
-   Flyway.
-   HikariCP.

## 3. Convenções

-   Utilizar nomes de tabelas no plural.
-   Utilizar snake_case nos nomes de tabelas e colunas.
-   Utilizar chaves primárias do tipo BIGINT com geração automática.
-   Utilizar Foreign Keys para garantir integridade referencial.
-   Utilizar índices nas colunas frequentemente consultadas.
-   Definir constraints para dados únicos e obrigatórios.
-   Utilizar migrations versionadas com Flyway.

Os nomes e tipos definitivos deverão ser validados durante a implementação.

## 4. Entidades

### 4.1 Users

Representa os usuários cadastrados.

Campos previstos:

| Campo | Tipo conceitual | Descrição |
| :--- | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | Identificador |
| `name` | `VARCHAR(100) NOT NULL` | Nome |
| `email` | `VARCHAR(255) NOT NULL` | E-mail único |
| `password_hash` | `VARCHAR(255) NOT NULL` | Hash da senha (Argon2id) |
| `timezone` | `VARCHAR(64) NOT NULL DEFAULT 'America/Sao_Paulo'` | Fuso horário IANA do usuário |
| `created_at` | `DATETIME NOT NULL` | Data de criação (UTC) |
| `updated_at` | `DATETIME NOT NULL` | Última atualização (UTC) |
| `deleted_at` | `DATETIME NULL` | Exclusão lógica da conta (UTC) |

Regras:

-   E-mail único.
-   Senha armazenada somente como hash Argon2id.
-   Não retornar `password_hash` pela API.
-   Contas com `deleted_at` preenchido têm todas as sessões revogadas e ficam impedidas de autenticar-se.

### 4.2 Tasks

Representa as tarefas dos usuários.

Campos previstos:

| Campo | Tipo conceitual | Descrição |
| :--- | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | Identificador |
| `user_id` | `BIGINT NOT NULL` | Proprietário |
| `category_id` | `BIGINT NULL` | Categoria |
| `title` | `VARCHAR(150) NOT NULL` | Título |
| `description` | `TEXT NULL` | Descrição (validada até 5000 caracteres na API) |
| `status` | `VARCHAR(20) NOT NULL` | Status |
| `priority` | `VARCHAR(20) NOT NULL` | Prioridade |
| `due_date` | `DATETIME NULL` | Prazo (UTC) |
| `completed_at` | `DATETIME NULL` | Data de conclusão (UTC) |
| `created_at` | `DATETIME NOT NULL` | Criação (UTC) |
| `updated_at` | `DATETIME NOT NULL` | Atualização (UTC) |
| `deleted_at` | `DATETIME NULL` | Exclusão lógica (UTC) |

Relacionamentos:

-   Muitas tarefas pertencem a um usuário.
-   Muitas tarefas podem pertencer a uma categoria.
-   Uma tarefa pertence a no máximo uma categoria.

### 4.3 Categories

Representa as categorias personalizadas dos usuários.

Campos previstos:

| Campo | Tipo conceitual | Descrição |
| :--- | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | Identificador |
| `user_id` | `BIGINT NOT NULL` | Proprietário |
| `name` | `VARCHAR(50) NOT NULL` | Nome |
| `color` | `VARCHAR(7) NULL` | Cor visual (`#RRGGBB`) |
| `created_at` | `DATETIME NOT NULL` | Criação (UTC) |
| `updated_at` | `DATETIME NOT NULL` | Atualização (UTC) |
| `deleted_at` | `DATETIME NULL` | Exclusão lógica (UTC) |

Regras:

-   Uma categoria pertence a um usuário.
-   O nome deverá possuir validação (1 a 50 caracteres após normalização).
-   A unicidade de nome por usuário aplica-se exclusivamente entre categorias ativas (`deleted_at IS NULL`), utilizando no MySQL 8.4 um índice único funcional: `UNIQUE INDEX uk_categories_user_active_name (user_id, (CASE WHEN deleted_at IS NULL THEN LOWER(name) END))` aliado à validação no Service.
-   A exclusão lógica da categoria não apaga tarefas relacionadas, devendo desassociar (`category_id = NULL`) todas as tarefas vinculadas (ativas e excluídas logicamente) na mesma transação.

### 4.4 ActivityLogs

Representa o histórico de atividades.

Campos previstos:

Campo

Tipo conceitual

Descrição

id

BIGINT

Identificador

user_id

BIGINT

Usuário responsável

task_id

BIGINT NULL

Tarefa relacionada

action

VARCHAR

Tipo de atividade

description

VARCHAR

Descrição

metadata

JSON NULL

Informações adicionais

created_at

DATETIME

Data da atividade

Regras:

-   O histórico deverá identificar o usuário responsável.
-   Os eventos deverão ser registrados pelo backend.
-   Não armazenar tokens, senhas ou informações sensíveis.
-   A exclusão de tarefas não deverá apagar automaticamente o histórico.

### 4.5 RefreshTokens

Representa as sessões de renovação de autenticação.

Campos previstos:

Campo

Tipo conceitual

Descrição

id

BIGINT

Identificador

user_id

BIGINT

Proprietário

token_hash

VARCHAR

Hash do token

expires_at

DATETIME

Expiração

revoked_at

DATETIME NULL

Revogação

created_at

DATETIME

Criação

replaced_by_id

BIGINT NULL

Token substituto

Regras:

-   Armazenar hash, nunca o Refresh Token original.
-   Permitir revogação.
-   Permitir identificar rotação.
-   Associar cada token a um usuário.

### 4.6 PasswordResetTokens

Representa os tokens de uso único para recuperação e redefinição de senha.

Campos previstos:

| Campo | Tipo conceitual | Descrição |
| :--- | :--- | :--- |
| `id` | `BIGINT AUTO_INCREMENT` | Identificador |
| `user_id` | `BIGINT NOT NULL` | Proprietário |
| `token_hash` | `VARCHAR(255) NOT NULL` | Hash do token de recuperação |
| `expires_at` | `DATETIME NOT NULL` | Expiração (30 minutos em UTC) |
| `used_at` | `DATETIME NULL` | Data de utilização (UTC) |
| `created_at` | `DATETIME NOT NULL` | Criação (UTC) |

Regras:

-   Armazenar somente o hash do token, nunca o valor original.
-   Token de uso único (`used_at` preenchido após consumo) com validade de 30 minutos.
-   Revogar todas as sessões ativas (`refresh_tokens`) após redefinição de senha bem-sucedida.

## 5. Relacionamentos

### Users

-   Um usuário possui várias tarefas.
-   Um usuário possui várias categorias.
-   Um usuário possui vários registros de atividade.
-   Um usuário possui várias sessões de autenticação (`RefreshTokens`).
-   Um usuário pode possuir tokens de recuperação de senha (`PasswordResetTokens`).

### Categories

-   Uma categoria pertence a um usuário.
-   Uma categoria pode possuir várias tarefas.

### Tasks

-   Uma tarefa pertence a um usuário.
-   Uma tarefa pode pertencer a uma categoria.
-   Uma tarefa pode possuir vários registros históricos.

### RefreshTokens

-   Um Refresh Token pertence a um usuário.
-   Um token poderá referenciar seu sucessor.

### PasswordResetTokens

-   Um Password Reset Token pertence a um usuário.

## 6. Índices previstos

### Users

-   `UNIQUE(email)`.

### Tasks

-   `INDEX(user_id)`.
-   `INDEX(user_id, status)`.
-   `INDEX(user_id, priority)`.
-   `INDEX(user_id, due_date)`.
-   `INDEX(user_id, category_id)`.
-   `INDEX(user_id, deleted_at)`.

### Categories

-   `INDEX(user_id)`.
-   `UNIQUE INDEX uk_categories_user_active_name (user_id, (CASE WHEN deleted_at IS NULL THEN LOWER(name) END))` para garantir unicidade apenas entre categorias ativas.

### ActivityLogs

-   `INDEX(user_id, created_at)`.
-   `INDEX(task_id)`.

### RefreshTokens

-   `UNIQUE(token_hash)`.
-   `INDEX(user_id)`.
-   `INDEX(expires_at)`.

### PasswordResetTokens

-   `UNIQUE(token_hash)`.
-   `INDEX(user_id)`.
-   `INDEX(expires_at)`.

Os índices definitivos deverão ser avaliados com base nas consultas reais.

## 7. Integridade referencial

-   `Tasks.user_id` deverá referenciar `Users.id`.
-   `Tasks.category_id` deverá referenciar `Categories.id`.
-   `Categories.user_id` deverá referenciar `Users.id`.
-   `ActivityLogs.user_id` deverá referenciar `Users.id`.
-   `RefreshTokens.user_id` deverá referenciar `Users.id`.
-   `PasswordResetTokens.user_id` deverá referenciar `Users.id`.

A associação entre tarefas e categorias deverá validar que ambas pertencem ao mesmo usuário.

A exclusão física deverá ser evitada quando puder comprometer o histórico ou a integridade referencial.

## 8. Migrações

O Flyway será responsável pelas alterações estruturais.

Padrão de nomenclatura:

-   `V1__create_users.sql`
-   `V2__create_categories.sql`
-   `V3__create_tasks.sql`
-   `V4__create_activity_logs.sql`
-   `V5__create_refresh_tokens.sql`
-   `V6__create_password_reset_tokens.sql`

Regras:

-   Não alterar migrations já aplicadas em ambientes compartilhados.
-   Criar novas migrations para alterações.
-   Validar migrations em banco limpo.
-   Manter consistência entre entidades e estrutura SQL.
-   Não utilizar Hibernate como substituto do versionamento de schema.

## 9. Estratégia de persistência

O acesso aos dados será realizado através de Spring Data JPA.

As regras de negócio deverão ficar nos Services.

As consultas deverão considerar o usuário autenticado.

Evitar:

-   N+1 queries.
-   Carregamento desnecessário de relacionamentos.
-   Exposição de entidades.
-   Consultas sem filtros de propriedade.
-   Exclusões físicas acidentais.

## 10. Definições consolidadas

-   **Exclusão de contas**: Soft Delete em `users` (`deleted_at DATETIME NULL`), com revogação imediata de todos os Refresh Tokens e bloqueio de novo login.
-   **Campos temporais e fuso horário**: todos os instantes persistidos como `DATETIME` em UTC (`Instant` no Java); `users.timezone` (`VARCHAR(64) NOT NULL DEFAULT 'America/Sao_Paulo'`) utilizado pelo backend para agrupamentos diários no Dashboard.
-   **Categorias (exclusão e restauração)**: Soft Delete desassociando (`category_id = NULL`) todas as tarefas vinculadas (ativas e na lixeira) na mesma transação; unicidade aplicada apenas a categorias ativas (`deleted_at IS NULL`) via índice único funcional no MySQL 8.4; restauração permitida apenas se não houver conflito de nome com categoria ativa existente.
-   **Retenção do histórico de atividades**: retenção indefinida em `activity_logs`, com consulta paginada via API (`GET /api/v1/activity-logs`) e exibição dos últimos 10 eventos no Dashboard.
-   **Limites dos campos**: `users.name` (100), `users.email` (255), `users.password_hash` (255), `users.timezone` (64), `categories.name` (50), `categories.color` (7), `tasks.title` (150), `tasks.description` (`TEXT`, validado até 5000 caracteres), `tasks.status` e `tasks.priority` (20).
-   **Estratégia de concorrência**: Last-Write-Wins com atualização automática e consistente de `updated_at` em UTC.