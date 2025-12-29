# Metasmart - Ideas in Progress

> This file contains brainstorming ideas and concepts under consideration. These are not yet implemented and may change significantly.

---

## Ranking & Competition System

**Status:** Brainstorming

**Problem:** Goals are hard to compare fairly (run 5km vs save $10,000). Pure self-reported progress is easy to game.

**Core Insight:** Use guardians as validators to add credibility and create meaningful competition.

### Accountability Score Concept

Instead of ranking raw progress, rank **validated, accountable progress**:

```
Accountability Score = (Streak Days × Consistency%) × Guardian Multiplier × Engagement Bonus
```

| Factor | Calculation |
|--------|-------------|
| **Streak Days** | Current streak (comparable across all goal types) |
| **Consistency %** | Days with progress / Days since goal started |
| **Guardian Multiplier** | 1 + (0.2 × active guardians) — max 2.0x with 5 guardians |
| **Engagement Bonus** | +10% per guardian nudge received this week |

### Guardian Validation Ideas

1. **Verified Progress** — Guardian can "confirm" a progress entry (saw you at the gym, watched you study). Verified entries worth 2x points.

2. **Guardian Check-ins** — Weekly prompt to guardians: "Did [user] make progress this week?" Yes/no affects score.

3. **Photo Proof** — User uploads progress photo, guardians can validate it. Creates social proof.

### Potential Leaderboards

| Leaderboard | What it measures |
|-------------|------------------|
| **Most Accountable** | Highest accountability score |
| **Longest Verified Streak** | Streak with guardian confirmations |
| **Best Guardian** | Nudge frequency, response rate, goals helped complete |
| **Most Supported** | Most guardian interactions received |
| **Rising Star** | Biggest score improvement this week |

### Competition Formats

- **Weekly Sprints** — Highest validated streak this week
- **Guardian Challenges** — "Get 3 nudge responses this week"
- **Team Goals** — Guardian + owner compete together as a unit

### Why This Approach Works

- Can't game it alone — need real guardians actively engaging
- Fair across goal types — measuring accountability, not raw numbers
- Encourages right behavior — get guardians, stay consistent, engage
- Social proof — validation from others adds credibility

### Open Questions

- [ ] Should rankings be global or category-based?
- [ ] How to handle privacy (opt-in to leaderboards)?
- [ ] Should there be rewards/badges for top performers?
- [ ] How to prevent guardian collusion (friends auto-validating)?
- [ ] Should verified streaks require minimum guardian count?

---

## Other Ideas to Explore

### AI Coach Integration
- Personalized suggestions based on progress patterns
- Predict when user might abandon goal
- Suggest optimal times for progress logging

### Social Features
- Public goal feed (opt-in)
- Follow other users for inspiration
- Goal communities by category

### Gamification Expansion
- Daily challenges
- Achievement unlocks with real rewards
- Seasonal events (New Year challenge, Summer fitness, etc.)

---

*Last updated: December 2024*
