# Metasmart API

> Plataforma de Gerenciamento de Metas com objetivos SMART, acompanhamento de progresso e parceiros de responsabilidade.

## Primeiros Passos

**URL Base:** `http://localhost:8080/relyon/metasmart`

**Swagger UI:** `http://localhost:8080/relyon/metasmart/swagger-ui.html`

**Autenticacao:** Todos os endpoints (exceto `/api/v1/auth/*`) exigem token JWT no header:
```
Authorization: Bearer <token>
```

---

## Resumo dos Endpoints

### Autenticacao (`/api/v1/auth`)
| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| POST | `/register` | Registrar usuario → retorna `{ token }` |
| POST | `/login` | Login → retorna `{ token }` |

**Corpo da requisicao:**
```json
{ "name": "John", "email": "john@example.com", "password": "123456" }
```

---

### Metas (`/api/v1/goals`)
| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| POST | `/` | Criar meta |
| GET | `/` | Listar todas (paginado) |
| GET | `/{id}` | Buscar por ID |
| GET | `/status/{status}` | Filtrar por status |
| GET | `/category/{category}` | Filtrar por categoria |
| PUT | `/{id}` | Atualizar meta |
| DELETE | `/{id}` | Excluir meta |

**Categorias:** `HEALTH` (Saude), `CAREER` (Carreira), `EDUCATION` (Educacao), `FINANCE` (Financas), `RELATIONSHIPS` (Relacionamentos), `PERSONAL` (Pessoal), `OTHER` (Outros)

**Status:** `ACTIVE` (Ativa), `COMPLETED` (Concluida), `PAUSED` (Pausada), `ABANDONED` (Abandonada)

**Resposta inclui campos calculados:**
- `progressPercentage` - % atual do objetivo
- `smartPillars` - Quais criterios SMART estao preenchidos
- `setupCompletionPercentage` - Quao completa esta a configuracao
- `currentStreak` / `longestStreak` - Dias consecutivos com progresso

---

### Progresso (`/api/v1/goals/{goalId}/progress`)
| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| POST | `/` | Adicionar entrada de progresso |
| GET | `/` | Historico (paginado) |
| GET | `/?startDate=&endDate=` | Filtrar por periodo |
| PUT | `/{progressId}` | Atualizar entrada |
| DELETE | `/{progressId}` | Excluir entrada |

**Corpo da requisicao:**
```json
{ "progressValue": 1, "note": "Nota opcional" }
```

---

### Marcos (`/api/v1/goals/{goalId}/milestones`)
| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| POST | `/` | Adicionar marco |
| GET | `/` | Listar todos |
| DELETE | `/{milestoneId}` | Excluir marco |

*Marcos padrao (25%, 50%, 75%, 100%) sao criados automaticamente.*

---

### Itens de Acao (`/api/v1/goals/{goalId}/action-items`)
| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| POST | `/` | Criar item de acao |
| GET | `/` | Listar todos (ordenados) |
| PUT | `/{itemId}` | Atualizar item |
| DELETE | `/{itemId}` | Excluir item |

---

### Obstaculos (`/api/v1/goals/{goalId}/obstacles`)
| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| POST | `/` | Registrar obstaculo |
| GET | `/` | Listar todos (paginado) |
| PUT | `/{obstacleId}` | Atualizar/resolver |
| DELETE | `/{obstacleId}` | Excluir |

---

### Templates de Meta (`/api/v1/goal-templates`)
| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| POST | `/` | Criar template |
| GET | `/` | Meus templates |
| GET | `/available` | Todos acessiveis |
| GET | `/public` | Apenas publicos |
| GET | `/{id}` | Buscar por ID |
| GET | `/{id}/goal` | Gerar meta a partir do template |
| PUT | `/{id}` | Atualizar template |
| DELETE | `/{id}` | Excluir template |

---

### Guardiao de Meta - Endpoints do Dono (`/api/v1/goals/{goalId}/guardians`)

*Para donos de metas gerenciarem seus parceiros de responsabilidade.*

| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| POST | `/` | Convidar guardiao (por email) |
| GET | `/` | Listar guardioes |
| DELETE | `/{guardianshipId}` | Remover guardiao |
| GET | `/nudges` | Ver cutucadas recebidas |
| PUT | `/nudges/{nudgeId}/read` | Marcar como lida |
| PUT | `/nudges/{nudgeId}/react` | Reagir a cutucada |

**Requisicao de convite:**
```json
{
  "guardianEmail": "amigo@example.com",
  "permissions": ["VIEW_PROGRESS", "SEND_NUDGE"],
  "inviteMessage": "Seja meu parceiro de responsabilidade!"
}
```

**Permissoes:**
- `VIEW_PROGRESS` - Ver progresso
- `VIEW_OBSTACLES` - Ver obstaculos
- `VIEW_ACTION_PLAN` - Ver plano de acao
- `VIEW_STREAK` - Ver sequencia
- `SEND_NUDGE` - Enviar cutucadas

---

### Guardiao de Meta - Endpoints do Guardiao (`/api/v1/guardian`)

*Para usuarios atuando como guardioes das metas de outros.*

| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| GET | `/invitations` | Convites pendentes |
| PUT | `/invitations/{id}/accept` | Aceitar convite |
| PUT | `/invitations/{id}/decline` | Recusar convite |
| GET | `/goals` | Metas que estou guardando |
| GET | `/goals/{goalId}` | Ver detalhes (filtrado por permissoes) |
| POST | `/goals/{goalId}/nudges` | Enviar cutucada |
| GET | `/nudges/sent` | Cutucadas enviadas |
| GET | `/nudges/unread-count` | Contagem de nao lidas (como dono) |

**Enviar cutucada:**
```json
{
  "message": "Continue assim!",
  "nudgeType": "ENCOURAGEMENT"
}
```

**Tipos de cutucada:**
- `ENCOURAGEMENT` - Encorajamento
- `REMINDER` - Lembrete
- `CELEBRATION` - Celebracao
- `CHECK_IN` - Verificacao

---

## Paginacao

Todos os endpoints paginados aceitam:
- `page` - Numero da pagina (comeca em 0)
- `size` - Itens por pagina (padrao: 10)
- `sort` - Campo e direcao (ex: `createdAt,desc`)

**Formato da resposta:**
```json
{
  "content": [...],
  "totalElements": 100,
  "totalPages": 10,
  "number": 0,
  "size": 10
}
```

---

## Respostas de Erro

```json
{
  "message": "Descricao do erro",
  "errors": { "campo": "erro de validacao" }
}
```

| Status | Significado |
|--------|-------------|
| 400 | Requisicao invalida / Erro de validacao |
| 401 | Token ausente ou invalido |
| 403 | Acesso negado |
| 404 | Recurso nao encontrado |
| 409 | Conflito (duplicado) |

---

## Colecao Postman

Importe `postman/Metasmart.postman_collection.json` para requisicoes prontas.

### Executando a Sequencia de Teste E2E

A colecao inclui uma pasta **"E2E Test Sequence"** projetada para testar todo o fluxo da aplicacao:

1. Abra o Postman e importe a colecao
2. Clique na pasta **"E2E Test Sequence"**
3. Clique em **"Run"** (ou clique com botao direito → "Run folder")
4. No Collection Runner, clique em **"Run Metasmart API"**

A sequencia ira:
- Registrar um novo usuario e autenticar
- Criar uma meta com configuracao SMART completa
- Adicionar entradas de progresso e marcos
- Criar itens de acao e obstaculos
- Configurar relacionamento de guardiao (registrar guardiao, convidar, aceitar)
- Enviar e reagir a cutucadas
- Limpar todos os dados criados

Todos os testes incluem validacoes para verificar o comportamento correto da API.

---

## Funcionalidades Futuras

As seguintes funcionalidades estao planejadas para versoes futuras:

### Notificacoes Push
- **Lembretes de metas** - Notificacoes agendadas para lembrar usuarios sobre suas metas
- **Lembretes de progresso** - Alertas diarios/semanais para registrar progresso
- **Celebracoes de marcos** - Notificacoes quando marcos sao alcancados
- **Alertas de sequencia** - Avisos antes de perder uma sequencia ("Sua sequencia de 45 dias esta em risco!")
- **Notificacoes de guardiao** - Push em tempo real quando guardioes enviam cutucadas

### Integracao com WhatsApp
- **Notificacoes via WhatsApp** - Receber lembretes e cutucadas pelo WhatsApp
- **Registro rapido de progresso** - Responder mensagens do WhatsApp para registrar progresso
- **Cutucadas de guardiao via WhatsApp** - Guardioes podem enviar encorajamento pelo WhatsApp
- **Resumos diarios/semanais** - Resumos de progresso entregues no WhatsApp

### Integracao com Stripe (Pagamentos)
- **Gerenciamento de assinaturas** - Plano Premium via Stripe Checkout
- **Compras avulsas** - Consumiveis (escudos de sequencia, impulsos) via Stripe
- **Portal do cliente** - Autoatendimento para gerenciar assinatura
- **Webhooks** - Atualizacoes em tempo real do status da assinatura
- **Suporte mobile** - Integracao da API Stripe para iOS/Android

### Funcionalidades de Engajamento e Retencao
- **Reflexoes Semanais** - Avaliar sua semana, ajustar metas e celebrar conquistas
- **Conquistas/Medalhas** - Gamificacao com medalhas como "Sequencia de 30 Dias", "Primeiro Marco", "Heroi Guardiao"
- **Pontuacao de Saude da Meta** - Pontuacao calculada por IA baseada na consistencia do progresso
- **Desafios Sazonais** - Desafios mensais tematicos para manter o engajamento
- **Fotos de Progresso** - Acompanhamento visual antes/depois para metas de fitness, criativas ou de casa
- **Ranking de Guardioes** - Classificacao dos melhores parceiros de responsabilidade
- **Prova Social** - Estatisticas como "87% dos usuarios com metas similares atingiram seu marco"
- **Construcao de Identidade** - Mensagens como "60 dias consistente - voce e um conquistador de metas!"

### Botao de Dificuldade
- **Pedir ajuda** - Botao de um toque quando estiver travado em uma meta
- **Sugestoes inteligentes** - Recomendacoes por IA para dividir metas ou estender prazos
- **Alerta ao guardiao** - Opcionalmente notificar guardioes quando estiver com dificuldade

---

### Ajuda com Dificuldades (`/api/v1/goals`)
| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| GET | `/struggling/status` | Ver pedidos gratuitos restantes |
| POST | `/{goalId}/struggling` | Pedir ajuda para uma meta |
| GET | `/struggling/history` | Ver historico de pedidos |
| PUT | `/struggling/{requestId}/feedback` | Marcar se a ajuda foi util |

**Tipos de dificuldade:** `LACK_OF_TIME` (Falta de tempo), `LACK_OF_MOTIVATION` (Falta de motivacao), `GOAL_TOO_AMBITIOUS` (Meta muito ambiciosa), `UNCLEAR_NEXT_STEPS` (Proximos passos incertos), `EXTERNAL_OBSTACLES` (Obstaculos externos), `LOST_INTEREST` (Perda de interesse), `OTHER` (Outro)

**Corpo da requisicao:**
```json
{
  "strugglingType": "LACK_OF_MOTIVATION",
  "message": "Nao consigo encontrar energia para continuar",
  "notifyGuardians": true
}
```

---

### Assinatura (`/api/v1/subscription`)
| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| GET | `/` | Ver assinatura atual |
| GET | `/entitlements` | Ver acesso a funcionalidades e limites |
| GET | `/purchases` | Ver historico de compras |

---

## Funcionalidades Premium (Planejadas)

### Niveis de Assinatura

| Funcionalidade | Gratis | Premium |
|----------------|--------|---------|
| Metas ativas | 3 | Ilimitadas |
| Guardioes por meta | 1 | 5 |
| Historico de progresso | 30 dias | Ilimitado |
| Templates de meta | Apenas publicos | Criar e compartilhar |
| Reflexoes semanais | Basico | Insights por IA |
| Escudos de sequencia | 1/mes | 3/mes |
| Botao de dificuldade | 1/mes | Ilimitado |
| Conquistas/Medalhas | Conjunto basico | Colecao completa |
| Exportar dados | - | CSV/PDF |
| Suporte prioritario | - | Sim |

### Compras Avulsas (Consumiveis)

| Item | Descricao |
|------|-----------|
| **Escudo de Sequencia** | Proteja sua sequencia por 1 dia perdido |
| **Assistencia de Dificuldade** | Pedido adicional de ajuda "Estou com dificuldade" |
| **Impulso de Meta** | Slot extra para metas ativas |
| **Slot de Guardiao** | Adicionar mais um guardiao a uma meta |

### Funcionalidades Exclusivas Premium (Recomendadas para Monetizacao)

1. **Escudos de Sequencia Ilimitados** - Alto valor, usuarios odeiam perder sequencias
2. **Insights e Sugestoes por IA** - Recomendacoes personalizadas baseadas em padroes de progresso
3. **Analytics Avancado** - Graficos, tendencias, previsoes
4. **Marketplace de Templates** - Criar e vender templates personalizados
5. **Integracao com WhatsApp** - Alto fator de conveniencia
6. **Planos de Equipe/Familia** - Multiplos usuarios, metas compartilhadas
