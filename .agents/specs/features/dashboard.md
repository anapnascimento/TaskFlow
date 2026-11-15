# Dashboard Specification

## 1. Objetivo

Definir os requisitos funcionais e técnicos do dashboard e do histórico de atividades do TaskFlow.

O dashboard apresentará indicadores e informações consolidadas sobre as tarefas do usuário autenticado.

## 2. Funcionalidades

O dashboard deverá apresentar:

-   Total de tarefas ativas.
-   Tarefas pendentes.
-   Tarefas em andamento.
-   Tarefas concluídas.
-   Tarefas atrasadas.
-   Taxa de conclusão.
-   Gráfico de conclusão de tarefas.
-   Distribuição por prioridade.
-   Distribuição por status.
-   Histórico recente de atividades.

## 3. Endpoint principal

### Endpoint

`GET /api/v1/dashboard`

### Autenticação

Obrigatória.

### Parâmetros opcionais

Parâmetro

Descrição

startDate

Data inicial

endDate

Data final

Os filtros de período deverão afetar os indicadores temporais e gráficos conforme a definição de cada métrica.

Os totais gerais poderão ser apresentados separadamente dos indicadores filtrados.

## 4. Resposta

### Response — 200

json

```
{
  "summary": {
    "totalTasks": 25,
    "pendingTasks": 8,
    "inProgressTasks": 5,
    "completedTasks": 9,
    "cancelledTasks": 3,
    "overdueTasks": 2,
    "completionRate": 36.0
  },
  "statusDistribution": [
    {
      "status": "PENDING",
      "count": 8
    },
    {
      "status": "IN_PROGRESS",
      "count": 5
    },
    {
      "status": "COMPLETED",
      "count": 9
    },
    {
      "status": "CANCELLED",
      "count": 3
    }
  ],
  "priorityDistribution": [
    {
      "priority": "LOW",
      "count": 4
    },
    {
      "priority": "MEDIUM",
      "count": 10
    },
    {
      "priority": "HIGH",
      "count": 8
    },
    {
      "priority": "URGENT",
      "count": 3
    }
  ],
  "completionHistory": [
    {
      "date": "2026-11-08",
      "completed": 2
    },
    {
      "date": "2026-11-09",
      "completed": 1
    },
    {
      "date": "2026-11-10",
      "completed": 3
    }
  ],
  "recentActivities": [
    {
      "id": 101,
      "action": "TASK_COMPLETED",
      "description": "Task Study Spring Boot completed",
      "taskId": 10,
      "createdAt": "2026-11-10T12:00:00Z"
    }
  ]
}

```

## 5. Indicadores

### Total de tarefas

Quantidade de tarefas não excluídas logicamente, incluindo tarefas canceladas.

### Tarefas pendentes

Quantidade de tarefas com status PENDING.

### Tarefas em andamento

Quantidade de tarefas com status IN_PROGRESS.

### Tarefas concluídas

Quantidade de tarefas com status COMPLETED.

### Tarefas canceladas

Quantidade de tarefas com status CANCELLED.

### Tarefas atrasadas

Quantidade de tarefas que:

-   Possuem prazo definido.
-   Possuem prazo anterior ao instante atual.
-   Não estão concluídas.
-   Não estão canceladas.
-   Não foram excluídas logicamente.

### Taxa de conclusão

A taxa de conclusão será calculada pela seguinte fórmula:

Taxa de conclusão = tarefas concluídas / total de tarefas × 100.

Para evitar distorções, tarefas canceladas serão excluídas do denominador.

Quando não houver tarefas elegíveis, retornar 0.

A fórmula definitiva deverá ser aplicada consistentemente nos gráficos e indicadores.

## 6. Distribuição por status

Apresentar a quantidade de tarefas por status.

Utilizar Recharts no frontend.

Visualizações possíveis:

-   Gráfico de pizza.
-   Gráfico de barras.

## 7. Distribuição por prioridade

Apresentar a quantidade de tarefas agrupadas por prioridade.

Prioridades:

-   LOW.
-   MEDIUM.
-   HIGH.
-   URGENT.

## 8. Histórico de conclusão

Apresentar a quantidade de tarefas concluídas agrupadas por dia.

A consulta deverá considerar completedAt.

O período padrão será definido inicialmente como os últimos 7 dias, incluindo o dia atual.

Dias sem conclusões deverão ser retornados com valor zero para manter a continuidade do gráfico.

O agrupamento deverá respeitar o fuso horário configurado para o usuário (`user.timezone`, padrão `America/Sao_Paulo`).

## 9. Atividades recentes e histórico paginado

### Resumo no Dashboard

O campo `recentActivities` do `GET /api/v1/dashboard` apresenta os 10 eventos mais recentes do usuário autenticado ordenados por data decrescente.

Eventos previstos:

-   Criação de tarefa.
-   Atualização.
-   Alteração de status.
-   Conclusão.
-   Cancelamento.
-   Exclusão.
-   Restauração.
-   Alterações de categoria, quando aplicável.

Não retornar atividades pertencentes a outros usuários.

### Endpoint de histórico completo

`GET /api/v1/activity-logs`

Permite consultar o histórico completo de atividades do usuário autenticado (retenção indefinida) com paginação no backend.

Parâmetros:

-   `page`: página (iniciando em `0`).
-   `size`: quantidade por página (padrão `10`, máximo `100`).
-   `sort`: ordenação (padrão `createdAt,desc`).

Response — 200:

```json
{
  "content": [
    {
      "id": 101,
      "action": "TASK_COMPLETED",
      "description": "Task Study Spring Boot completed",
      "taskId": 10,
      "metadata": {
        "previousStatus": "IN_PROGRESS",
        "newStatus": "COMPLETED"
      },
      "createdAt": "2026-11-10T12:00:00Z"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

## 10. Performance

As consultas do dashboard deverão ser executadas no backend.

Regras:

-   Evitar carregar todas as tarefas para realizar cálculos em memória.
-   Utilizar consultas agregadas.
-   Evitar N+1 queries.
-   Considerar índices existentes.
-   Retornar somente os dados necessários para a interface.

Cache poderá ser considerado futuramente, caso exista necessidade comprovada.

## 11. Testes de aceitação

-   Dashboard de usuário sem tarefas.
-   Dashboard com tarefas pendentes.
-   Dashboard com tarefas concluídas.
-   Dashboard com tarefas atrasadas.
-   Distribuição por status.
-   Distribuição por prioridade.
-   Taxa de conclusão.
-   Histórico de conclusão.
-   Atividades recentes.
-   Filtros de período.
-   Isolamento entre usuários.
-   Exclusão lógica não contabilizada.

## 12. Critérios de conclusão

-   Endpoint consolidado funcional.
-   Indicadores calculados corretamente.
-   Gráficos com dados reais.
-   Histórico funcional.
-   Filtros temporais funcionais.
-   Consultas otimizadas.
-   Testes automatizados aprovados.