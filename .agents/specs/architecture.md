# Architecture

## 1. Visão geral

O TaskFlow utilizará uma arquitetura Full Stack separada em duas aplicações independentes:

-   Frontend: aplicação React responsável pela interface e experiência do usuário.
-   Backend: API REST desenvolvida com Java e Spring Boot, responsável pelas regras de negócio, autenticação e persistência.

A comunicação entre frontend e backend será realizada por HTTP utilizando JSON.

O backend será responsável por centralizar as regras de negócio e garantir a integridade e segurança dos dados.

## 2. Estilo arquitetural

### Backend

Será utilizada uma arquitetura em camadas (Layered Architecture), com separação de responsabilidades.

Camadas:

1.  Controller
2.  Service
3.  Repository
4.  Persistence

Componentes auxiliares:

-   DTO
-   Mapper
-   Entity
-   Config
-   Security
-   Exception
-   Enum

### Responsabilidades

#### Controller

Responsável por:

-   Receber requisições HTTP.
-   Validar parâmetros de entrada.
-   Acionar os serviços correspondentes.
-   Retornar respostas HTTP apropriadas.

Não deve conter regras de negócio.

#### Service

Responsável por:

-   Implementar regras de negócio.
-   Coordenar operações entre repositories.
-   Controlar transações.
-   Aplicar regras de autorização relacionadas aos dados.

#### Repository

Responsável por:

-   Consultar dados.
-   Persistir entidades.
-   Atualizar e excluir registros.
-   Executar consultas personalizadas.

#### Entity

Representa os modelos persistidos no banco de dados.

Não deve ser utilizada diretamente como contrato de entrada ou saída da API.

#### DTO

Responsável por:

-   Representar dados recebidos pela API.
-   Representar respostas da API.
-   Evitar exposição desnecessária de informações internas.

#### Mapper

Responsável por converter DTOs em entidades e entidades em DTOs.

#### Exception

Responsável pelo tratamento padronizado de erros e exceções.

#### Security

Responsável pela autenticação, autorização e proteção dos endpoints.

## 3. Frontend

O frontend utilizará React com TypeScript e organização baseada em funcionalidades e responsabilidades.

Estrutura principal:

-   assets
-   components
-   pages
-   layouts
-   hooks
-   services
-   stores
-   schemas
-   types
-   utils
-   routes

### Responsabilidades

#### Components

Componentes reutilizáveis da interface.

#### Pages

Páginas e telas da aplicação.

#### Layouts

Estruturas visuais compartilhadas entre páginas.

#### Hooks

Hooks personalizados para reutilização de lógica.

#### Services

Comunicação com a API REST.

#### Stores

Gerenciamento de estados globais utilizando Zustand.

#### Schemas

Schemas de validação utilizando Zod.

#### Types

Tipos e interfaces TypeScript.

#### Routes

Configuração e proteção de rotas.

## 4. Gerenciamento de estado

Serão utilizadas duas estratégias:

### TanStack Query

Responsável pelos dados remotos:

-   Consultas à API.
-   Cache.
-   Sincronização.
-   Invalidação de consultas.
-   Estados de carregamento e erro.
-   Mutations.

### Zustand

Responsável por estados globais compartilhados que não sejam adequadamente representados pelo TanStack Query.

Exemplos:

-   Preferências de interface.
-   Estado de elementos globais.
-   Configurações de apresentação.

Não duplicar dados remotos no Zustand sem necessidade.

## 5. Comunicação entre frontend e backend

A comunicação será realizada por meio de uma API REST.

Padrões:

-   JSON para troca de informações.
-   HTTP para comunicação.
-   DTOs para contratos da API.
-   Códigos HTTP adequados.
-   Validação de entradas.
-   Tratamento padronizado de erros.

A versão inicial da API utilizará o prefixo:

`/api/v1`

## 6. Persistência

O banco de dados será MySQL 8.4 LTS.

A persistência será realizada utilizando:

-   Spring Data JPA.
-   Hibernate ORM.
-   HikariCP.
-   Flyway.

O Flyway será responsável pelo versionamento das alterações do banco.

As alterações de estrutura deverão ser realizadas por migrations versionadas.

## 7. Multiusuário

A aplicação será multiusuário.

Cada usuário terá acesso somente aos dados que lhe pertencem.

As operações envolvendo dados privados deverão considerar o usuário autenticado.

O backend será responsável por aplicar e validar o isolamento dos dados.

Não confiar em identificadores de usuário enviados pelo frontend como mecanismo de autorização.

## 8. Segurança

A autenticação utilizará Spring Security e JWT.

Estratégia prevista:

-   Access Token.
-   Refresh Token com rotação.
-   Hash seguro de senhas.
-   Revogação de sessões.
-   Proteção de endpoints.
-   Autorização por usuário.

Os detalhes estão definidos em `security.md`.

## 9. Tratamento de erros

O backend deverá possuir tratamento global de exceções.

As respostas de erro deverão seguir um formato consistente.

Informações internas, stack traces e dados sensíveis não deverão ser expostos ao cliente.

## 10. Testabilidade

As aplicações deverão possuir testes automatizados.

Backend:

-   JUnit 5.
-   Mockito.
-   Spring Boot Test.
-   Testcontainers.
-   JaCoCo.

Frontend:

-   Vitest.
-   React Testing Library.
-   Playwright.

Os detalhes estão definidos em `../skills/testing/SKILL.md`.

## 11. Infraestrutura

A aplicação utilizará Docker e Docker Compose para padronizar o ambiente de desenvolvimento.

Os serviços deverão ser configurados para facilitar execução local e integração entre frontend, backend e banco de dados.

## 12. Princípios arquiteturais

-   Separação de responsabilidades.
-   Baixo acoplamento.
-   Alta coesão.
-   Código legível.
-   Manutenibilidade.
-   Segurança por padrão.
-   Testabilidade.
-   Simplicidade antes de complexidade.
-   Evitar abstrações desnecessárias.

## 13. Evolução da arquitetura

A arquitetura deverá evoluir conforme as necessidades reais do projeto.

Novas tecnologias, camadas ou padrões arquiteturais somente deverão ser introduzidos quando houver justificativa técnica.

Decisões arquiteturais importantes deverão ser registradas em `DECISIONS.md`.