# Authentication Specification

## 1. Objetivo

Definir os requisitos funcionais e técnicos do módulo de autenticação.

O módulo será responsável pelo cadastro de usuários, login, gerenciamento de sessões, renovação de tokens, logout e recuperação de senha.

## 2. Funcionalidades

-   Cadastro.
-   Login.
-   Logout.
-   Renovação de sessão.
-   Recuperação de senha.
-   Redefinição de senha.
-   Consulta do usuário autenticado.
-   Atualização de perfil (`name` e `timezone`).
-   Exclusão lógica da conta (`Soft Delete`).
-   Alteração de senha.

## 3. Cadastro

### Endpoint

`POST /api/v1/auth/register`

### Request

```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "StrongPassword123!",
  "timezone": "America/Sao_Paulo"
}
```

### Response — 201

```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "timezone": "America/Sao_Paulo",
  "createdAt": "2026-11-10T12:00:00Z"
}
```

### Regras

-   Nome obrigatório (1 a 100 caracteres).
-   E-mail obrigatório, válido e normalizado (até 255 caracteres).
-   E-mail único.
-   Senha obrigatória.
-   `timezone` opcional (identificador IANA válido até 64 caracteres; padrão `America/Sao_Paulo` se omitido).
-   Senha armazenada utilizando Argon2id.
-   Não retornar senha ou hash.
-   Não permitir cadastro com e-mail já utilizado.

O cadastro não deverá conceder automaticamente acesso autenticado, salvo decisão futura explícita.

## 4. Login

### Endpoint

`POST /api/v1/auth/login`

### Request

```json
{
  "email": "john@example.com",
  "password": "StrongPassword123!"
}
```

### Response — 200

```json
{
  "accessToken": "<access_token>",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "timezone": "America/Sao_Paulo"
  }
}
```

O Refresh Token será enviado separadamente através de cookie HttpOnly.

### Regras

-   Validar credenciais e garantir que a conta não foi excluída logicamente (`deleted_at IS NULL`).
-   Não revelar se o e-mail existe.
-   Criar uma sessão (permitindo múltiplas sessões simultâneas por usuário, uma família de tokens por login/dispositivo).
-   Gerar Access Token.
-   Gerar Refresh Token.
-   Armazenar somente o hash do Refresh Token.
-   Aplicar Rate Limiting em memória (máximo de 5 tentativas falhas em 15 minutos por e-mail/IP, retornando `429 Too Many Requests`).
-   Registrar o evento de autenticação.

## 5. Access Token

Características definidas:

-   Tipo: JWT.
-   Duração: 15 minutos (`900` segundos).
-   Algoritmo: assinatura simétrica `HS256` (`HMAC-SHA256`) com `JWT_SECRET` (mínimo 256 bits via variável de ambiente).
-   Identificador do usuário: subject (`sub`).
-   Identificador único do token: `jti`.
-   Emissão: `iat`.
-   Expiração: `exp`.
-   Emissor: `iss`.
-   Audiência: `aud`.

Não incluir informações sensíveis no payload.

No frontend, o Access Token deverá permanecer exclusivamente em memória (nunca em `localStorage` ou `sessionStorage`), sendo renovado silenciosamente via `/api/v1/auth/refresh` quando necessário.

## 6. Refresh Token

Características:

-   Token opaco, gerado criptograficamente.
-   Duração: 7 dias.
-   Armazenamento somente do hash.
-   Cookie `refresh_token` com `HttpOnly=true`, `SameSite=Lax`, `Path=/api/v1/auth` e `Secure=true` em produção.
-   Múltiplas sessões simultâneas permitidas (uma família de tokens por sessão).
-   Rotação a cada renovação.

O Refresh Token não deverá ser utilizado como Access Token.

## 7. Renovação de sessão

### Endpoint

`POST /api/v1/auth/refresh`

O Refresh Token deverá ser enviado automaticamente pelo cookie.

### Response — 200

json

```
{
  "accessToken": "<new_access_token>",
  "tokenType": "Bearer",
  "expiresIn": 900
}

```

O backend deverá substituir o cookie pelo novo Refresh Token.

### Regras

-   Validar o Refresh Token.
-   Verificar expiração.
-   Verificar revogação.
-   Invalidar o token anterior.
-   Gerar novo Refresh Token.
-   Persistir o hash do novo token.
-   Emitir novo Access Token.

A rotação deverá ocorrer de maneira transacional.

### Reutilização

Se um Refresh Token já utilizado for apresentado novamente:

-   Considerar possível comprometimento da sessão.
-   Revogar a família de tokens associada.
-   Exigir nova autenticação.
-   Registrar o evento de segurança.

## 8. Logout

### Endpoint

`POST /api/v1/auth/logout`

### Response

`204 No Content`

### Regras

-   Revogar a sessão correspondente.
-   Invalidar o Refresh Token.
-   Remover o cookie de autenticação.
-   Registrar o evento.

O Access Token já emitido poderá continuar válido até sua expiração, salvo implementação de mecanismo adicional de revogação.

## 9. Consulta do usuário autenticado

### Endpoint

`GET /api/v1/auth/me`

### Response — 200

```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "timezone": "America/Sao_Paulo",
  "createdAt": "2026-11-10T12:00:00Z"
}
```

Retornar somente informações permitidas ao usuário autenticado.

## 10. Atualização de perfil e exclusão de conta

### Atualização de perfil

`PATCH /api/v1/users/me`

#### Request

```json
{
  "name": "John Updated",
  "timezone": "America/Sao_Paulo"
}
```

#### Response — 200

```json
{
  "id": 1,
  "name": "John Updated",
  "email": "john@example.com",
  "timezone": "America/Sao_Paulo",
  "createdAt": "2026-11-10T12:00:00Z"
}
```

#### Regras

-   Exigir autenticação.
-   Atualizar apenas os campos enviados (`name` entre 1 e 100 caracteres e `timezone` IANA válido até 64 caracteres).
-   Não permitir alteração de `email` por este endpoint.

### Exclusão lógica da conta

`DELETE /api/v1/users/me`

#### Response

`204 No Content`

#### Regras

-   Exigir autenticação.
-   Preencher `deleted_at` do usuário (`Soft Delete`).
-   Revogar imediatamente todos os Refresh Tokens do usuário.
-   Remover o cookie `refresh_token`.
-   Impedir novos logins ou renovações de sessão para a conta excluída.

## 11. Alteração de senha

### Endpoint

`PATCH /api/v1/users/me/password`

### Request

```json
{
  "currentPassword": "CurrentPassword123!",
  "newPassword": "NewPassword123!"
}
```

### Regras

-   Exigir autenticação.
-   Validar senha atual.
-   Validar nova senha.
-   Atualizar o hash (Argon2id).
-   Revogar todas as sessões ativas (Refresh Tokens) do usuário.
-   Registrar evento de alteração.

## 12. Recuperação de senha

### Solicitação

`POST /api/v1/auth/password/forgot`

Request:

```json
{
  "email": "john@example.com"
}
```

Resposta genérica:

```json
{
  "message": "If the account exists, instructions will be sent."
}
```

### Redefinição

`POST /api/v1/auth/password/reset`

Request:

```json
{
  "token": "<reset_token>",
  "newPassword": "NewPassword123!"
}
```

### Regras

-   Persistir o hash do token na tabela `password_reset_tokens`.
-   O token deverá possuir expiração de 30 minutos e ser de uso único (`used_at`).
-   Em ambiente de desenvolvimento, emitir o link/token de redefinição apenas no log estruturado do backend (sem exigir servidor SMTP externo e sem expor em logs de produção).
-   Não revelar se a conta existe.
-   Revogar todas as sessões ativas após redefinição.
-   Não registrar tokens em logs de produção.

## 13. Proteção contra ataques

Implementar:

-   Rate Limiting em memória no login (máximo 5 falhas em 15 minutos por e-mail/IP, retornando HTTP `429`).
-   Proteção contra enumeração de usuários.
-   Validação de tokens (`HS256`).
-   Proteção CSRF via cookie `SameSite=Lax`, CORS restritivo e `Content-Type: application/json`.
-   Cookies seguros (`HttpOnly`, `Path=/api/v1/auth`, `Secure` em produção).
-   Proteção contra reutilização de Refresh Tokens (revogação da família de tokens).
-   Armazenamento seguro de segredos via variáveis de ambiente.

## 14. Testes de aceitação

-   Cadastro válido e e-mail duplicado.
-   Login válido, inválido e bloqueio por Rate Limiting (`429`).
-   Access Token expirado e inválido.
-   Refresh Token válido, expirado, revogado e reutilizado.
-   Logout.
-   Consulta e atualização de perfil (`GET /auth/me`, `PATCH /users/me`).
-   Exclusão lógica da conta (`DELETE /users/me`) com revogação de sessões.
-   Alteração de senha com revogação de sessões.
-   Recuperação e redefinição de senha (`password/forgot` e `password/reset`).

## 15. Critérios de conclusão

-   Todos os endpoints implementados.
-   Tokens validados corretamente.
-   Rotação implementada.
-   Revogação funcional.
-   Cookies configurados corretamente.
-   Testes automatizados aprovados.
-   Nenhuma informação sensível exposta.