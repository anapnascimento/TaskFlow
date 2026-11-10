# TaskFlow

Sistema web de gerenciamento de tarefas e produtividade pessoal, desenvolvido com React, TypeScript, Java 25 e Spring Boot.

O projeto tem como objetivo oferecer uma plataforma para organização de tarefas, gerenciamento de prioridades, acompanhamento de atividades e visualização de indicadores de produtividade.

> **Status:** Em planejamento e preparação para desenvolvimento.

## Sobre o projeto

O TaskFlow será uma aplicação full-stack, multiusuário, com autenticação segura e gerenciamento individualizado de tarefas.

A aplicação permitirá que cada usuário organize suas atividades por categorias, defina prioridades e prazos, acompanhe seu progresso e visualize indicadores através de um dashboard.

O desenvolvimento será realizado de forma incremental, seguindo especificações técnicas, arquitetura documentada e testes automatizados.

## Funcionalidades planejadas

### Autenticação

-   Cadastro de usuários.
-   Login e logout.
-   Autenticação com JWT.
-   Renovação de sessão com Refresh Token.
-   Recuperação e redefinição de senha.
-   Gerenciamento de perfil.

### Gerenciamento de tarefas

-   Criar, consultar, editar e excluir tarefas.
-   Definir descrição e prazo.
-   Definir prioridades.
-   Gerenciar status.
-   Paginação e filtros.
-   Pesquisa por título e descrição.
-   Soft Delete e restauração.

### Categorias

-   Criar categorias personalizadas.
-   Definir cores.
-   Editar e excluir categorias.
-   Associar categorias às tarefas.

### Dashboard

-   Total de tarefas.
-   Tarefas pendentes.
-   Tarefas em andamento.
-   Tarefas concluídas.
-   Tarefas atrasadas.
-   Indicadores de produtividade.
-   Gráficos de conclusão.
-   Histórico de atividades.

### Experiência do usuário

-   Interface responsiva.
-   Tema claro e escuro.
-   Feedback visual das operações.
-   Navegação intuitiva.

## Stack tecnológica

### Frontend

|Tecnologia | Finalidade|
| :--- | :---: |
|React | Biblioteca de interface|
|TypeScript|Tipagem estática|
|Vite|Ferramenta de desenvolvimento|
|Tailwind CSS|Estilização|
|shadcn/ui|Componentes de interface|
|React Router|Navegação|
|TanStack Query|Gerenciamento de dados remotos|
|Zustand|Estado global|
|React Hook Form|Gerenciamento de formulários|
|Zod|Validação|
|Recharts|Gráficos|

### Backend

|Tecnologia | Finalidade|
| :--- | :---: |
|Java 25 | Linguagem|
|Spring Boot | Framework backend|
|Maven 3.9.16 | Gerenciamento de dependências|
|Spring Web |	API REST|
|Spring Data JPA | Persistência|
|Spring Security | Segurança|
|Hibernate | ORM|
|Flyway | Migrações do banco|
|Spring Actuator | Monitoramento|
|Lombok | Redução de código boilerplate (opcional)

### Banco de dados

-   MySQL 8.4 LTS.
-   HikariCP.
-   Flyway.

### Autenticação e segurança

-   Spring Security.
-   JWT Access Token.
-   Refresh Token com rotação.
-   Argon2id.
-   Controle de acesso por usuário.

### Testes

**Backend**

-   JUnit 5.
-   Mockito.
-   Spring Boot Test.
-   Testcontainers.
-   JaCoCo.

**Frontend**

-   Vitest.
-   React Testing Library.
-   Playwright.

### Infraestrutura

-   Docker.
-   Docker Compose.

## Arquitetura

O projeto utiliza arquitetura cliente-servidor, com frontend independente e backend organizado em camadas.

text

```
productivity-system/
│
├── .agent/
│   ├── specs/
│   ├── skills/
│   └── tasks/
│
├── backend/
│
├── frontend/
│
├── README.md
├── ARCHITECTURE.md
├── DECISIONS.md
├── PROGRESS.md
└── docker-compose.yml

```

### Comunicação

O frontend se comunica com o backend através de uma API REST utilizando JSON.

Base URL prevista:

`http://localhost:8080/api/v1`

Para mais detalhes, consulte ARCHITECTURE.md.

## Documentação

A documentação técnica e as especificações do projeto estão organizadas em `.agent/`.

Documento

Descrição

`.agent/specs/project-overview.md`

Visão geral do projeto

`.agent/specs/technology-stack.md`

Tecnologias utilizadas

`.agent/specs/architecture.md`

Arquitetura detalhada

`.agent/specs/business-rules.md`

Regras de negócio

`.agent/specs/database.md`

Modelagem e persistência

`.agent/specs/api-contracts.md`

Contratos da API

`.agent/specs/security.md`

Requisitos de segurança

`.agent/specs/features/`

Especificações das funcionalidades

`.agent/skills/`

Instruções de desenvolvimento

`.agent/tasks/`

Planejamento e acompanhamento das etapas

## Desenvolvimento

O projeto será desenvolvido incrementalmente, seguindo o roadmap definido em `.agent/tasks/roadmap.md`.

Cada etapa deverá possuir:

-   Escopo definido.
-   Critérios de aceitação.
-   Implementação incremental.
-   Testes automatizados.
-   Documentação atualizada.

As funcionalidades somente serão consideradas concluídas após a validação dos requisitos e testes correspondentes.

## Status do projeto

-   Definição da stack tecnológica.
-   Planejamento arquitetural inicial.
-   Definição das principais funcionalidades.
-   Estruturação das especificações.
-   Configuração do ambiente de desenvolvimento.
-   Implementação da autenticação.
-   Implementação do gerenciamento de tarefas.
-   Implementação de categorias.
-   Implementação do dashboard.
-   Testes e validação geral.
-   Preparação para produção.

## Objetivo

Este projeto também tem como propósito aprofundar conhecimentos em desenvolvimento full-stack, arquitetura de software, segurança, persistência de dados, testes automatizados e boas práticas de engenharia de software.