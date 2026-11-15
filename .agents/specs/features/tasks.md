# Tasks Specification

## 1. Objetivo

Definir os requisitos funcionais e técnicos do módulo de gerenciamento de tarefas.

O módulo permitirá criar, consultar, atualizar, concluir, cancelar, excluir e restaurar tarefas.

## 2. Entidade Task

Campos:

Campo

Tipo

Obrigatório

id

BIGINT

Sim

userId

BIGINT

Sim

categoryId

BIGINT

Não

title

VARCHAR

Sim

description

TEXT

Não

status

ENUM

Sim

priority

ENUM

Sim

dueDate

DATETIME

Não

completedAt

DATETIME

Não

createdAt

DATETIME

Sim

updatedAt

DATETIME

Sim

deletedAt

DATETIME

Não

## 3. Valores permitidos

### Status

-   PENDING
-   IN_PROGRESS
-   COMPLETED
-   CANCELLED

### Prioridade

-   LOW
-   MEDIUM
-   HIGH
-   URGENT

## 4. Criar tarefa

### Endpoint

`POST /api/v1/tasks`

### Request

json

```
{
  "title": "Study Spring Boot",
  "description": "Study Spring Security and JPA",
  "categoryId": 1,
  "priority": "HIGH",
  "dueDate": "2026-11-15T23:59:00Z"
}

```

### Response — 201

json

```
{
  "id": 10,
  "title": "Study Spring Boot",
  "description": "Study Spring Security and JPA",
  "status": "PENDING",
  "priority": "HIGH",
  "category": {
    "id": 1,
    "name": "Studies",
    "color": "#6366F1"
  },
  "dueDate": "2026-11-15T23:59:00Z",
  "completedAt": null,
  "createdAt": "2026-11-10T12:00:00Z",
  "updatedAt": "2026-11-10T12:00:00Z"
}

```

### Regras

-   Título obrigatório (1 a 150 caracteres após remoção de espaços excedentes).
-   Descrição opcional (até 5000 caracteres).
-   Categoria opcional.
-   Prioridade obrigatória, com padrão MEDIUM se omitida.
-   Status inicial PENDING.
-   O usuário será identificado pelo contexto autenticado.
-   Categoria deverá pertencer ao usuário autenticado.
-   Criar registro de atividade.

## 5. Consultar tarefa

### Endpoint

`GET /api/v1/tasks/{id}`

### Regras

-   Consultar somente tarefas do usuário autenticado.
-   Não retornar tarefas excluídas logicamente.
-   Retornar 404 quando não existir ou não pertencer ao usuário.

## 6. Listar tarefas

### Endpoint

`GET /api/v1/tasks`

Parâmetros:

Parâmetro

Descrição

page

Página

size

Quantidade por página

sort

Ordenação

status

Status

priority

Prioridade

categoryId

Categoria

startDate

Início do período

endDate

Fim do período

search

Pesquisa por texto

deleted

Filtrar Lixeira (`true` para listar tarefas excluídas logicamente; padrão `false`)

Exemplo:

`GET /api/v1/tasks?status=PENDING&priority=HIGH&page=0&size=10`

Exemplo Lixeira:

`GET /api/v1/tasks?deleted=true&page=0&size=10`

### Regras

-   Filtros combináveis.
-   Pesquisa por título e descrição.
-   Quando `deleted=false` (ou omitido), retornar apenas tarefas não excluídas; quando `deleted=true`, retornar apenas tarefas excluídas logicamente (Lixeira).
-   Apenas tarefas do usuário autenticado.
-   Paginação no backend (tamanho padrão `10`, máximo `100`).
-   Ordenação permitida por campos definidos.
-   Ordenação padrão por createdAt DESC.

## 7. Atualizar tarefa

### Endpoint

`PATCH /api/v1/tasks/{id}`

### Request

json

```
{
  "title": "Study Spring Boot Advanced",
  "description": "Study Spring Security",
  "priority": "URGENT",
  "categoryId": 2,
  "dueDate": "2026-11-20T23:59:00Z"
}

```

### Regras

-   Atualizar somente os campos enviados.
-   Não permitir alteração de userId.
-   Validar categoria.
-   Atualizar updatedAt.
-   Registrar alterações importantes no histórico.
-   Preservar os demais campos.

Para remover uma categoria, utilizar um valor explícito null em categoryId.

Campos não enviados deverão permanecer inalterados.

## 8. Alterar status

### Endpoint

`PATCH /api/v1/tasks/{id}/status`

### Request

json

```
{
  "status": "COMPLETED"
}

```

### Regras

-   Validar o status.
-   Atualizar completedAt ao concluir.
-   Limpar completedAt ao reabrir.
-   Atualizar updatedAt.
-   Registrar atividade.
-   Não permitir transições inválidas.

Transições previstas:

Estado atual

Próximo estado

PENDING

IN_PROGRESS

PENDING

COMPLETED

PENDING

CANCELLED

IN_PROGRESS

PENDING

IN_PROGRESS

COMPLETED

IN_PROGRESS

CANCELLED

COMPLETED

PENDING

COMPLETED

IN_PROGRESS

CANCELLED

PENDING

CANCELLED

IN_PROGRESS

As transições serão permitidas somente quando respeitarem as regras de negócio.

## 9. Concluir tarefa

A conclusão poderá utilizar o endpoint de alteração de status.

Ao concluir:

-   Status será COMPLETED.
-   completedAt receberá o instante atual em UTC.
-   updatedAt será atualizado.
-   Um evento de conclusão será registrado.

## 10. Excluir tarefa

### Endpoint

`DELETE /api/v1/tasks/{id}`

### Response

`204 No Content`

### Regras

-   Utilizar Soft Delete.
-   Preencher deletedAt.
-   Atualizar updatedAt.
-   Registrar exclusão.
-   Não remover fisicamente o registro.
-   Não permitir acesso pelas consultas comuns.

## 11. Restaurar tarefa

### Endpoint

`POST /api/v1/tasks/{id}/restore`

### Regras

-   Permitir restauração somente pelo proprietário.
-   Limpar deletedAt.
-   Atualizar updatedAt.
-   Registrar restauração.
-   Caso a categoria anteriormente associada à tarefa tenha sido excluída logicamente, a tarefa já terá sido desassociada (`categoryId = null`) e será restaurada sem categoria.

## 12. Histórico

As operações importantes deverão gerar registros em ActivityLogs.

Eventos previstos:

-   TASK_CREATED.
-   TASK_UPDATED.
-   TASK_STATUS_CHANGED.
-   TASK_COMPLETED.
-   TASK_CANCELLED.
-   TASK_DELETED.
-   TASK_RESTORED.

As alterações relevantes poderão armazenar metadados com valores anteriores e novos.

## 13. Indicador de atraso

Uma tarefa será considerada atrasada quando:

-   Possuir dueDate.
-   dueDate for anterior ao instante atual.
-   Status não for COMPLETED.
-   Status não for CANCELLED.

O cálculo será realizado pelo backend.

## 14. Testes de aceitação

-   Criar tarefa.
-   Consultar tarefa.
-   Listar tarefas.
-   Atualizar tarefa.
-   Alterar status.
-   Concluir tarefa.
-   Reabrir tarefa.
-   Cancelar tarefa.
-   Excluir tarefa.
-   Restaurar tarefa.
-   Filtrar por status.
-   Filtrar por prioridade.
-   Filtrar por categoria.
-   Filtrar por período.
-   Pesquisar por texto.
-   Validar paginação.
-   Impedir acesso a tarefas de outros usuários.
-   Impedir associação com categoria de outro usuário.

## 15. Critérios de conclusão

-   CRUD completo.
-   Filtros funcionais.
-   Paginação funcional.
-   Soft Delete funcional.
-   Histórico integrado.
-   Isolamento multiusuário.
-   Testes automatizados aprovados.