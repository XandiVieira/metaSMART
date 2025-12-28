# Metasmart

> Goal Management Platform with emotional motivation and smart reminders.

## Planned Features

### MVP

- [x] User authentication (email/password + JWT)
- [x] SMART goal management (CRUD)
- [x] Progress tracking with milestones
- [x] Progress history with timestamps
- [x] Quick progress entry (increment buttons: +1, +5, +10)
- [x] Progress notes (optional comment per entry)
- [x] SMART pillars visualization (completion status per pillar)
- [x] Action plan per goal (CRUD with order and completion status)
- [ ] Shared goals (couples, friends, family)
- [ ] Emotional anchors (photos, audio attachments)
- [ ] Basic reminders (push, email)
- [ ] AI goal assistance (vague → SMART, help completing pillars)
- [ ] Goal guardian (accountability partner who can view progress and push you)

### Enhancements

- [ ] Visual gamification (progress trees, avatars, vaults)
- [x] Setup completion indicator (% of goal configuration done)
- [ ] Motivational messages (contextual encouragement)
- [ ] Celebrate achievements (share milestones with community)
- [ ] Adaptive nudges (location, time, behavior-based)
- [ ] Multi-channel notifications (WhatsApp, Telegram, Alexa, smartwatch)
- [x] Quick obstacle diary (daily check-in with solutions)
- [ ] Goal groups (community with similar objectives)
- [ ] AI review cycle (weekly/monthly analysis)
- [ ] Rearview mode (past achievements timeline)
- [x] Streak tracking (current and longest streak)
- [ ] Calendar integration
- [x] Goal templates (personal and public templates)
- [x] Default deadline suggestion (90 days via templates)
- [ ] Export & reports

---

## Features Reference

### User Authentication
JWT-based authentication with email/password. Register and login endpoints return a JWT token that must be included in the `Authorization` header as `Bearer <token>` for all protected endpoints.

**Endpoints:**
- `POST /api/v1/auth/register` - Register a new user
- `POST /api/v1/auth/login` - Login and receive JWT token

### Goal Management
SMART goals with categories (HEALTH, CAREER, EDUCATION, FINANCE, RELATIONSHIPS, PERSONAL, OTHER) and status tracking (ACTIVE, COMPLETED, PAUSED, ABANDONED).

**Endpoints:**
- `POST /api/v1/goals` - Create a new goal
- `GET /api/v1/goals` - List all goals (paginated)
- `GET /api/v1/goals/{id}` - Get goal by ID
- `GET /api/v1/goals/status/{status}` - Filter by status
- `GET /api/v1/goals/category/{category}` - Filter by category
- `PUT /api/v1/goals/{id}` - Update goal
- `DELETE /api/v1/goals/{id}` - Delete goal

**Response includes computed fields:**
- `progressPercentage` - Current progress as percentage of target
- `smartPillars` - SMART pillar completion status (specific, measurable, achievable, relevant, timeBound)
- `setupCompletionPercentage` - How complete the goal configuration is
- `currentStreak` - Consecutive days with progress entries
- `longestStreak` - Best streak achieved

### Progress Tracking
Track progress entries with optional notes. Automatic goal completion when target is reached.

**Endpoints:**
- `POST /api/v1/goals/{goalId}/progress` - Add progress entry
- `GET /api/v1/goals/{goalId}/progress` - Get progress history (paginated, supports date range filtering)
- `PUT /api/v1/goals/{goalId}/progress/{progressId}` - Update progress entry
- `DELETE /api/v1/goals/{goalId}/progress/{progressId}` - Delete progress entry

### Milestones
Default milestones (25%, 50%, 75%, 100%) are auto-created with each goal. Custom milestones can be added.

**Endpoints:**
- `POST /api/v1/goals/{goalId}/milestones` - Add custom milestone
- `GET /api/v1/goals/{goalId}/milestones` - List all milestones
- `DELETE /api/v1/goals/{goalId}/milestones/{milestoneId}` - Delete milestone

### Action Plan
Ordered list of action items per goal with due dates and completion tracking.

**Endpoints:**
- `POST /api/v1/goals/{goalId}/action-items` - Create action item
- `GET /api/v1/goals/{goalId}/action-items` - List action items (ordered)
- `PUT /api/v1/goals/{goalId}/action-items/{itemId}` - Update action item
- `DELETE /api/v1/goals/{goalId}/action-items/{itemId}` - Delete action item

### Obstacle Diary
Quick obstacle/solution journal per goal with resolution tracking.

**Endpoints:**
- `POST /api/v1/goals/{goalId}/obstacles` - Log an obstacle
- `GET /api/v1/goals/{goalId}/obstacles` - List obstacles (paginated, supports date range filtering)
- `PUT /api/v1/goals/{goalId}/obstacles/{obstacleId}` - Update obstacle (mark resolved, add solution)
- `DELETE /api/v1/goals/{goalId}/obstacles/{obstacleId}` - Delete obstacle

### Goal Templates
Create reusable templates from your goals. Templates can be personal (private) or public (shared with all users).

**Endpoints:**
- `POST /api/v1/goal-templates` - Create template
- `GET /api/v1/goal-templates` - List your templates (paginated)
- `GET /api/v1/goal-templates/available` - List all templates you can use (yours + public)
- `GET /api/v1/goal-templates/public` - List public templates only
- `GET /api/v1/goal-templates/{id}` - Get template by ID
- `GET /api/v1/goal-templates/{id}/goal` - Generate a GoalRequest from template
- `PUT /api/v1/goal-templates/{id}` - Update template
- `DELETE /api/v1/goal-templates/{id}` - Delete template

---

# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/4.0.1/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/4.0.1/maven-plugin/build-image.html)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/4.0.1/reference/using/devtools.html)
* [Spring Web](https://docs.spring.io/spring-boot/4.0.1/reference/web/servlet.html)
* [Spring Security](https://docs.spring.io/spring-boot/4.0.1/reference/web/spring-security.html)
* [Spring Data JPA](https://docs.spring.io/spring-boot/4.0.1/reference/data/sql.html#data.sql.jpa-and-spring-data)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Securing a Web Application](https://spring.io/guides/gs/securing-web/)
* [Spring Boot and OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
* [Authenticating a User with LDAP](https://spring.io/guides/gs/authenticating-ldap/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.

