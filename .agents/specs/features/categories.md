# Categories Specification

## 1. Objetivo

Definir os requisitos funcionais e técnicos do módulo de categorias.

O módulo permitirá que cada usuário organize suas tarefas através de categorias personalizadas.

## 2. Entidade Category

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

name

VARCHAR

Sim

color

VARCHAR

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

## 3. Criar categoria

### Endpoint

`POST /api/v1/categories`

### Request

json

```
{
  "name": "Studies",
  "color": "#6366F1"
}

```

### Response — 201

json

```
{
  "id": 1,
  "name": "Studies",
  "color": "#6366F1",
  "createdAt": "2026-11-10T12:00:00Z",
  "updatedAt": "2026-11-10T12:00:00Z"
}

```

### Regras

-   Nome obrigatório (1 a 50 caracteres após remoção de espaços excedentes).
-   Nome não poderá ser vazio.
-   Nome deverá ser normalizado.
-   Cor opcional (`#RRGGBB`, 7 caracteres).
-   O usuário será identificado pelo contexto autenticado.
-   Categorias deverão pertencer exclusivamente ao usuário.
-   Não permitir nomes duplicados entre categorias ativas (`deleted_at IS NULL`) para o mesmo usuário, considerando a política de normalização.

## 4. Consultar categoria

### Endpoint

`GET /api/v1/categories/{id}`

### Regras

-   Retornar somente categorias do usuário autenticado.
-   Não retornar categorias excluídas.
-   Retornar 404 quando não existir ou não pertencer ao usuário.

## 5. Listar categorias

### Endpoint

`GET /api/v1/categories`

### Regras

-   Listar somente categorias do usuário autenticado.
-   Excluir registros removidos logicamente.
-   Ordenar por nome ASC.
-   Permitir consulta da quantidade de tarefas associadas, se necessário.

Exemplo:

json

```
{
  "content": [
    {
      "id": 1,
      "name": "Studies",
      "color": "#6366F1",
      "taskCount": 5
    },
    {
      "id": 2,
      "name": "Work",
      "color": "#10B981",
      "taskCount": 8
    }
  ]
}

```

## 6. Atualizar categoria

### Endpoint

`PATCH /api/v1/categories/{id}`

### Request

json

```
{
  "name": "Professional Studies",
  "color": "#8B5CF6"
}

```

### Regras

-   Atualizar somente os campos enviados.
-   Validar nome.
-   Validar cor.
-   Não permitir nomes duplicados entre categorias ativas (`deleted_at IS NULL`) para o mesmo usuário.
-   Atualizar updatedAt.
-   Registrar alterações relevantes quando aplicável.

## 7. Excluir categoria

### Endpoint

`DELETE /api/v1/categories/{id}`

### Response

`204 No Content`

### Regras

-   Utilizar Soft Delete.
-   Preencher deletedAt.
-   Atualizar updatedAt.
-   Não excluir tarefas associadas.
-   Desassociar todas as tarefas vinculadas (ativas e excluídas logicamente na Lixeira) da categoria excluída (`category_id = NULL`).
-   Registrar a operação quando aplicável.

A desassociação deverá ocorrer na mesma transação da exclusão lógica da categoria.

As tarefas permanecerão disponíveis e sem categoria.

## 8. Restaurar categoria

### Endpoint

`POST /api/v1/categories/{id}/restore`

### Regras

-   Permitir somente ao proprietário.
-   Limpar deletedAt.
-   Atualizar updatedAt.
-   Validar se não existe conflito com outra categoria ativa de mesmo nome.
-   Não reassociar automaticamente tarefas antigas.

## 9. Validação de cores

A cor será representada por hexadecimal.

Formato previsto:

`#RRGGBB`

Exemplos válidos:

-   #6366F1
-   #10B981
-   #EF4444

Valores inválidos deverão ser rejeitados.

## 10. Associação com tarefas

Uma tarefa poderá possuir uma categoria ou nenhuma.

Regras:

-   A categoria deverá pertencer ao mesmo usuário.
-   A exclusão da categoria deverá remover as associações.
-   Tarefas sem categoria continuarão disponíveis.
-   Não permitir associação a categorias excluídas.

## 11. Testes de aceitação

-   Criar categoria.
-   Consultar categoria.
-   Listar categorias.
-   Atualizar categoria.
-   Excluir categoria.
-   Restaurar categoria.
-   Validar nome duplicado.
-   Validar cor.
-   Associar categoria a tarefa.
-   Impedir associação entre usuários diferentes.
-   Verificar desassociação após exclusão.

## 12. Critérios de conclusão

-   CRUD funcional.
-   Validações implementadas.
-   Soft Delete funcional.
-   Relacionamento com tarefas funcional.
-   Isolamento multiusuário.
-   Testes automatizados aprovados.