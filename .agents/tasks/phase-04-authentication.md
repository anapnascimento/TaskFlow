# Phase 04 — Authentication

## Objetivo

Implementar o sistema de autenticação e gerenciamento de sessão dos usuários.

## Dependências

-   Phase 01 — Planning
-   Phase 02 — Environment Setup
-   Phase 03 — Database

## Escopo

### Cadastro

-   Criar endpoint de cadastro.
-   Validar dados de entrada.
-   Verificar e-mail duplicado.
-   Aplicar hash seguro à senha.
-   Persistir usuário.

### Login

-   Criar endpoint de login.
-   Validar credenciais.
-   Gerar Access Token.
-   Criar Refresh Token.
-   Persistir hash do Refresh Token.

### Sessão

-   Implementar renovação de sessão.
-   Implementar rotação de Refresh Token (múltiplas sessões simultâneas permitidas).
-   Implementar logout e revogação.
-   Implementar recuperação e redefinição de senha (`password_reset_tokens`, emissão em log estruturado em dev).
-   Implementar gerenciamento de perfil (`PATCH /api/v1/users/me`), alteração de senha (`PATCH /api/v1/users/me/password`) e exclusão lógica de conta (`DELETE /api/v1/users/me`).
-   Proteger endpoints privados.

### Segurança

-   Configurar Spring Security.
-   Configurar filtros JWT (`HS256` com `JWT_SECRET`).
-   Configurar Rate Limiting em memória no login (máx. 5 falhas em 15 min retornando `429`).
-   Configurar CORS.
-   Garantir isolamento por usuário.

### Testes

-   Testar cadastro.
-   Testar login.
-   Testar credenciais inválidas.
-   Testar tokens expirados.
-   Testar refresh token reutilizado.
-   Testar acesso não autorizado.
-   Testar isolamento entre usuários.

## Critérios de aceitação

-   Usuários conseguem criar contas.
-   Usuários conseguem autenticar-se.
-   Endpoints privados exigem autenticação.
-   Senhas nunca são armazenadas em texto puro.
-   Refresh Tokens são rotacionados e revogáveis.
-   Um usuário não consegue acessar os dados de outro.

## Fora do escopo

-   Envio de e-mail por servidor SMTP externo (em desenvolvimento o link/token de recuperação será emitido via log estruturado).
-   Interface de autenticação no frontend.

Essas funcionalidades serão tratadas em etapas próprias.

## Definição de concluído

A etapa estará concluída quando os critérios de aceitação forem atendidos e os testes correspondentes passarem.