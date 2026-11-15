# Security Specification

## 1. Objetivo

Definir os requisitos de segurança do TaskFlow.

A segurança deverá ser considerada em todas as camadas da aplicação.

O backend será responsável pela autenticação, autorização e proteção dos dados.

## 2. Autenticação

Tecnologias:

-   Spring Security.
-   JWT.
-   Access Token.
-   Refresh Token.

### Access Token

Será utilizado para autenticar requisições aos endpoints protegidos.

Requisitos:

-   Possuir tempo de expiração limitado.
-   Ser assinado com algoritmo seguro.
-   Conter apenas informações necessárias.
-   Ser validado pelo backend.
-   Não conter informações sensíveis.

### Refresh Token

Será utilizado para renovar sessões sem exigir novo login.

Requisitos:

-   Possuir prazo de expiração.
-   Ter rotação a cada renovação.
-   Permitir revogação.
-   Ser armazenado no banco somente como hash.
-   Não ser reutilizável após rotação.
-   Ser invalidado em caso de logout.

A reutilização de um token já rotacionado deverá ser tratada como possível comprometimento de sessão, com política de revogação da família de tokens.

## 3. Armazenamento de tokens

Estratégia definida:

-   Access Token com duração curta (15 minutos), armazenado exclusivamente em memória no frontend e renovado silenciosamente via `POST /api/v1/auth/refresh` ao recarregar a página.
-   Refresh Token em cookie `refresh_token` (`HttpOnly=true`, `SameSite=Lax`, `Path=/api/v1/auth`, `Secure=true` em produção).
-   Proteção contra CSRF garantida pela combinação de cookie `SameSite=Lax`, política de CORS restritiva com origens explícitas e exigência de `Content-Type: application/json`.

Não armazenar tokens de autenticação em `localStorage` ou `sessionStorage`.

Os mecanismos de renovação e armazenamento deverão ser implementados de maneira consistente entre frontend e backend.

## 4. Senhas

As senhas deverão ser armazenadas utilizando hash seguro.

Algoritmo definido:

-   Argon2id.

Requisitos:

-   Nunca armazenar senhas em texto puro.
-   Nunca registrar senhas nos logs.
-   Não retornar hashes nas respostas da API.
-   Validar requisitos mínimos de senha.
-   Evitar mensagens que revelem se determinado e-mail está cadastrado.

## 5. Autorização

O sistema deverá validar as permissões em todas as operações privadas.

### Isolamento multiusuário

O usuário autenticado somente poderá acessar:

-   Suas tarefas.
-   Suas categorias.
-   Seu perfil.
-   Seu histórico.
-   Suas sessões.

Não utilizar IDs enviados pelo frontend como fonte de autoridade sobre a identidade do usuário.

O usuário deverá ser identificado a partir do contexto de autenticação validado pelo backend.

## 6. Proteção de endpoints

Endpoints públicos:

-   Cadastro.
-   Login.
-   Documentação pública, conforme configuração.

Endpoints privados:

-   Gerenciamento de tarefas.
-   Gerenciamento de categorias.
-   Dashboard.
-   Histórico.
-   Perfil.
-   Renovação e revogação de sessão, com regras específicas.

A autorização deverá ser aplicada tanto na camada de segurança quanto nas regras de acesso aos recursos.

## 7. CORS

Configurar CORS de maneira restritiva.

Requisitos:

-   Permitir somente origens autorizadas.
-   Definir métodos HTTP necessários.
-   Restringir headers.
-   Não utilizar wildcard indiscriminadamente.
-   Permitir credenciais somente quando necessário e com origens explícitas.

## 8. Validação de entradas

Todos os dados recebidos pela API deverão ser validados.

Utilizar:

-   Jakarta Bean Validation.
-   Validações específicas de negócio.
-   Consultas parametrizadas.
-   Normalização de dados quando apropriado.

## 9. Proteção de informações

Nunca expor:

-   Senhas.
-   Hashes de senhas.
-   Refresh Tokens originais.
-   Segredos JWT.
-   Credenciais de banco.
-   Informações internas de infraestrutura.

Logs não deverão conter dados sensíveis.

## 10. Variáveis de ambiente

Informações sensíveis deverão ser externas ao código-fonte.

Exemplos:

-   DB_USERNAME.
-   DB_PASSWORD.
-   JWT_SECRET.
-   JWT_ACCESS_EXPIRATION.
-   JWT_REFRESH_EXPIRATION.

Não versionar arquivos contendo credenciais reais.

Disponibilizar somente arquivos de exemplo com valores fictícios.

## 11. Tratamento de erros

Respostas de erro não deverão revelar:

-   Stack traces.
-   Estrutura interna de classes.
-   Credenciais.
-   Segredos.
-   Detalhes internos de banco de dados.

Utilizar respostas consistentes e códigos HTTP apropriados.

## 12. Sessões

O sistema deverá permitir:

-   Renovar sessões.
-   Revogar sessões.
-   Encerrar sessões no logout.
-   Detectar reutilização de Refresh Tokens rotacionados.

## 13. Auditoria

Registrar eventos relevantes de segurança, como:

-   Login bem-sucedido.
-   Tentativas de login inválidas.
-   Logout.
-   Revogação de sessões.
-   Alterações importantes de conta.

Não registrar tokens ou senhas.

## 14. Requisitos de validação

A implementação deverá possuir testes para:

-   Login válido.
-   Login inválido.
-   Token expirado.
-   Token inválido.
-   Refresh Token válido.
-   Refresh Token revogado.
-   Reutilização de Refresh Token.
-   Acesso sem autenticação.
-   Acesso a recursos de outro usuário.
-   CORS conforme configuração.

## 15. Princípios

-   Security by Design.
-   Least Privilege.
-   Defense in Depth.
-   Secure Defaults.
-   Fail Securely.

## 16. Definições consolidadas

-   **Algoritmo de assinatura JWT**: `HS256` (`HMAC-SHA256`) com `JWT_SECRET` (mínimo 256 bits) fornecido via variável de ambiente.
-   **Duração do Access Token**: 15 minutos (`900` segundos), mantido apenas em memória no frontend.
-   **Duração do Refresh Token**: 7 dias, opaco, persistido apenas como hash no banco, com rotação transacional e detecção de reuso por família de tokens.
-   **Política de sessões simultâneas**: múltiplas sessões ativas permitidas por usuário (uma família de tokens por login/dispositivo); alteração/redefinição de senha ou exclusão da conta revogam todas as sessões ativas do usuário.
-   **Estratégia de recuperação de senha**: tabela `password_reset_tokens` (validade de 30 minutos, uso único, armazenamento como hash); em ambiente de desenvolvimento, o link/token é emitido no log estruturado do backend (sem servidor SMTP externo).
-   **Configuração definitiva de cookies e proteção CSRF**: cookie `refresh_token` com `HttpOnly=true`, `SameSite=Lax`, `Path=/api/v1/auth` e `Secure=true` em produção, aliado a CORS restritivo com origens explícitas e `Content-Type: application/json`.
-   **Política de limitação de tentativas de login (Rate Limiting)**: controle em memória no endpoint de login bloqueando após 5 tentativas falhas em 15 minutos por e-mail/IP e retornando HTTP `429 Too Many Requests`.