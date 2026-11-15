---
name: testing
description: Planeja, implementa e executa testes automatizados para o TaskFlow, incluindo testes unitários, integração e ponta a ponta no backend e frontend.
---

# Testing

## 1. Objetivo

Garantir a qualidade, confiabilidade e estabilidade do TaskFlow através de testes automatizados.

Os testes deverão validar comportamentos, regras de negócio, integrações e fluxos críticos.

## 2. Referências obrigatórias

Consultar:

-   `.agents/specs/project-overview.md`
-   `.agents/specs/architecture.md`
-   `.agents/specs/business-rules.md`
-   `.agents/specs/database.md`
-   `.agents/specs/security.md`
-   `.agents/specs/technology-stack.md`

## 3. Stack de testes

### Backend

-   JUnit 5.
-   Mockito.
-   Spring Boot Test.
-   Testcontainers.
-   JaCoCo.

### Frontend

-   Vitest.
-   React Testing Library.
-   Playwright.

## 4. Estratégia de testes

Utilizar três níveis principais:

### Testes unitários

Validam unidades isoladas de código.

Backend:

-   Services.
-   Regras de negócio.
-   Validações.
-   Métodos específicos.

Frontend:

-   Hooks.
-   Utilitários.
-   Schemas.
-   Componentes isolados.

### Testes de integração

Validam a comunicação entre componentes reais.

Backend:

-   Controllers.
-   Services.
-   Repositories.
-   Spring Security.
-   Persistência.
-   MySQL com Testcontainers.

Frontend:

-   Componentes integrados.
-   Formulários.
-   Integração com serviços utilizando dependências controladas.

### Testes E2E

Validam fluxos completos da aplicação.

Utilizar Playwright.

Exemplos:

-   Cadastro.
-   Login.
-   Criação de tarefa.
-   Edição de tarefa.
-   Conclusão de tarefa.
-   Filtragem.
-   Navegação.
-   Logout.

## 5. Regras de testes backend

### JUnit 5

Utilizar para organizar e executar testes.

### Mockito

Utilizar para simular dependências quando apropriado.

### Spring Boot Test

Utilizar para testar a integração dos componentes Spring.

### Testcontainers

Utilizar MySQL em container para testes que dependam do comportamento real do banco.

Evitar depender de diferenças entre H2 e MySQL nos testes de persistência.

### JaCoCo

Utilizar para gerar relatórios de cobertura.

Priorizar cobertura das regras de negócio e funcionalidades críticas.

## 6. Regras de testes frontend

### Vitest

Utilizar para execução de testes unitários.

### React Testing Library

Testar componentes com foco no comportamento percebido pelo usuário.

Evitar testes excessivamente dependentes de detalhes internos de implementação.

### Playwright

Testar os principais fluxos de utilização da aplicação.

Validar:

-   Navegação.
-   Autenticação.
-   Formulários.
-   Operações principais.
-   Tratamento de erros.
-   Responsividade em cenários relevantes.

## 7. Cenários críticos

### Autenticação

-   Cadastro válido.
-   Cadastro inválido.
-   E-mail duplicado.
-   Login válido.
-   Login inválido.
-   Access Token expirado.
-   Refresh Token inválido.
-   Refresh Token revogado.
-   Reutilização de Refresh Token.
-   Logout.

### Tarefas

-   Criar tarefa válida.
-   Criar tarefa inválida.
-   Editar tarefa.
-   Excluir tarefa.
-   Restaurar tarefa, quando implementado.
-   Alterar status.
-   Alterar prioridade.
-   Definir prazo.
-   Filtrar tarefas.
-   Pesquisar tarefas.
-   Paginar resultados.

### Categorias

-   Criar categoria.
-   Editar categoria.
-   Excluir categoria.
-   Associar categoria a tarefa.
-   Tentar utilizar categoria de outro usuário.

### Multiusuário

-   Acessar tarefa própria.
-   Tentar acessar tarefa de outro usuário.
-   Tentar editar tarefa de outro usuário.
-   Tentar excluir tarefa de outro usuário.
-   Tentar utilizar categoria de outro usuário.
-   Consultar dashboard de outro usuário.

Todos os cenários de acesso indevido deverão ser rejeitados.

### Dashboard

-   Total de tarefas.
-   Contagem por status.
-   Tarefas atrasadas.
-   Tarefas concluídas.
-   Isolamento dos indicadores por usuário.

## 8. Organização

Os testes deverão acompanhar a estrutura do código.

Backend:

-   src/test/java.

Frontend:

-   Arquivos de teste próximos aos componentes ou em diretórios de testes organizados por funcionalidade.

Testes E2E:

-   Diretório dedicado para os cenários Playwright.

## 9. Boas práticas

-   Testes independentes.
-   Dados de teste controlados.
-   Nomes descritivos.
-   Cenários positivos e negativos.
-   Não depender da ordem de execução.
-   Não utilizar banco de produção.
-   Evitar mocks desnecessários.
-   Testar comportamentos, não detalhes internos.
-   Garantir limpeza e isolamento dos dados de teste.

## 10. Processo de validação

Para cada funcionalidade:

1.  Identificar regras de negócio envolvidas.
2.  Identificar cenários positivos.
3.  Identificar cenários negativos.
4.  Identificar possíveis falhas de segurança.
5.  Implementar testes unitários pertinentes.
6.  Implementar testes de integração quando necessário.
7.  Implementar testes E2E para fluxos críticos.
8.  Executar os testes.
9.  Registrar falhas e pendências.
10.  Corrigir problemas antes de considerar a entrega concluída.

## 11. Critérios de conclusão

Uma funcionalidade estará validada quando:

-   Os testes relevantes estiverem implementados.
-   Os cenários críticos estiverem cobertos.
-   Os testes forem executados com sucesso.
-   Não houver falhas conhecidas sem justificativa.
-   Os resultados estiverem documentados.

## 12. Métricas

Utilizar JaCoCo para acompanhar a cobertura backend.

Utilizar os relatórios do Vitest para acompanhar a cobertura frontend.

A cobertura deverá servir como indicador de qualidade, não como objetivo isolado.

Priorizar testes de regras de negócio, segurança, persistência e fluxos principais.