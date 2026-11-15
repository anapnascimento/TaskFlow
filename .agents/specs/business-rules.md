# Business Rules

## 1. Objetivo

Este documento define as regras de negócio do TaskFlow.

As regras devem ser respeitadas pelo backend e refletidas adequadamente no frontend.

O backend será sempre a fonte de verdade para validação e execução das regras de negócio.

## 2. Usuários

### BR-USER-001 — Cadastro

Um usuário deve possuir uma conta para acessar as funcionalidades privadas do sistema.

### BR-USER-002 — E-mail único

Cada endereço de e-mail deverá pertencer a somente uma conta.

A comparação deverá considerar uma política consistente de normalização de e-mail.

### BR-USER-003 — Isolamento

Cada usuário somente poderá acessar e modificar seus próprios dados.

### BR-USER-004 — Conta

Um usuário poderá visualizar e atualizar seus dados de perfil (`name` e `timezone`).

### BR-USER-005 — Exclusão de conta

A exclusão da conta deverá utilizar Soft Delete (`deleted_at`), revogando imediatamente todas as sessões ativas (Refresh Tokens) do usuário e impedindo novos logins ou renovações de token.

## 3. Tarefas

### BR-TASK-001 — Propriedade

Toda tarefa deverá estar associada a um usuário.

### BR-TASK-002 — Campos obrigatórios

Uma tarefa deverá possuir, no mínimo:

-   Título.
-   Usuário responsável.
-   Status.
-   Prioridade.

A descrição e o prazo serão opcionais.

### BR-TASK-003 — Título

O título deverá possuir conteúdo válido após a remoção de espaços excedentes.

Limites de tamanho deverão ser definidos na especificação técnica.

### BR-TASK-004 — Criação

Uma tarefa deverá ser criada inicialmente com status PENDING, salvo quando houver uma regra explícita diferente.

### BR-TASK-005 — Conclusão

Uma tarefa poderá ser marcada como concluída.

A conclusão deverá registrar a data e hora em que ocorreu.

### BR-TASK-006 — Reabertura

Uma tarefa concluída poderá voltar a um status não concluído.

Quando reaberta, a data de conclusão deverá ser atualizada adequadamente, preservando o histórico de atividades.

### BR-TASK-007 — Exclusão

A exclusão deverá utilizar Soft Delete.

Registros excluídos logicamente não deverão aparecer nas consultas comuns.

### BR-TASK-008 — Prazo

Uma tarefa poderá possuir uma data de vencimento.

Tarefas vencidas e não concluídas deverão ser consideradas atrasadas.

### BR-TASK-009 — Atualização

O usuário somente poderá atualizar tarefas pertencentes à sua própria conta.

## 4. Status

Os status disponíveis serão:

Código

Descrição

PENDING

Pendente

IN_PROGRESS

Em andamento

COMPLETED

Concluída

CANCELLED

Cancelada

A transição entre status deverá ser validada pelo backend.

Uma tarefa cancelada não deverá ser considerada concluída.

Uma tarefa concluída poderá ser reaberta.

## 5. Prioridades

As prioridades disponíveis serão:

Código

Descrição

LOW

Baixa

MEDIUM

Média

HIGH

Alta

URGENT

Urgente

Toda tarefa deverá possuir uma prioridade.

A prioridade não deverá alterar automaticamente o status da tarefa.

## 6. Categorias

### BR-CAT-001 — Propriedade

Cada categoria deverá pertencer a um usuário.

### BR-CAT-002 — Organização

Um usuário poderá criar múltiplas categorias.

### BR-CAT-003 — Associação

Uma tarefa poderá estar associada a uma categoria.

### BR-CAT-004 — Isolamento

Um usuário não poderá associar tarefas a categorias pertencentes a outro usuário.

### BR-CAT-005 — Exclusão

A exclusão lógica de uma categoria não deverá provocar exclusão automática das tarefas associadas.

Na mesma transação da exclusão lógica da categoria, todas as tarefas associadas (ativas e excluídas logicamente na Lixeira) deverão ser desassociadas (`category_id = NULL`).

## 7. Filtros e pesquisa

O usuário poderá filtrar tarefas por:

-   Status.
-   Prioridade.
-   Categoria.
-   Período.
-   Texto.

Os filtros deverão poder ser combinados.

As consultas deverão considerar somente tarefas pertencentes ao usuário autenticado.

A paginação deverá ser executada no backend.

## 8. Dashboard

Os indicadores deverão considerar exclusivamente os dados do usuário autenticado.

Indicadores:

-   Total de tarefas ativas.
-   Tarefas pendentes.
-   Tarefas em andamento.
-   Tarefas concluídas.
-   Tarefas atrasadas.

### Definição de tarefa atrasada

Uma tarefa será considerada atrasada quando:

-   Possuir prazo definido.
-   Seu prazo tiver expirado.
-   Seu status não for COMPLETED nem CANCELLED.

A interpretação do prazo deverá respeitar o fuso horário definido para a aplicação.

### Consistência

Os indicadores deverão ser calculados a partir dos dados persistidos, não de valores enviados pelo frontend.

## 9. Histórico de atividades

O sistema deverá registrar eventos importantes, como:

-   Criação de tarefa.
-   Atualização de tarefa.
-   Alteração de status.
-   Conclusão.
-   Exclusão.
-   Restauração, caso implementada.

Os registros deverão identificar o usuário responsável e a data da atividade.

## 10. Soft Delete

A exclusão lógica deverá preservar os dados originais.

Registros excluídos não deverão participar das consultas comuns.

A interface deverá disponibilizar uma visualização/filtro de Lixeira (`deleted=true`) para consulta e restauração de tarefas excluídas pelo próprio usuário.

A restauração de tarefas e categorias deverá respeitar as permissões do usuário proprietário; para categorias, a restauração só será permitida caso não exista outra categoria ativa do usuário com o mesmo nome normalizado.

## 11. Notificações

O sistema deverá gerar notificações in-app no sino do topo da interface relacionadas a:

-   Tarefas próximas do vencimento: tarefas ativas, não concluídas e não canceladas com prazo (`due_date`) nas próximas 24 horas (`DUE_SOON`).
-   Tarefas atrasadas: tarefas ativas, não concluídas e não canceladas com prazo vencido (`OVERDUE`).

As notificações serão estritamente in-app (sem envio de e-mail) e deduplicadas por `(taskId, alertType)` para evitar repetição de alertas já lidos ou dispensados pelo usuário.

## 12. Regras gerais

-   O backend é responsável pela validação das regras.
-   Nenhuma operação poderá violar o isolamento entre usuários.
-   Alterações importantes deverão gerar histórico quando aplicável.
-   Datas deverão possuir interpretação consistente.
-   Operações críticas deverão preservar a integridade dos dados.

## 13. Definições consolidadas

As seguintes definições foram fechadas e valem para todo o projeto:

-   **Limite de tarefas por usuário**: sem limite máximo de tarefas por usuário; listagens utilizam paginação no backend limitada a `size <= 100` (padrão `10`).
-   **Limites de tamanho dos campos**: `user.name` (100 caracteres), `user.email` (255 caracteres), `user.timezone` (64 caracteres), `task.title` (150 caracteres), `task.description` (5000 caracteres), `category.name` (50 caracteres) e `category.color` (7 caracteres, formato `#RRGGBB`).
-   **Compartilhamento**: sem compartilhamento de tarefas entre usuários (isolamento total por usuário autenticado).
-   **Política de exclusão de categorias**: Soft Delete desassociando (`category_id = NULL`) todas as tarefas vinculadas (ativas e na lixeira) na mesma transação; unicidade de nome garantida apenas entre categorias ativas (`deleted_at IS NULL`).
-   **Política de restauração de tarefas**: via filtro/visualização de Lixeira (`deleted=true`) e endpoint `POST /api/v1/tasks/{id}/restore`.
-   **Notificações**: in-app no sino do topo (sem e-mail) para vencimento em até 24h (`DUE_SOON`) e atrasadas (`OVERDUE`), deduplicadas por `(taskId, alertType)`.
-   **Fuso horário**: instantes persistidos em UTC (`DATETIME`) e fuso configurável por usuário (`users.timezone`, padrão `America/Sao_Paulo`, enviado pelo navegador no cadastro e editável no perfil) utilizado nos agrupamentos diários do Dashboard.
-   **Retenção do histórico de atividades**: retenção indefinida em `ActivityLogs`, com consulta paginada via `GET /api/v1/activity-logs` e exibição dos 10 eventos mais recentes no Dashboard.