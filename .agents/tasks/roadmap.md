# Development Roadmap

## 1. Objetivo

Definir as etapas de desenvolvimento do TaskFlow, organizando o trabalho de maneira incremental.

Cada etapa deverá possuir:

-   Objetivo.
-   Escopo.
-   Dependências.
-   Entregáveis.
-   Critérios de aceitação.

Uma etapa somente será considerada concluída quando seus critérios de aceitação forem atendidos.

## 2. Etapas do projeto

### Phase 01 — Planning and Architecture

Status: Completed

Objetivo: Definir a estrutura inicial, arquitetura e documentação do projeto.

Atividades:

-   Definir arquitetura.
-   Documentar stack tecnológica.
-   Definir regras de negócio iniciais.
-   Definir modelagem conceitual.
-   Definir estrutura de diretórios.
-   Criar documentação inicial.

Entregáveis:

-   Documentação arquitetural.
-   SPECs iniciais.
-   Roadmap.
-   Estrutura do repositório.

### Phase 02 — Environment Setup

Status: Completed

Dependências: Phase 01

Objetivo: Preparar o ambiente de desenvolvimento.

Atividades:

-   Criar projeto React com Vite.
-   Configurar TypeScript.
-   Configurar Tailwind CSS.
-   Configurar shadcn/ui.
-   Configurar React Router.
-   Configurar TanStack Query.
-   Configurar Zustand.
-   Configurar projeto Spring Boot.
-   Configurar Java 25.
-   Configurar Maven 3.9.16.
-   Configurar dependências do backend.
-   Criar Docker Compose.
-   Configurar MySQL.
-   Validar comunicação entre backend e banco.

Entregáveis:

-   Frontend inicial executável.
-   Backend inicial executável.
-   Banco de dados disponível.
-   Ambiente Docker funcional.

### Phase 03 — Database and Persistence

Status: Completed

Dependências: Phase 02

Objetivo: Implementar a estrutura inicial do banco de dados e a persistência.

Atividades:

-   Criar entidade User.
-   Criar entidade Task.
-   Criar entidade Category.
-   Criar entidade ActivityLog.
-   Definir relacionamentos.
-   Configurar JPA e Hibernate.
-   Configurar Flyway.
-   Criar migrations.
-   Implementar repositories.
-   Validar persistência.
-   Criar testes de integração.

Entregáveis:

-   Modelagem implementada.
-   Migrations versionadas.
-   Persistência funcional.

### Phase 04 — Authentication and Security

Status: Completed

Dependências: Phase 03

Objetivo: Implementar autenticação e segurança.

Atividades:

-   Implementar cadastro.
-   Implementar login.
-   Implementar hash de senhas.
-   Implementar JWT Access Token.
-   Implementar Refresh Token.
-   Implementar rotação de Refresh Token.
-   Implementar logout e revogação.
-   Configurar Spring Security.
-   Proteger endpoints.
-   Garantir isolamento entre usuários.
-   Criar testes de segurança.

Entregáveis:

-   Autenticação funcional.
-   Endpoints protegidos.
-   Sessões controladas.
-   Testes de segurança.

### Phase 05 — Task Management

Status: Not Started

Dependências: Phase 04

Objetivo: Implementar o gerenciamento de tarefas e categorias.

Atividades:

-   CRUD de tarefas.
-   CRUD de categorias.
-   Implementar prioridades.
-   Implementar status.
-   Implementar prazos.
-   Implementar validações.
-   Implementar filtros.
-   Implementar pesquisa.
-   Implementar paginação.
-   Garantir isolamento multiusuário.
-   Criar testes.

Entregáveis:

-   API de tarefas funcional.
-   API de categorias funcional.
-   Filtros e paginação.
-   Testes automatizados.

### Phase 06 — Dashboard and Activity History

Status: Not Started

Dependências: Phase 05

Objetivo: Implementar indicadores e histórico de produtividade.

Atividades:

-   Total de tarefas.
-   Tarefas pendentes.
-   Tarefas em andamento.
-   Tarefas concluídas.
-   Tarefas atrasadas.
-   Histórico de atividades.
-   Consultas agregadas.
-   Testes dos indicadores.

Entregáveis:

-   API de dashboard.
-   Histórico de atividades.
-   Indicadores validados.

### Phase 07 — Backend Testing and Quality

Status: Not Started

Dependências: Phases 04, 05 e 06

Objetivo: Validar a qualidade e confiabilidade do backend.

Atividades:

-   Revisar testes unitários.
-   Revisar testes de integração.
-   Validar cenários de autenticação.
-   Validar isolamento multiusuário.
-   Validar regras de negócio.
-   Configurar JaCoCo.
-   Corrigir problemas identificados.
-   Revisar tratamento de exceções.

Entregáveis:

-   Suíte de testes backend.
-   Relatório de cobertura.
-   Backend validado.

### Phase 08 — Frontend Foundation

Status: Not Started

Dependências: Phase 02

Objetivo: Construir a estrutura inicial do frontend.

Atividades:

-   Configurar estrutura de diretórios.
-   Configurar rotas.
-   Criar layouts.
-   Configurar componentes reutilizáveis.
-   Configurar tema visual.
-   Configurar formulários.
-   Configurar schemas Zod.
-   Configurar stores Zustand.
-   Configurar TanStack Query.
-   Criar componentes base.

Entregáveis:

-   Estrutura frontend organizada.
-   Componentes base.
-   Navegação inicial.

### Phase 09 — Frontend and API Integration

Status: Not Started

Dependências: Phases 04, 05 e 08

Objetivo: Integrar o frontend aos serviços backend.

Atividades:

-   Configurar cliente HTTP.
-   Integrar cadastro.
-   Integrar login.
-   Integrar gerenciamento de sessão.
-   Integrar CRUD de tarefas.
-   Integrar categorias.
-   Integrar filtros.
-   Integrar paginação.
-   Implementar tratamento de erros.
-   Validar fluxos de autenticação.

Entregáveis:

-   Frontend conectado à API.
-   Fluxos principais funcionais.

### Phase 10 — Interface and Dashboard

Status: Not Started

Dependências: Phase 09

Objetivo: Desenvolver as interfaces principais.

Atividades:

-   Criar tela de login.
-   Criar tela de cadastro.
-   Criar dashboard.
-   Criar listagem de tarefas.
-   Criar formulário de tarefas.
-   Criar gerenciamento de categorias.
-   Criar filtros.
-   Criar gráficos com Recharts.
-   Criar perfil.
-   Implementar responsividade.
-   Implementar Dark Mode.

Entregáveis:

-   Interface completa.
-   Dashboard integrado.
-   Experiência responsiva.

### Phase 11 — Frontend Testing and Additional Features

Status: Not Started

Dependências: Phase 10

Objetivo: Validar o frontend e implementar funcionalidades complementares.

Atividades:

-   Criar testes unitários.
-   Criar testes de componentes.
-   Criar testes E2E.
-   Implementar Soft Delete.
-   Implementar notificações de vencimento.
-   Implementar funcionalidades complementares de auditoria.
-   Validar responsividade.

Entregáveis:

-   Frontend testado.
-   Funcionalidades adicionais implementadas.

### Phase 12 — Finalization and Documentation

Status: Not Started

Dependências: Phases 07, 10 e 11

Objetivo: Preparar o projeto para apresentação e execução.

Atividades:

-   Configurar Swagger/OpenAPI.
-   Configurar Spring Boot Actuator.
-   Configurar logs estruturados.
-   Revisar Docker Compose.
-   Revisar segurança.
-   Executar testes completos.
-   Atualizar README.
-   Documentar instalação.
-   Documentar variáveis de ambiente.
-   Revisar arquitetura.
-   Validar execução completa.

Entregáveis:

-   Aplicação funcional.
-   Documentação completa.
-   Ambiente reproduzível.
-   Projeto pronto para apresentação.

## 3. Regras de execução

1.  Desenvolver uma etapa por vez.
2.  Respeitar as dependências entre etapas.
3.  Não avançar automaticamente.
4.  Validar entregáveis antes de marcar uma etapa como concluída.
5.  Atualizar PROGRESS.md após cada sessão.
6.  Registrar decisões arquiteturais importantes.
7.  Atualizar as SPECs quando houver mudanças aprovadas.

## 4. Definição de concluído

Uma etapa será considerada concluída quando:

-   Todas as atividades previstas no escopo forem implementadas.
-   Os critérios de aceitação forem atendidos.
-   Os testes pertinentes forem executados.
-   Não existirem erros conhecidos que impeçam a entrega.
-   A documentação relevante estiver atualizada.
-   As pendências estiverem registradas.

## 5. Evolução do roadmap

O roadmap poderá ser atualizado conforme o projeto evoluir.

Alterações de escopo deverão ser justificadas e documentadas.

Não antecipar funcionalidades de etapas futuras sem necessidade técnica ou aprovação.