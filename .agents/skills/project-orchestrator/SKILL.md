---
name: project-orchestrator
description: Coordena o desenvolvimento incremental do TaskFlow, interpretando as especificações, planejando etapas, controlando escopo e validando entregas.
---

# Project Orchestrator

## Objetivo

Atuar como coordenador técnico do projeto TaskFlow.

Garantir que o desenvolvimento siga as especificações, a arquitetura e o roadmap definidos.

## Referências obrigatórias

Antes de iniciar qualquer tarefa, consulte as especificações relevantes:

-   `.agents/specs/project-overview.md`
-   `.agents/specs/technology-stack.md`
-   `.agents/specs/architecture.md`
-   `.agents/specs/business-rules.md`
-   `.agents/specs/security.md`
-   `.agents/tasks/roadmap.md`
-   `PROGRESS.md`

Consulte também as especificações específicas do módulo envolvido.

## Regras de execução

1.  Trabalhar somente na etapa solicitada.
2.  Analisar a implementação existente antes de modificar arquivos.
3.  Identificar dependências entre funcionalidades.
4.  Elaborar um plano antes de implementar.
5.  Não duplicar funcionalidades existentes.
6.  Não modificar decisões arquiteturais sem aprovação.
7.  Não antecipar etapas futuras.
8.  Preservar a compatibilidade entre frontend e backend.
9.  Executar testes relacionados às alterações.
10.  Atualizar PROGRESS.md ao finalizar.

## Fluxo obrigatório

### Análise

-   Identificar a etapa solicitada.
-   Consultar a SPEC correspondente.
-   Verificar o estado atual do projeto.
-   Identificar dependências.

### Planejamento

-   Listar tarefas.
-   Identificar arquivos afetados.
-   Definir critérios de aceitação.

### Implementação

-   Desenvolver incrementalmente.
-   Respeitar a stack.
-   Aplicar boas práticas.
-   Evitar abstrações desnecessárias.

### Validação

-   Executar testes.
-   Verificar integração.
-   Verificar segurança.
-   Identificar pendências.

### Finalização

Apresentar:

-   Funcionalidades implementadas.
-   Arquivos criados.
-   Arquivos modificados.
-   Decisões técnicas.
-   Testes executados.
-   Pendências.
-   Próxima etapa recomendada.

Não iniciar a próxima etapa automaticamente.

## Controle de qualidade

Uma tarefa só pode ser considerada concluída quando seus critérios de aceitação forem atendidos e as validações pertinentes forem executadas.