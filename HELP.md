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
{ "name": "John", "email": "john@example.com", "password": "Password123!" }
```

**Password requirements:** At least 8 characters with uppercase, lowercase, number, and special character (@$!%*?&).

---

### Dashboard (`/api/v1/dashboard`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Get dashboard summary |
| GET | `/stats` | Get goal statistics |

**Dashboard response includes:**
- `activeGoalsCount` - Number of active goals
- `completedGoalsCount` - Number of completed goals
- `pendingReflectionsCount` - Reflections due
- `unreadNudgesCount` - Unread nudges from guardians
- `streakShieldsAvailable` - Available streak shields
- `streaksAtRisk` - Goals with streaks that may break

---

### User Profile (`/api/v1/users`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/profile` | Get current user profile |
| PUT | `/profile` | Update profile (name) |
| POST | `/streak-shields/use` | Use a streak shield |

**Profile response includes:**
- User info (id, name, email, joinedAt)
- `totalGoals` / `completedGoals` - Goal counts
- `streakShields` - Available streak shields

*Streak shields are earned at 50% and 100% milestones.*

---

### Goals (`/api/v1/goals`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Create goal |
| GET | `/` | List all (paginated) |
| GET | `/{id}` | Get by ID |
| GET | `/status/{status}` | Filter by status |
| GET | `/category/{category}` | Filter by category |
| GET | `/archived` | List archived goals |
| PUT | `/{id}` | Update goal |
| PUT | `/{id}/archive` | Archive goal (soft delete) |
| PUT | `/{id}/unarchive` | Restore archived goal |
| DELETE | `/{id}` | Delete goal permanently |

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

### Struggling Help (`/api/v1/goals`)

*Request help when feeling stuck on a goal.*

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

**Features:**
- AI-generated suggestions based on struggling type
- Optional guardian notification
- Free requests limit (1/month for free tier, unlimited for premium)

---

### Reflections (`/api/v1/goals`)

*Periodic check-ins with smart frequency based on goal duration.*

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/reflections/pending` | Get all pending reflections |
| GET | `/{goalId}/reflections/status` | Get reflection status for a goal |
| POST | `/{goalId}/reflections` | Create reflection for current period |
| GET | `/{goalId}/reflections` | Get reflection history (paginated) |
| GET | `/{goalId}/reflections/{id}` | Get specific reflection |
| PUT | `/{goalId}/reflections/{id}` | Update reflection |

**Smart Frequency Calculation:**
| Goal Duration | Reflection Frequency |
|---------------|---------------------|
| 1-14 days | Daily |
| 15-60 days | Every 3 days |
| 61-180 days | Weekly |
| 180+ days | Bi-weekly |

**Ratings:** `TERRIBLE`, `POOR`, `OKAY`, `GOOD`, `EXCELLENT`

**Request body:**
```json
{
  "rating": "GOOD",
  "wentWell": "Maintained consistency this week",
  "challenges": "Had trouble finding time on Wednesday",
  "adjustments": "Will schedule sessions earlier in the day",
  "moodNote": "Feeling motivated!",
  "willContinue": true,
  "motivationLevel": 8
}
```

---

### Social Proof (`/api/v1/social`)

*Anonymous aggregate statistics for motivation - see how others are doing without compromising privacy.*

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/stats` | Get global platform statistics |
| GET | `/stats/category/{category}` | Get stats for a specific category |
| GET | `/goals/{goalId}/insights` | Get insights based on similar goals |
| GET | `/goals/{goalId}/milestone-stats` | Compare your milestone progress |

**Categories:** `HEALTH`, `FINANCE`, `EDUCATION`, `CAREER`, `RELATIONSHIPS`, `PERSONAL_DEVELOPMENT`, `HOBBIES`, `OTHER`

**Global stats response:**
```json
{
  "totalActiveUsers": 1247,
  "totalGoalsCreated": 3456,
  "totalGoalsCompleted": 892,
  "overallCompletionRate": 25.8,
  "goalsByCategory": {"HEALTH": 1200, "FINANCE": 800, ...},
  "completionRateByCategory": {"HEALTH": 32.5, "FINANCE": 28.1, ...},
  "averageStreakAcrossUsers": 12,
  "longestStreakEver": 365
}
```

**Goal insights response:**
```json
{
  "goalId": 1,
  "category": "HEALTH",
  "usersWithSimilarGoals": 523,
  "similarGoalsCompletionRate": 32.5,
  "averageDaysToComplete": 60,
  "commonObstacles": ["LACK_OF_TIME", "LACK_OF_MOTIVATION"],
  "suggestedStrategies": ["Start with 5-minute daily sessions", ...],
  "encouragementMessage": "You've joined 523 others on this journey!"
}
```

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

### Stripe Payment Integration
- **Subscription management** - Premium tier with Stripe Checkout
- **One-time purchases** - Consumables (streak shields, goal boosts) via Stripe
- **Customer portal** - Self-service subscription management
- **Webhook handling** - Real-time subscription status updates
- **Mobile support** - Stripe API integration for iOS/Android

### Engagement & Retention Features
- **Weekly Reflections** - Prompts to rate your week, adjust goals, and celebrate wins
- **Achievements/Badges** - Gamification with badges like "30-Day Streak", "First Milestone", "Guardian Hero"
- **Goal Health Score** - AI-calculated score based on progress consistency
- **Seasonal Challenges** - Monthly themed challenges to keep engagement fresh
- **Progress Photos** - Visual before/after tracking for fitness, creative, or home goals
- **Guardian Leaderboard** - Rankings for best accountability partners
- **Identity Building** - Messages like "60 days consistent - you're a goal achiever!"

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
