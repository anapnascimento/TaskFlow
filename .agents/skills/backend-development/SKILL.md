---
name: backend-development
description: Desenvolve funcionalidades backend utilizando Java 25, Spring Boot, Maven, Spring Data JPA e MySQL para o TaskFlow. Utilize para implementar APIs REST, regras de negócio, persistência, autenticação e integrações backend.
---

# Backend Development

## 1. Objetivo

Implementar funcionalidades backend do TaskFlow seguindo as especificações, arquitetura (`com.taskflow`) e boas práticas definidas no projeto.

## 2. Referências obrigatórias

Antes de implementar, consultar:

-   `.agents/specs/project-overview.md`
-   `.agents/specs/technology-stack.md`
-   `.agents/specs/architecture.md`
-   `.agents/specs/business-rules.md`
-   `.agents/specs/database.md`
-   `.agents/specs/api-contracts.md`
-   `.agents/specs/security.md`

Consultar também a SPEC específica da funcionalidade em `.agents/specs/features/`.

## 3. Stack

-   Java 25.
-   Spring Boot.
-   Maven 3.9.16.
-   Spring Web.
-   Spring Data JPA.
-   Spring Security.
-   Spring Validation.
-   Hibernate.
-   Flyway.
-   MySQL 8.4.

## 4. Arquitetura

Seguir arquitetura em camadas.

Estrutura:

-   config
-   security
-   controller
-   service
-   repository
-   entity
-   dto
-   mapper
-   exception
-   enums

Não misturar responsabilidades entre camadas.

## 5. Regras de implementação

### Controllers

-   Responsáveis pelas requisições HTTP.
-   Devem delegar operações aos Services.
-   Devem utilizar DTOs.
-   Devem retornar códigos HTTP adequados.
-   Não devem concentrar regras de negócio.

### Services

-   Concentrar regras de negócio.
-   Validar operações.
-   Coordenar repositories.
-   Gerenciar transações quando necessário.
-   Garantir autorização relacionada aos dados.

### Repositories

-   Responsáveis pelo acesso aos dados.
-   Utilizar Spring Data JPA.
-   Criar consultas específicas quando necessário.
-   Evitar consultas ineficientes.

### Entities

-   Representar o modelo persistido.
-   Utilizar relacionamentos adequados.
-   Evitar lógica de negócio complexa.
-   Não expor diretamente pela API.

### DTOs

-   Separar entrada e saída.
-   Evitar exposição de campos internos.
-   Aplicar validações apropriadas.

### Exceptions

-   Utilizar exceções específicas.
-   Centralizar tratamento global.
-   Retornar respostas padronizadas.

## 6. Persistência

-   Utilizar Spring Data JPA.
-   Utilizar Hibernate ORM.
-   Utilizar Flyway para migrations.
-   Utilizar transações adequadamente.
-   Respeitar integridade referencial.
-   Evitar N+1 queries.
-   Evitar carregamento desnecessário de relacionamentos.

Nunca alterar migrations já aplicadas em ambientes compartilhados.

## 7. Segurança

-   Utilizar Spring Security.
-   Validar tokens.
-   Proteger endpoints privados.
-   Garantir isolamento multiusuário.
-   Não confiar em userId enviado pelo cliente.
-   Não expor informações sensíveis.
-   Aplicar validações de autorização nas operações.

Consultar sempre security.md.

## 8. API REST

Seguir os seguintes padrões:

-   Prefixo /api/v1.
-   Utilizar métodos HTTP adequados.
-   Utilizar status HTTP apropriados.
-   Utilizar DTOs.
-   Validar entradas.
-   Padronizar erros.
-   Documentar endpoints relevantes.

## 9. Qualidade de código

Aplicar:

-   Clean Code.
-   SOLID quando aplicável.
-   DRY.
-   Separação de responsabilidades.
-   Baixo acoplamento.
-   Alta coesão.

Evitar:

-   Classes excessivamente grandes.
-   Métodos com muitas responsabilidades.
-   Abstrações desnecessárias.
-   Duplicação de regras.
-   Complexidade prematura.

## 10. Testes

Utilizar:

-   JUnit 5.
-   Mockito.
-   Spring Boot Test.
-   Testcontainers.
-   JaCoCo.

Criar testes para:

-   Regras de negócio.
-   Serviços.
-   Repositories quando necessário.
-   Endpoints.
-   Validações.
-   Autenticação.
-   Autorização.
-   Isolamento entre usuários.

## 11. Processo de implementação

1.  Analisar a funcionalidade solicitada.
2.  Consultar as SPECs.
3.  Analisar o código existente.
4.  Identificar arquivos afetados.
5.  Elaborar um plano.
6.  Implementar a funcionalidade.
7.  Criar ou atualizar testes.
8.  Executar testes e compilação.
9.  Verificar integração.
10.  Atualizar documentação.

## 12. Critérios de conclusão

Uma implementação estará concluída quando:

-   O código compilar.
-   Os testes pertinentes passarem.
-   As regras de negócio forem respeitadas.
-   A segurança estiver validada.
-   Não houver regressões conhecidas.
-   A documentação estiver atualizada quando necessário.

Não considerar uma funcionalidade concluída somente porque seus arquivos foram criados.