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

---

## Social Proof System

**Status:** Brainstorming

**Problem:** Users feel isolated when working on goals. Seeing that others are succeeding with similar goals creates motivation and urgency.

### Option 2: Goal Communities (Full Social)

Users can opt-in to join communities based on goal category/type.

**New entities needed:**
- `GoalCommunity` - Group of users with similar goals
- `CommunityPost` - Updates shared to community
- `CommunityMember` - User membership with privacy settings

**Endpoints:**
```
GET    /api/v1/communities                    # Browse communities
POST   /api/v1/communities/{id}/join          # Join community
GET    /api/v1/communities/{id}/feed          # Community posts
POST   /api/v1/communities/{id}/posts         # Share progress
GET    /api/v1/communities/{id}/leaderboard   # Top performers
```

**Features:**
- Share milestones to community (opt-in)
- See anonymized progress of others
- Leaderboards (streaks, completion %)
- Community challenges

**Pros:** High engagement, real motivation
**Cons:** Moderation needed, complexity, privacy considerations

---

### Option 3: Goal Matching (Middle Ground)

Match users with similar goals without full social features.

**Endpoints:**
```
GET  /api/v1/goals/{id}/similar-users         # Find matches
POST /api/v1/goals/{id}/share-milestone       # Broadcast milestone
GET  /api/v1/feed/milestones                  # See others' milestones
```

**How it works:**
1. When user hits milestone, option to "share with community"
2. Other users with similar goals see: "Someone just hit 50% on their 'Run 5k' goal!"
3. Can react with encouragement (no direct messaging)

**New entity:**
```java
@Entity
public class SharedMilestone {
    private Goal goal;           // Reference (not exposed)
    private String categoryName; // "HEALTH - Running"
    private Integer percentage;
    private LocalDateTime sharedAt;
    private Boolean isAnonymous;
}
```

**Pros:**
- Low complexity - Just a few new tables and endpoints
- Privacy-safe - Anonymous by default
- No moderation - No user-generated text beyond preset reactions
- Motivating - Seeing others succeed creates urgency
- Extensible - Can evolve into full communities later

**Cons:** Less engaging than full communities

---

### Open Questions for Social Proof

- [ ] Should stats be real-time or cached (daily refresh)?
- [ ] How to handle categories with few users (statistical significance)?
- [ ] Should top performers be highlighted (privacy opt-in)?
- [ ] Integration with Guardian system (social proof from guardians)?

### Gamification Expansion
- Daily challenges
- Achievement unlocks with real rewards
- Seasonal events (New Year challenge, Summer fitness, etc.)

---

*Last updated: December 2024*
