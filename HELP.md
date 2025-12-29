# Metasmart API

> Goal Management Platform with SMART goals, progress tracking, and accountability partners.

## Quick Start

**Base URL:** `http://localhost:8080/relyon/metasmart`

**Swagger UI:** `http://localhost:8080/relyon/metasmart/swagger-ui.html`

**Authentication:** All endpoints (except `/api/v1/auth/*`) require JWT token in header:
```
Authorization: Bearer <token>
```

---

## Endpoints Summary

### Auth (`/api/v1/auth`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/register` | Register user → returns `{ token }` |
| POST | `/login` | Login → returns `{ token }` |

**Request body:**
```json
{ "name": "John", "email": "john@example.com", "password": "123456" }
```

---

### Goals (`/api/v1/goals`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Create goal |
| GET | `/` | List all (paginated) |
| GET | `/{id}` | Get by ID |
| GET | `/status/{status}` | Filter by status |
| GET | `/category/{category}` | Filter by category |
| PUT | `/{id}` | Update goal |
| DELETE | `/{id}` | Delete goal |

**Categories:** `HEALTH`, `CAREER`, `EDUCATION`, `FINANCE`, `RELATIONSHIPS`, `PERSONAL`, `OTHER`

**Statuses:** `ACTIVE`, `COMPLETED`, `PAUSED`, `ABANDONED`

**Response includes computed fields:**
- `progressPercentage` - Current % of target
- `smartPillars` - Which SMART criteria are met
- `setupCompletionPercentage` - How complete the goal config is
- `currentStreak` / `longestStreak` - Consecutive days with progress

---

### Progress (`/api/v1/goals/{goalId}/progress`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Add progress entry |
| GET | `/` | Get history (paginated) |
| GET | `/?startDate=&endDate=` | Filter by date range |
| PUT | `/{progressId}` | Update entry |
| DELETE | `/{progressId}` | Delete entry |

**Request body:**
```json
{ "progressValue": 1, "note": "Optional note" }
```

---

### Milestones (`/api/v1/goals/{goalId}/milestones`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Add milestone |
| GET | `/` | List all |
| DELETE | `/{milestoneId}` | Delete milestone |

*Default milestones (25%, 50%, 75%, 100%) are auto-created.*

---

### Action Items (`/api/v1/goals/{goalId}/action-items`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Create action item |
| GET | `/` | List all (ordered) |
| PUT | `/{itemId}` | Update item |
| DELETE | `/{itemId}` | Delete item |

---

### Obstacles (`/api/v1/goals/{goalId}/obstacles`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Log obstacle |
| GET | `/` | List all (paginated) |
| PUT | `/{obstacleId}` | Update/resolve |
| DELETE | `/{obstacleId}` | Delete |

---

### Goal Templates (`/api/v1/goal-templates`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Create template |
| GET | `/` | My templates |
| GET | `/available` | All accessible templates |
| GET | `/public` | Public templates only |
| GET | `/{id}` | Get by ID |
| GET | `/{id}/goal` | Generate goal from template |
| PUT | `/{id}` | Update template |
| DELETE | `/{id}` | Delete template |

---

### Goal Guardian - Owner Endpoints (`/api/v1/goals/{goalId}/guardians`)

*For goal owners to manage their accountability partners.*

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Invite guardian (by email) |
| GET | `/` | List guardians |
| DELETE | `/{guardianshipId}` | Remove guardian |
| GET | `/nudges` | Get received nudges |
| PUT | `/nudges/{nudgeId}/read` | Mark as read |
| PUT | `/nudges/{nudgeId}/react` | React to nudge |

**Invite request:**
```json
{
  "guardianEmail": "friend@example.com",
  "permissions": ["VIEW_PROGRESS", "SEND_NUDGE"],
  "inviteMessage": "Be my accountability partner!"
}
```

**Permissions:** `VIEW_PROGRESS`, `VIEW_OBSTACLES`, `VIEW_ACTION_PLAN`, `VIEW_STREAK`, `SEND_NUDGE`

---

### Goal Guardian - Guardian Endpoints (`/api/v1/guardian`)

*For users acting as guardians for others' goals.*

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/invitations` | Pending invitations |
| PUT | `/invitations/{id}/accept` | Accept invite |
| PUT | `/invitations/{id}/decline` | Decline invite |
| GET | `/goals` | Goals I'm guarding |
| GET | `/goals/{goalId}` | View goal details (filtered by permissions) |
| POST | `/goals/{goalId}/nudges` | Send nudge |
| GET | `/nudges/sent` | My sent nudges |
| GET | `/nudges/unread-count` | Unread count (as owner) |

**Send nudge:**
```json
{
  "message": "Keep going!",
  "nudgeType": "ENCOURAGEMENT"
}
```

**Nudge types:** `ENCOURAGEMENT`, `REMINDER`, `CELEBRATION`, `CHECK_IN`

---

## Pagination

All paginated endpoints accept:
- `page` - Page number (0-indexed)
- `size` - Items per page (default: 10)
- `sort` - Sort field and direction (e.g., `createdAt,desc`)

**Response format:**
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

## Error Responses

```json
{
  "message": "Error description",
  "errors": { "field": "validation error" }
}
```

| Status | Meaning |
|--------|---------|
| 400 | Bad request / Validation error |
| 401 | Missing or invalid token |
| 403 | Access denied |
| 404 | Resource not found |
| 409 | Conflict (duplicate) |

---

## Postman Collection

Import `postman/Metasmart.postman_collection.json` for ready-to-use requests.

### Running E2E Test Sequence

The collection includes an **"E2E Test Sequence"** folder designed to test the entire application flow:

1. Open Postman and import the collection
2. Click the **"E2E Test Sequence"** folder
3. Click **"Run"** (or right-click → "Run folder")
4. In the Collection Runner, click **"Run Metasmart API"**

The sequence will:
- Register a new user and authenticate
- Create a goal with full SMART setup
- Add progress entries and milestones
- Create action items and obstacles
- Set up a guardian relationship (register guardian, invite, accept)
- Send and react to nudges
- Clean up all created data

All tests include assertions to verify correct API behavior.

---

## Future Features

The following features are planned for future releases:

### Push Notifications
- **Goal reminders** - Scheduled notifications to remind users about their goals
- **Progress reminders** - Daily/weekly prompts to log progress
- **Milestone celebrations** - Notifications when milestones are reached
- **Streak alerts** - Warnings before losing a streak ("Your 45-day streak is at risk!")
- **Guardian notifications** - Real-time push when guardians send nudges

### WhatsApp Integration
- **WhatsApp notifications** - Receive reminders and nudges via WhatsApp
- **Quick progress logging** - Reply to WhatsApp messages to log progress
- **Guardian nudges via WhatsApp** - Guardians can send encouragement through WhatsApp
- **Daily/weekly summaries** - Progress summaries delivered to WhatsApp

### Engagement & Retention Features
- **Weekly Reflections** - Prompts to rate your week, adjust goals, and celebrate wins
- **Achievements/Badges** - Gamification with badges like "30-Day Streak", "First Milestone", "Guardian Hero"
- **Goal Health Score** - AI-calculated score based on progress consistency
- **Seasonal Challenges** - Monthly themed challenges to keep engagement fresh
- **Progress Photos** - Visual before/after tracking for fitness, creative, or home goals
- **Guardian Leaderboard** - Rankings for best accountability partners
- **Social Proof** - Stats like "87% of users with similar goals hit their milestone"
- **Identity Building** - Messages like "60 days consistent - you're a goal achiever!"

### Struggling Button
- **Request help** - One-tap button when feeling stuck on a goal
- **Smart suggestions** - AI-powered recommendations to break down goals or extend deadlines
- **Guardian alert** - Optionally notify guardians when you're struggling

---

### Struggling Help (`/api/v1/goals`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/struggling/status` | Get remaining free requests |
| POST | `/{goalId}/struggling` | Request help for a goal |
| GET | `/struggling/history` | View help request history |
| PUT | `/struggling/{requestId}/feedback` | Mark if help was useful |

**Struggling types:** `LACK_OF_TIME`, `LACK_OF_MOTIVATION`, `GOAL_TOO_AMBITIOUS`, `UNCLEAR_NEXT_STEPS`, `EXTERNAL_OBSTACLES`, `LOST_INTEREST`, `OTHER`

**Request body:**
```json
{
  "strugglingType": "LACK_OF_MOTIVATION",
  "message": "I can't seem to find the energy to continue",
  "notifyGuardians": true
}
```

---

### Subscription (`/api/v1/subscription`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Get current subscription |
| GET | `/entitlements` | Get feature access and limits |
| GET | `/purchases` | Get purchase history |

---

## Premium Features (Planned)

### Subscription Tiers

| Feature | Free | Premium |
|---------|------|---------|
| Active goals | 3 | Unlimited |
| Guardians per goal | 1 | 5 |
| Progress history | 30 days | Unlimited |
| Goal templates | Public only | Create & share |
| Weekly reflections | Basic | AI-powered insights |
| Streak shields | 1/month | 3/month |
| Struggling button | 1/month | Unlimited |
| Achievements/Badges | Basic set | Full collection |
| Export data | - | CSV/PDF export |
| Priority support | - | Yes |

### One-Time Purchases (Consumables)

| Item | Description |
|------|-------------|
| **Streak Shield** | Protect your streak for 1 missed day |
| **Struggling Assist** | Additional "I'm struggling" help request |
| **Goal Boost** | Extra slot for active goals |
| **Guardian Slot** | Add one more guardian to a goal |

### Premium-Only Features (Recommended for Monetization)

1. **Unlimited Streak Shields** - High value, users hate losing streaks
2. **AI Insights & Suggestions** - Personalized recommendations based on progress patterns
3. **Advanced Analytics** - Charts, trends, predictions
4. **Goal Templates Marketplace** - Create and sell custom templates
5. **WhatsApp Integration** - High convenience factor
6. **Team/Family Plans** - Multiple users, shared goals
