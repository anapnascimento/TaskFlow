# Technical Decisions

Registro das principais decisões técnicas e arquiteturais do TaskFlow.

Este documento tem como objetivo manter o histórico das decisões tomadas durante o desenvolvimento, documentando seus motivos, alternativas consideradas e consequências.

As decisões registradas aqui devem permanecer consistentes com a documentação arquitetural e as especificações do projeto.

## Índice


| ID | Decisão | Status| 
| :--- | :---: | :---: |
| ADR-001| Arquitetura cliente-servidor| Accepted| 
| ADR-002| Stack tecnológica| Accepted| 
| ADR-003| Arquitetura em camadas no backend| Accepted| 
| ADR-004| MySQL como banco de dados| Accepted| 
| ADR-005| API REST versionada| Accepted| 
| ADR-006| Autenticação JWT com Refresh Token| Accepted
| ADR-007| Hash de senhas com Argon2id| Accepted| 
| ADR-008| Isolamento multiusuário| Accepted| 
| ADR-009| Soft Delete| Accepted| 
| ADR-010| Identificadores BIGINT| Accepted| 
| ADR-011| Datas armazenadas em UTC| Accepted| 
| ADR-012| Flyway para migrações| Accepted| 
| ADR-013| Estratégia de testes automatizados| Accepted| 
| ADR-014| Docker e Docker Compose| Accepted| 

----------

## ADR-001 — Arquitetura cliente-servidor

**Status:** Accepted

### Contexto

O sistema precisa separar a interface do usuário das regras de negócio e da persistência de dados.

### Decisão

Adotar arquitetura cliente-servidor, utilizando React no frontend e Spring Boot no backend.

A comunicação ocorrerá através de uma API REST utilizando JSON.

### Alternativas consideradas

-   Aplicação monolítica tradicional.
-   Frontend e backend independentes.
-   Arquitetura baseada em microserviços.

### Justificativa

A separação entre frontend e backend permite desenvolvimento independente, melhor organização das responsabilidades e maior facilidade de manutenção.

### Consequências

-   Frontend e backend terão estruturas independentes.
-   A comunicação dependerá de contratos HTTP.
-   Será necessário gerenciar autenticação e CORS.
-   A integração deverá ser validada através de testes.

----------

## ADR-002 — Stack tecnológica

**Status:** Accepted

### Contexto

O projeto necessita de tecnologias modernas, com boa documentação, suporte e aplicabilidade no desenvolvimento full-stack.

### Decisão

Adotar:

**Frontend**

-   React.
-   TypeScript.
-   Vite.
-   Tailwind CSS.
-   shadcn/ui.
-   TanStack Query.
-   Zustand.

**Backend**

-   Java 25.
-   Spring Boot.
-   Maven 3.9.16.
-   Spring Data JPA.
-   Spring Security.
-   Hibernate.

**Banco de dados**

-   MySQL 8.4 LTS.

### Justificativa

A stack oferece ferramentas consolidadas para construção de aplicações web, separação de responsabilidades, persistência e testes automatizados.

### Consequências

-   Utilização de TypeScript no frontend.
-   Desenvolvimento backend com Java e Spring.
-   Padronização das dependências através do Maven.
-   Necessidade de manter compatibilidade entre as versões utilizadas.

----------

## ADR-003 — Arquitetura em camadas no backend

**Status:** Accepted

### Contexto

O backend precisa manter as responsabilidades organizadas e facilitar testes e manutenção.

### Decisão

Adotar arquitetura em camadas:

-   Controller.
-   Service.
-   Repository.
-   Entity.
-   DTO.
-   Security.
-   Config.
-   Exception.
-   Mapper.

### Justificativa

A separação permite organizar responsabilidades e reduzir o acoplamento entre a API, as regras de negócio e a persistência.

### Consequências

-   Controllers não deverão concentrar regras de negócio.
-   Services serão responsáveis pelas regras de negócio.
-   Repositories serão responsáveis pelo acesso aos dados.
-   DTOs serão utilizados nos contratos da API.

----------

## ADR-004 — MySQL como banco de dados

**Status:** Accepted

### Contexto

O sistema precisa de persistência relacional, integridade referencial e suporte a transações.

### Decisão

Utilizar MySQL 8.4 LTS.

### Justificativa

O MySQL oferece suporte a relacionamentos, transações, índices e consultas necessárias para o sistema.

### Consequências

-   Utilização de Hibernate e Spring Data JPA.
-   Gerenciamento de conexões com HikariCP.
-   Migrações controladas pelo Flyway.
-   Testes de integração utilizando MySQL com Testcontainers.

----------

## ADR-005 — API REST versionada

**Status:** Accepted

### Contexto

Frontend e backend precisam de um contrato de comunicação estável e independente da implementação interna.

### Decisão

Adotar API REST utilizando JSON, com versionamento através do prefixo `/api/v1`.

### Justificativa

A abordagem oferece simplicidade, padronização e facilidade de integração.

### Consequências

-   Endpoints deverão seguir convenções REST.
-   Respostas deverão utilizar formatos padronizados.
-   Erros deverão possuir estrutura consistente.
-   Alterações incompatíveis deverão possuir estratégia de versionamento.

----------

## ADR-006 — JWT com Refresh Token rotativo

**Status:** Accepted

### Contexto

O sistema precisa oferecer autenticação segura e gerenciamento do ciclo de vida das sessões.

### Decisão

Utilizar:

-   JWT como Access Token.
-   Refresh Token opaco com rotação.
-   Access Token com duração inicial de 15 minutos.
-   Refresh Token com duração inicial de 7 dias.
-   Refresh Token armazenado em cookie HttpOnly.
-   Hash do Refresh Token persistido no banco.

### Justificativa

A estratégia permite autenticação stateless para requisições comuns, mantendo controle de sessões através do Refresh Token.

### Consequências

-   Necessidade de persistência de sessões.
-   Implementação de rotação e revogação.
-   Proteção contra reutilização de tokens.
-   Configuração de cookies e proteção CSRF.
-   Gerenciamento seguro das chaves de assinatura.

----------

## ADR-007 — Argon2id para senhas

**Status:** Accepted

### Contexto

As senhas dos usuários precisam ser armazenadas de maneira segura.

### Decisão

Utilizar Argon2id para geração dos hashes de senha.

### Justificativa

Argon2id é um algoritmo de hashing de senhas desenvolvido para oferecer resistência a ataques baseados em hardware especializado.

### Consequências

-   Senhas nunca serão armazenadas em texto puro.
-   Os parâmetros de custo deverão ser configurados adequadamente.
-   A verificação será realizada através do mecanismo de hashing.
-   Não será possível recuperar a senha original a partir do hash.

----------

## ADR-008 — Isolamento multiusuário

**Status:** Accepted

### Contexto

O sistema permitirá que vários usuários utilizem a aplicação simultaneamente.

Os dados de cada usuário deverão permanecer isolados.

### Decisão

Todas as entidades pertencentes a usuários deverão possuir relacionamento com o respectivo usuário.

O backend utilizará a identidade do usuário autenticado para controlar o acesso aos recursos.

### Justificativa

O isolamento deverá ser garantido no servidor, independentemente das informações enviadas pelo frontend.

### Consequências

-   Consultas deverão considerar o usuário autenticado.
-   Operações deverão validar propriedade dos recursos.
-   Testes deverão verificar tentativas de acesso entre usuários.
-   O frontend não será responsável pela autorização definitiva.

----------

## ADR-009 — Soft Delete

**Status:** Accepted

### Contexto

O sistema deverá permitir a exclusão lógica de tarefas e categorias, preservando a possibilidade de recuperação quando aplicável.

### Decisão

Utilizar o campo `deletedAt` para representar a exclusão lógica.

Registros excluídos não deverão aparecer nas consultas comuns.

### Justificativa

A exclusão lógica permite recuperação de dados e preservação do histórico.

### Consequências

-   Consultas comuns deverão filtrar registros excluídos.
-   Deverá existir operação de restauração.
-   Exclusão lógica deverá ser considerada nas métricas do dashboard.
-   Relacionamentos deverão possuir regras explícitas para exclusão e restauração.

----------

## ADR-010 — Identificadores BIGINT

**Status:** Accepted

### Contexto

As entidades precisam de identificadores únicos e eficientes para relacionamentos no banco de dados.

### Decisão

Utilizar BIGINT como identificador primário das entidades.

### Justificativa

BIGINT oferece capacidade suficiente para o crescimento esperado do sistema e possui ampla compatibilidade com MySQL e Hibernate.

### Consequências

-   IDs serão representados como Long no Java.
-   IDs serão tratados como números no banco.
-   O frontend deverá utilizar tipos compatíveis.
-   Os identificadores não deverão ser utilizados como mecanismo de autorização.

----------

## ADR-011 — Datas armazenadas em UTC

**Status:** Accepted

### Contexto

O sistema poderá ser utilizado por usuários em diferentes fusos horários.

### Decisão

Utilizar UTC para persistência e comunicação de instantes absolutos.

No backend:

-   Instant para instantes.
-   LocalDate para datas civis.
-   ZoneId para regras de fuso horário.

Na API:

-   Datas e instantes em ISO 8601.
-   Instantes absolutos com indicação explícita de fuso.

No frontend:

-   Conversão para o fuso horário do usuário na apresentação.

### Justificativa

Essa abordagem evita inconsistências temporais e facilita o tratamento de prazos e históricos.

### Consequências

-   Configuração consistente de timezone no backend e banco.
-   Conversões explícitas entre datas civis e instantes.
-   Testes para mudanças de fuso horário e horário de verão quando aplicável.

----------

## ADR-012 — Flyway para migrações

**Status:** Accepted

### Contexto

O esquema do banco precisa evoluir de maneira controlada e reproduzível.

### Decisão

Utilizar Flyway para gerenciar as migrações do MySQL.

### Justificativa

O Flyway permite versionar alterações do esquema e manter ambientes consistentes.

### Consequências

-   Migrações deverão ser versionadas.
-   Migrações aplicadas não deverão ser alteradas.
-   Novas alterações deverão utilizar novas migrations.
-   A criação e atualização do banco deverão ser reproduzíveis.

----------

## ADR-013 — Estratégia de testes automatizados

**Status:** Accepted

### Contexto

O projeto precisa garantir confiabilidade das regras de negócio, integrações e funcionalidades.

### Decisão

Adotar:

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

### Justificativa

A combinação permite validar componentes isolados, integrações e fluxos completos da aplicação.

### Consequências

-   Testes deverão acompanhar a implementação.
-   Regras de negócio críticas deverão possuir cobertura.
-   Testes de persistência deverão utilizar MySQL quando necessário.
-   Fluxos principais deverão possuir testes E2E.

----------

## ADR-014 — Docker e Docker Compose

**Status:** Accepted

### Contexto

O projeto precisa oferecer um ambiente de desenvolvimento reproduzível.

### Decisão

Utilizar Docker e Docker Compose para gerenciamento dos serviços e dependências de infraestrutura.

### Justificativa

A containerização facilita a configuração, inicialização e integração dos serviços.

### Consequências

-   Configurações deverão ser documentadas.
-   Segredos não deverão ser versionados.
-   Serviços deverão possuir configurações específicas por ambiente.
-   Persistência do MySQL deverá utilizar volumes apropriados.

----------

## Processo de atualização

Novas decisões deverão ser registradas neste documento seguindo a estrutura:

1.  Identificador sequencial.
2.  Título.
3.  Status.
4.  Contexto.
5.  Decisão.
6.  Alternativas consideradas, quando aplicável.
7.  Justificativa.
8.  Consequências.

### Status possíveis

-   Proposed: decisão proposta, ainda não aprovada.
-   Accepted: decisão aprovada.
-   Rejected: decisão avaliada e rejeitada.
-   Superseded: decisão substituída por outra.

Decisões antigas não deverão ser removidas quando substituídas. Seu histórico deverá ser preservado.

## Princípio geral

Toda decisão arquitetural relevante deverá ser documentada antes ou durante sua implementação.

O objetivo é manter transparência, consistência e rastreabilidade na evolução técnica do TaskFlow.