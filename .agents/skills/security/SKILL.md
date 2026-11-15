---
name: security
description: Implementa e audita os mecanismos de autenticação, autorização e segurança do TaskFlow com Spring Security, JWT (HS256), Refresh Token rotativo e Argon2id.
---

# Security

## 1. Objetivo

Garantir que todas as camadas do TaskFlow sigam os requisitos de segurança definidos em `.agents/specs/security.md` e `.agents/specs/features/authentication.md`, protegendo credenciais, sessões e o isolamento multiusuário.

## 2. Referências obrigatórias

-   `.agents/specs/security.md`
-   `.agents/specs/features/authentication.md`
-   `.agents/specs/api-contracts.md`
-   `.agents/specs/business-rules.md`

## 3. Controles obrigatórios

### Autenticação e JWT (Access Token)

-   Utilizar JWT assinado com `HS256` (`HMAC-SHA256`) utilizando `JWT_SECRET` (mínimo 256 bits) fornecido via variável de ambiente.
-   Duração do Access Token: 15 minutos (`900` segundos).
-   Claims mínimos: `sub` (userId), `jti`, `iat`, `exp`, `iss`, `aud`. Nunca incluir dados sensíveis no payload.
-   No frontend, manter o Access Token apenas em memória (Zustand/contexto em memória, nunca em `localStorage` ou `sessionStorage`); ao recarregar a página, renovar silenciosamente via `POST /api/v1/auth/refresh`.

### Refresh Token e sessões

-   Token opaco gerado criptograficamente, com duração de 7 dias.
-   Armazenar somente o hash do token na tabela `refresh_tokens` (nunca o valor bruto).
-   Transportar em cookie `refresh_token` com `HttpOnly=true`, `SameSite=Lax`, `Path=/api/v1/auth` e `Secure=true` em produção.
-   Permitir múltiplas sessões simultâneas por usuário (uma família de tokens por login/dispositivo).
-   Executar rotação transacional a cada `POST /api/v1/auth/refresh`.
-   Ao detectar reutilização de um Refresh Token já rotacionado/substituído, revogar toda a família de tokens associada, registrar evento de segurança e exigir novo login.
-   Revogar todas as sessões ativas do usuário ao alterar senha, redefinir senha ou excluir logicamente a conta (`DELETE /api/v1/users/me`).

### Senhas e recuperação de senha

-   Hash exclusivo com `Argon2id` (nunca armazenar ou logar senhas em texto puro).
-   Contas excluídas logicamente (`deleted_at IS NOT NULL`) não podem autenticar-se ou renovar sessão.
-   Recuperação de senha via `password_reset_tokens`: token de uso único, expiração de 30 minutos, persistido somente como hash, resposta genérica para evitar enumeração de e-mails e emissão do link/token apenas no log estruturado em ambiente de desenvolvimento (sem expor tokens em produção ou respostas HTTP).

### Proteção contra ataques e isolamento

-   Rate Limiting em memória no endpoint `POST /api/v1/auth/login`: bloquear após 5 tentativas falhas em 15 minutos por e-mail/IP, retornando HTTP `429 Too Many Requests`.
-   CORS restritivo configurado com origens explícitas e `allowCredentials(true)`.
-   Isolamento multiusuário obrigatório no backend: identificar o usuário exclusivamente pelo contexto autenticado (`SecurityContextHolder`), jamais confiando em `userId` enviado pelo cliente. Retornar `404` ao tentar acessar recurso inexistente ou pertencente a outro usuário.
-   Nunca expor stack traces, segredos, hashes ou detalhes internos de infraestrutura nas respostas Problem Details (RFC 9457).
