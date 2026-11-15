# Project Overview

## 1. Visão geral

O TaskFlow é uma aplicação full-stack de gerenciamento de tarefas e produtividade pessoal.

O sistema permitirá que usuários organizem suas atividades, acompanhem prazos, classifiquem tarefas e visualizem indicadores de produtividade.

## 2. Objetivo

Desenvolver uma aplicação moderna, responsiva, segura e escalável, com suporte a múltiplos usuários.

O projeto também deverá demonstrar boas práticas de engenharia de software e desenvolvimento full-stack.

## 3. Funcionalidades principais

-   Cadastro e autenticação de usuários.
-   Gerenciamento completo de tarefas.
-   Gerenciamento de categorias.
-   Classificação por prioridade.
-   Controle de status.
-   Filtros e pesquisa.
-   Dashboard de produtividade.
-   Histórico de atividades.
-   Gerenciamento de perfil.

## 4. Requisitos funcionais

-   RF01: O usuário deve conseguir criar uma conta.
-   RF02: O usuário deve conseguir autenticar-se.
-   RF03: O usuário deve conseguir criar tarefas.
-   RF04: O usuário deve conseguir editar tarefas.
-   RF05: O usuário deve conseguir excluir tarefas.
-   RF06: O usuário deve conseguir organizar tarefas por categorias.
-   RF07: O usuário deve conseguir definir prioridades.
-   RF08: O usuário deve conseguir filtrar tarefas.
-   RF09: O usuário deve visualizar indicadores de produtividade.
-   RF10: O usuário deve visualizar seu histórico de atividades.

## 5. Requisitos não funcionais

-   RNF01: A aplicação deve ser responsiva.
-   RNF02: Os dados devem ser isolados por usuário.
-   RNF03: A aplicação deve possuir autenticação segura.
-   RNF04: O backend deve disponibilizar uma API REST.
-   RNF05: O sistema deve possuir testes automatizados.
-   RNF06: O banco de dados deve utilizar migrações versionadas.
-   RNF07: O projeto deve ser executável utilizando Docker.

## 6. Escopo

O sistema será desenvolvido incrementalmente, começando por um MVP e evoluindo com funcionalidades adicionais.

A imagem `modelo-projeto.jpg` será utilizada exclusivamente como referência visual (layout, paleta de cores, cards e navegação lateral adaptada aos módulos do TaskFlow: Dashboard, Tarefas, Categorias, Histórico, Lixeira e Perfil). O domínio permanece estritamente individual por usuário, sem entidade de Projetos e sem compartilhamento de tarefas entre usuários.

Funcionalidades não previstas nesta especificação deverão ser avaliadas antes de sua implementação.