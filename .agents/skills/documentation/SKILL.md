---
name: documentation
description: Mantém a documentação técnica, especificações (.agents/specs), ADRs (DECISIONS.md), progresso (PROGRESS.md) e contratos OpenAPI/Swagger do TaskFlow atualizados e consistentes.
---

# Documentation

## 1. Objetivo

Garantir que toda a documentação do TaskFlow permaneça sincronizada com a arquitetura, as decisões técnicas, os contratos de API e o andamento real das etapas do projeto.

## 2. Documentos principais do repositório

-   `README.md`: visão geral, stack, arquitetura resumida, estrutura de diretórios e status do projeto.
-   `ARCHITECTURE.md`: organização de camadas (`com.taskflow` no backend e estrutura modular no frontend), banco, segurança e princípios arquiteturais.
-   `DECISIONS.md`: registro cronológico de decisões técnicas e arquiteturais (ADRs).
-   `PROGRESS.md`: checklist atualizado de documentação e fases de desenvolvimento.
-   `.agents/specs/`: especificações técnicas, regras de negócio, modelagem de banco, contratos REST, segurança e features.
-   `.agents/tasks/`: roadmap (`roadmap.md`) e detalhamentos de fases.

## 3. Regras de manutenção documental

-   Manter consistência estrita entre `README.md`, `ARCHITECTURE.md`, `DECISIONS.md`, `.agents/specs/` e o código implementado.
-   Utilizar sempre o nome **TaskFlow** e o pacote base **`com.taskflow`**.
-   Referenciar caminhos padronizados em `.agents/specs/`, `.agents/skills/` e `.agents/tasks/`.
-   Registrar novas decisões arquiteturais em `DECISIONS.md` com identificador sequencial (`ADR-XXX`), status (`Proposed`, `Accepted`, `Rejected`, `Superseded`), contexto, decisão, justificativa e consequências. Nunca remover ADRs antigas ao substituí-las.
-   Atualizar `PROGRESS.md` e o status das fases em `.agents/tasks/roadmap.md` ao concluir cada etapa validada.

## 4. Documentação de API (OpenAPI / Swagger)

-   Documentar todos os endpoints sob `/api/v1` via Swagger / OpenAPI no backend.
-   Garantir que esquemas de request/response, códigos HTTP, paginação, Problem Details (RFC 9457) e requisitos de autenticação Bearer estejam alinhados com `.agents/specs/api-contracts.md` e `.agents/specs/features/`.
