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
