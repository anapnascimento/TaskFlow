# API Contracts

## 1. Objetivo

Definir os padrões de comunicação entre frontend e backend do TaskFlow.

A API deverá seguir o estilo REST, utilizando HTTP e JSON.

Este documento estabelece os contratos compartilhados por todas as funcionalidades.

## 2. Convenções gerais

### Base URL

Desenvolvimento:

`http://localhost:8080/api/v1`

Produção:

`https://api.example.com/api/v1`

### Formato

-   Content-Type: application/json
-   Accept: application/json
-   Codificação: UTF-8.
-   Datas: ISO 8601.
-   Instantes: UTC, utilizando sufixo Z.

### Convenções de nomenclatura

-   Endpoints em inglês.
-   Recursos no plural.
-   JSON em camelCase.
-   Enums em UPPER_SNAKE_CASE.
-   Identificadores representados como números inteiros.

Exemplo:

`GET /api/v1/tasks`

## 3. Métodos HTTP

Método

Finalidade

GET

Consultar recursos

POST

Criar recursos ou executar operações específicas

PUT

Substituir um recurso

PATCH

Atualizar parcialmente um recurso

DELETE

Excluir logicamente um recurso

As operações deverão respeitar a semântica HTTP.

## 4. Códigos HTTP

Código

Significado

200

Operação realizada

201

Recurso criado

204

Operação concluída sem conteúdo

400

Requisição inválida

401

Não autenticado

403

Acesso proibido

404

Recurso não encontrado

409

Conflito

422

Erro de validação semântica, quando adotado

429

Limite de requisições excedido

500

Erro interno

Utilizar os códigos de maneira consistente.

## 5. Formato de resposta

As respostas deverão utilizar JSON.

Exemplo de recurso:

json

```
{
  "id": 1,
  "title": "Study Spring Boot",
  "status": "PENDING",
  "priority": "HIGH"
}

```

Não é obrigatório envolver todas as respostas em uma propriedade data.

A estrutura deverá ser consistente por tipo de endpoint.

## 6. Formato de erros

Utilizar um formato padronizado baseado em Problem Details (RFC 9457), com extensões quando necessário.

Exemplo:

json

```
{
  "type": "about:blank",
  "title": "Validation Error",
  "status": 400,
  "detail": "One or more fields are invalid.",
  "instance": "/api/v1/tasks",
  "code": "VALIDATION_ERROR",
  "timestamp": "2026-11-10T12:00:00Z",
  "errors": [
    {
      "field": "title",
      "message": "Title is required."
    }
  ]
}

```

Regras:

-   Não retornar stack traces.
-   Não expor informações internas.
-   Utilizar códigos de erro estáveis.
-   Mensagens devem ser compreensíveis.
-   Erros de validação devem identificar os campos quando apropriado.

## 7. Paginação

Endpoints de listagem deverão suportar paginação.

Parâmetros:

-   page: número da página, começando em zero.
-   size: quantidade de registros por página.
-   sort: campo e direção da ordenação.

Exemplo:

`GET /api/v1/tasks?page=0&size=10&sort=createdAt,desc`

Resposta:

json

```
{
  "content": [],
  "page": 0,
  "size": 10,
  "totalElements": 0,
  "totalPages": 0,
  "first": true,
  "last": true
}

```

Regras:

-   A paginação será realizada no backend.
-   O parâmetro `size` terá valor padrão `10` e limite máximo de `100` registros por página.
-   Ordenações deverão utilizar campos permitidos.
-   Consultas deverão respeitar o usuário autenticado.

## 8. Autenticação

Endpoints privados deverão exigir Access Token válido.

Formato:

`Authorization: Bearer <access_token>`

A renovação da sessão utilizará Refresh Token conforme definido em authentication.md.

O Refresh Token será transportado por cookie HttpOnly, conforme a estratégia de segurança.

## 9. Identificação do usuário

O backend deverá identificar o usuário através do contexto autenticado.

Não confiar em userId enviado pelo frontend para determinar a propriedade de recursos.

## 10. Filtros

Endpoints de listagem poderão aceitar filtros por query parameters.

Exemplo:

`GET /api/v1/tasks?status=PENDING&priority=HIGH`

Filtros combinados deverão utilizar lógica AND, salvo definição específica em contrário.

Parâmetros inválidos deverão retornar erro apropriado.

## 11. Datas

Utilizar ISO 8601.

Exemplo:

`2026-11-10T15:30:00Z`

Regras:

-   Instantes armazenados em UTC.
-   Backend responsável pela interpretação das datas.
-   Frontend responsável pela apresentação no fuso do usuário.
-   Não interpretar datas sem fuso como instantes UTC automaticamente.

Para campos que representem somente uma data civil, utilizar formato `YYYY-MM-DD`.

## 12. Segurança

-   Todos os endpoints privados deverão validar autenticação.
-   Todas as operações deverão validar autorização.
-   CORS deverá possuir origens autorizadas.
-   Dados sensíveis não deverão ser retornados.
-   Requisições deverão possuir validação de entrada.
-   Não utilizar informações enviadas pelo cliente como prova de autorização.

## 13. Versionamento

A versão inicial será:

`/api/v1`

Alterações incompatíveis deverão utilizar uma nova versão.

Alterações compatíveis deverão preservar os contratos existentes sempre que possível.

## 14. Documentação

Utilizar Swagger/OpenAPI para documentar:

-   Endpoints.
-   Parâmetros.
-   DTOs.
-   Códigos HTTP.
-   Respostas.
-   Requisitos de autenticação.

A documentação gerada deverá permanecer consistente com os contratos definidos nas SPECs.

## 15. Visão geral dos endpoints

### Auth (`/api/v1/auth`)

-   `POST /api/v1/auth/register` — Cadastro de usuário (público).
-   `POST /api/v1/auth/login` — Autenticação com emissão de Access Token e Refresh Token em cookie HttpOnly (público).
-   `POST /api/v1/auth/refresh` — Renovação de sessão com rotação de Refresh Token (público via cookie).
-   `POST /api/v1/auth/logout` — Encerramento e revogação da sessão atual (autenticado ou via cookie).
-   `GET /api/v1/auth/me` — Consulta do usuário autenticado.
-   `POST /api/v1/auth/password/forgot` — Solicitação de recuperação de senha (público).
-   `POST /api/v1/auth/password/reset` — Redefinição de senha com token de uso único (público).

### Users (`/api/v1/users`)

-   `PATCH /api/v1/users/me` — Atualização de perfil (`name` e `timezone`).
-   `PATCH /api/v1/users/me/password` — Alteração de senha do usuário autenticado.
-   `DELETE /api/v1/users/me` — Exclusão lógica da conta (`Soft Delete`) e revogação de todas as sessões.

### Tasks (`/api/v1/tasks`)

-   `POST /api/v1/tasks` — Criar tarefa.
-   `GET /api/v1/tasks` — Listar tarefas paginadas com filtros (inclui `deleted=true` para Lixeira).
-   `GET /api/v1/tasks/{id}` — Consultar tarefa.
-   `PATCH /api/v1/tasks/{id}` — Atualizar tarefa.
-   `PATCH /api/v1/tasks/{id}/status` — Alterar status da tarefa.
-   `DELETE /api/v1/tasks/{id}` — Excluir logicamente (`Soft Delete`).
-   `POST /api/v1/tasks/{id}/restore` — Restaurar tarefa da Lixeira.

### Categories (`/api/v1/categories`)

-   `POST /api/v1/categories` — Criar categoria.
-   `GET /api/v1/categories` — Listar categorias ativas.
-   `GET /api/v1/categories/{id}` — Consultar categoria.
-   `PATCH /api/v1/categories/{id}` — Atualizar categoria.
-   `DELETE /api/v1/categories/{id}` — Excluir logicamente (`Soft Delete`) e desassociar tarefas.
-   `POST /api/v1/categories/{id}/restore` — Restaurar categoria.

### Dashboard e Activity Logs

-   `GET /api/v1/dashboard` — Indicadores consolidados, distribuições, histórico de conclusão e últimas 10 atividades.
-   `GET /api/v1/activity-logs` — Listagem paginada do histórico completo de atividades do usuário autenticado.

## 16. Princípios

-   Consistência.
-   Simplicidade.
-   Segurança.
-   Previsibilidade.
-   Compatibilidade.
-   Separação entre contratos e entidades internas.