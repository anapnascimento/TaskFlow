---
name: frontend-development
description: Desenvolve interfaces e funcionalidades frontend para o TaskFlow utilizando React, TypeScript, Vite, Tailwind CSS, shadcn/ui, TanStack Query e Zustand.
---

# Frontend Development

## 1. Objetivo

Desenvolver o frontend do TaskFlow seguindo a arquitetura definida, com foco em usabilidade, responsividade, acessibilidade e manutenibilidade, utilizando `modelo-projeto.jpg` como inspiração visual para layout, paleta e cards.

## 2. Referências obrigatórias

Antes de implementar, consultar:

-   `.agents/specs/project-overview.md`
-   `.agents/specs/technology-stack.md`
-   `.agents/specs/architecture.md`
-   `.agents/specs/business-rules.md`
-   `.agents/specs/api-contracts.md`
-   `.agents/specs/security.md`

Consultar também as especificações da funcionalidade solicitada em `.agents/specs/features/`.

## 3. Stack

-   React.
-   Vite.
-   TypeScript.
-   Tailwind CSS.
-   shadcn/ui.
-   React Router.
-   TanStack Query.
-   Zustand.
-   React Hook Form.
-   Zod.
-   Recharts.

## 4. Organização

Estrutura:

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

Organizar funcionalidades de maneira consistente, evitando arquivos excessivamente grandes.

## 5. Componentes

-   Priorizar componentes reutilizáveis.
-   Utilizar shadcn/ui quando apropriado.
-   Evitar duplicação de interface.
-   Manter responsabilidades específicas.
-   Utilizar propriedades tipadas.
-   Evitar componentes excessivamente complexos.

## 6. TypeScript

-   Utilizar tipagem explícita quando necessário.
-   Evitar o uso de any.
-   Definir interfaces e tipos reutilizáveis.
-   Tipar propriedades de componentes.
-   Tipar respostas e requisições da API.
-   Tratar corretamente valores opcionais e nulos.

## 7. Gerenciamento de estado

### TanStack Query

Utilizar para:

-   Consultas à API.
-   Cache.
-   Invalidação.
-   Mutations.
-   Estados de carregamento.
-   Estados de erro.
-   Sincronização de dados remotos.

### Zustand

Utilizar para:

-   Preferências globais.
-   Estados compartilhados da interface.
-   Configurações de apresentação.

Não duplicar desnecessariamente dados da API no Zustand.

## 8. Formulários

Utilizar:

-   React Hook Form.
-   Zod.

Regras:

-   Definir schemas de validação.
-   Apresentar mensagens de erro claras.
-   Validar campos obrigatórios.
-   Evitar submissões duplicadas.
-   Apresentar estados de carregamento.
-   Tratar erros retornados pela API.

A validação frontend não substitui a validação backend.

## 9. Comunicação com API

-   Centralizar a configuração HTTP.
-   Manter serviços separados por funcionalidade.
-   Tipar requisições e respostas.
-   Tratar erros HTTP.
-   Utilizar TanStack Query para sincronização.
-   Respeitar os contratos da API.

Não implementar regras de negócio importantes exclusivamente no frontend.

## 10. Autenticação

-   Proteger rotas privadas.
-   Gerenciar estado de autenticação.
-   Tratar expiração de sessão.
-   Implementar renovação de sessão conforme a estratégia definida em security.md.
-   Evitar armazenamento inseguro de tokens.
-   Tratar falhas de autenticação adequadamente.

O frontend nunca deve ser considerado responsável pela autorização definitiva.

## 11. Interface e experiência

A interface deverá ser:

-   Responsiva.
-   Consistente.
-   Intuitiva.
-   Acessível.
-   Visualmente organizada.

Utilizar Tailwind CSS e shadcn/ui.

Prever:

-   Estados de carregamento.
-   Estados vazios.
-   Estados de erro.
-   Feedback de sucesso.
-   Confirmação de operações destrutivas.
-   Navegação intuitiva.
-   Tema claro e escuro.

## 12. Dashboard

Utilizar Recharts para apresentar indicadores e gráficos.

Os dados deverão ser obtidos da API.

Não utilizar dados fictícios para simular indicadores em funcionalidades implementadas.

## 13. Responsividade

Garantir adaptação para:

-   Desktop.
-   Tablet.
-   Mobile.

Evitar layouts que dependam exclusivamente de larguras fixas.

## 14. Qualidade

Aplicar:

-   Componentização.
-   Separação de responsabilidades.
-   Reutilização.
-   Tipagem.
-   Acessibilidade.
-   Código legível.
-   Simplicidade.

Evitar:

-   Componentes gigantes.
-   Lógica de API dentro de componentes visuais.
-   Duplicação de formulários.
-   Estados globais desnecessários.
-   Uso excessivo de useEffect.
-   Tipagem inadequada.

## 15. Testes

Utilizar:

-   Vitest.
-   React Testing Library.
-   Playwright.

Criar testes para:

-   Componentes.
-   Formulários.
-   Validações.
-   Hooks.
-   Fluxos de autenticação.
-   Interações principais.
-   Navegação.
-   Fluxos completos da aplicação.

## 16. Processo de implementação

1.  Analisar a funcionalidade.
2.  Consultar as SPECs.
3.  Analisar componentes existentes.
4.  Identificar arquivos afetados.
5.  Planejar a implementação.
6.  Desenvolver os componentes.
7.  Integrar os serviços.
8.  Criar testes.
9.  Validar responsividade.
10.  Verificar erros e regressões.

## 17. Critérios de conclusão

-   Funcionalidade implementada.
-   Tipagem adequada.
-   Componentes organizados.
-   Integração funcional.
-   Testes pertinentes executados.
-   Interface responsiva.
-   Estados de carregamento e erro tratados.
-   Sem regressões conhecidas.