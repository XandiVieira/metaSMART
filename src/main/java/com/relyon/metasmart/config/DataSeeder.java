package com.relyon.metasmart.config;

import com.relyon.metasmart.entity.actionplan.*;
import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.goal.GoalCategory;
import com.relyon.metasmart.entity.goal.GoalNote;
import com.relyon.metasmart.entity.goal.GoalStatus;
import com.relyon.metasmart.entity.guardian.GoalGuardian;
import com.relyon.metasmart.entity.guardian.GuardianNudge;
import com.relyon.metasmart.entity.guardian.GuardianPermission;
import com.relyon.metasmart.entity.guardian.GuardianStatus;
import com.relyon.metasmart.entity.guardian.NudgeType;
import com.relyon.metasmart.entity.notification.NotificationPreferences;
import com.relyon.metasmart.entity.obstacle.ObstacleEntry;
import com.relyon.metasmart.entity.progress.Milestone;
import com.relyon.metasmart.entity.progress.ProgressEntry;
import com.relyon.metasmart.entity.reflection.GoalReflection;
import com.relyon.metasmart.entity.reflection.ReflectionRating;
import com.relyon.metasmart.entity.streak.StreakInfo;
import com.relyon.metasmart.entity.subscription.SubscriptionStatus;
import com.relyon.metasmart.entity.subscription.SubscriptionTier;
import com.relyon.metasmart.entity.subscription.UserSubscription;
import com.relyon.metasmart.entity.template.GoalTemplate;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.entity.user.UserPreferences;
import com.relyon.metasmart.repository.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final String DEFAULT_PASSWORD = "Test@123";
    private static final String POLY_EMAIL = "poly.fucilini.s@gmail.com";
    private static final String POLY_PASSWORD = "Poly3011$";

    private final UserRepository userRepository;
    private final GoalRepository goalRepository;
    private final ProgressEntryRepository progressEntryRepository;
    private final MilestoneRepository milestoneRepository;
    private final ActionItemRepository actionItemRepository;
    private final GoalGuardianRepository goalGuardianRepository;
    private final ObstacleEntryRepository obstacleEntryRepository;
    private final GoalTemplateRepository goalTemplateRepository;
    private final UserPreferencesRepository userPreferencesRepository;
    private final NotificationPreferencesRepository notificationPreferencesRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final StreakInfoRepository streakInfoRepository;
    private final TaskCompletionRepository taskCompletionRepository;
    private final TaskScheduleSlotRepository taskScheduleSlotRepository;
    private final GoalReflectionRepository goalReflectionRepository;
    private final GoalNoteRepository goalNoteRepository;
    private final GuardianNudgeRepository guardianNudgeRepository;
    private final PasswordEncoder passwordEncoder;

    private final Random random = new Random();

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already populated, skipping seed...");
            return;
        }

        log.info("Starting database seeding...");

        var users = createUsers();
        var goals = createGoals(users);
        var actionItems = createActionItems(goals);
        createProgressEntries(goals);
        createMilestones(goals);
        var guardians = createGuardians(goals, users);
        createObstacles(goals);
        createTemplates(users);

        // Create additional entities for comprehensive testing
        createUserPreferences(users);
        createNotificationPreferences(users);
        createUserSubscriptions(users);
        createStreakInfo(users, goals, actionItems);
        createTaskScheduleSlotsAndCompletions(actionItems);
        createGoalReflections(users, goals);
        createGoalNotes(goals);
        createGuardianNudges(guardians);

        log.info("Database seeding completed!");
        log.info("Created {} users, {} goals", users.size(), goals.size());
    }

    private List<User> createUsers() {
        log.info("Creating users...");
        var users = new ArrayList<User>();
        var encodedDefaultPassword = passwordEncoder.encode(DEFAULT_PASSWORD);
        var encodedPolyPassword = passwordEncoder.encode(POLY_PASSWORD);

        var userData = List.of(
                new String[]{"Maria Silva", "maria@test.com", encodedDefaultPassword},
                new String[]{"Joao Santos", "joao@test.com", encodedDefaultPassword},
                new String[]{"Ana Oliveira", "ana@test.com", encodedDefaultPassword},
                new String[]{"Poly Fucilini", POLY_EMAIL, encodedPolyPassword}
        );

        for (int userIndex = 0; userIndex < userData.size(); userIndex++) {
            var data = userData.get(userIndex);
            var user = User.builder()
                    .name(data[0])
                    .email(data[1])
                    .password(data[2])
                    .role(userIndex == 0 ? User.Role.ADMIN : User.Role.USER)
                    .streakShields(random.nextInt(5))
                    .build();
            users.add(userRepository.save(user));
        }

        log.info("Created {} users (default password: {}, Poly password: {})", users.size(), DEFAULT_PASSWORD, POLY_PASSWORD);
        return users;
    }

    private void createUserPreferences(List<User> users) {
        log.info("Creating user preferences...");
        for (var user : users) {
            var prefs = UserPreferences.builder()
                    .user(user)
                    .timezone(user.getEmail().equals(POLY_EMAIL) ? "America/Sao_Paulo" : "UTC")
                    .weekStartDay(user.getEmail().equals(POLY_EMAIL) ? 0 : 1)
                    .language(user.getEmail().equals(POLY_EMAIL) ? "pt-BR" : "en")
                    .emailNotifications(true)
                    .pushNotifications(true)
                    .weeklyDigest(true)
                    .streakReminders(true)
                    .guardianNudges(true)
                    .preferredReminderTime(user.getEmail().equals(POLY_EMAIL) ? "08:00" : "09:00")
                    .build();
            userPreferencesRepository.save(prefs);
        }
    }

    private void createNotificationPreferences(List<User> users) {
        log.info("Creating notification preferences...");
        for (var user : users) {
            var prefs = NotificationPreferences.builder()
                    .user(user)
                    .pushEnabled(true)
                    .pushGoalReminders(true)
                    .pushProgressReminders(true)
                    .pushMilestones(true)
                    .pushStreakAlerts(true)
                    .pushGuardianNudges(true)
                    .emailEnabled(true)
                    .emailWeeklyDigest(true)
                    .emailMilestones(true)
                    .emailStreakAtRisk(true)
                    .whatsappEnabled(user.getEmail().equals(POLY_EMAIL))
                    .whatsappNumber(user.getEmail().equals(POLY_EMAIL) ? "+5511999999999" : null)
                    .quietHoursEnabled(user.getEmail().equals(POLY_EMAIL))
                    .quietHoursStart(user.getEmail().equals(POLY_EMAIL) ? "22:00" : null)
                    .quietHoursEnd(user.getEmail().equals(POLY_EMAIL) ? "07:00" : null)
                    .build();
            notificationPreferencesRepository.save(prefs);
        }
    }

    private void createUserSubscriptions(List<User> users) {
        log.info("Creating user subscriptions...");
        for (var user : users) {
            var isPremium = user.getEmail().equals(POLY_EMAIL) || user.getEmail().equals("maria@test.com");
            var subscription = UserSubscription.builder()
                    .user(user)
                    .tier(isPremium ? SubscriptionTier.PREMIUM : SubscriptionTier.FREE)
                    .status(SubscriptionStatus.ACTIVE)
                    .startDate(LocalDateTime.now().minusDays(isPremium ? 30 : 90))
                    .endDate(isPremium ? LocalDateTime.now().plusDays(335) : null)
                    .priceAmount(isPremium ? new BigDecimal("29.90") : null)
                    .priceCurrency(isPremium ? "BRL" : null)
                    .billingPeriod(isPremium ? "monthly" : null)
                    .paymentProvider(isPremium ? "stripe" : null)
                    .build();
            userSubscriptionRepository.save(subscription);
        }
    }

    private void createStreakInfo(List<User> users, List<Goal> goals, List<ActionItem> actionItems) {
        log.info("Creating streak info...");

        // User-level streaks
        for (var user : users) {
            var streakInfo = StreakInfo.builder()
                    .user(user)
                    .currentMaintainedStreak(random.nextInt(30) + 1)
                    .bestMaintainedStreak(random.nextInt(50) + 30)
                    .currentPerfectStreak(random.nextInt(15))
                    .bestPerfectStreak(random.nextInt(30) + 15)
                    .lastUpdatedAt(LocalDateTime.now().minusHours(random.nextInt(24)))
                    .build();
            streakInfoRepository.save(streakInfo);
        }

        // Goal-level streaks for Poly's goals
        var polyGoals = goals.stream()
                .filter(g -> g.getOwner().getEmail().equals(POLY_EMAIL))
                .toList();
        for (var goal : polyGoals) {
            var streakInfo = StreakInfo.builder()
                    .user(goal.getOwner())
                    .goal(goal)
                    .currentMaintainedStreak(random.nextInt(20) + 5)
                    .bestMaintainedStreak(random.nextInt(40) + 20)
                    .currentPerfectStreak(random.nextInt(10))
                    .bestPerfectStreak(random.nextInt(20) + 10)
                    .lastUpdatedAt(LocalDateTime.now().minusHours(random.nextInt(48)))
                    .build();
            streakInfoRepository.save(streakInfo);
        }

        // Task-level streaks for some action items
        var polyActionItems = actionItems.stream()
                .filter(ai -> ai.getGoal().getOwner().getEmail().equals(POLY_EMAIL))
                .limit(5)
                .toList();
        for (var actionItem : polyActionItems) {
            var streakInfo = StreakInfo.builder()
                    .user(actionItem.getGoal().getOwner())
                    .goal(actionItem.getGoal())
                    .actionItem(actionItem)
                    .currentMaintainedStreak(random.nextInt(15) + 1)
                    .bestMaintainedStreak(random.nextInt(25) + 15)
                    .currentPerfectStreak(random.nextInt(7))
                    .bestPerfectStreak(random.nextInt(14) + 7)
                    .lastUpdatedAt(LocalDateTime.now().minusHours(random.nextInt(72)))
                    .build();
            streakInfoRepository.save(streakInfo);
        }
    }

    private void createTaskScheduleSlotsAndCompletions(List<ActionItem> actionItems) {
        log.info("Creating task schedule slots and completions...");

        var recurringItems = actionItems.stream()
                .filter(ai -> ai.getTaskType() != TaskType.ONE_TIME)
                .toList();

        for (var actionItem : recurringItems) {
            // Create schedule slots
            var slotCount = actionItem.getTaskType() == TaskType.DAILY_HABIT ? 7 :
                    actionItem.getTaskType() == TaskType.FREQUENCY_BASED ? 4 : 2;

            var slots = new ArrayList<TaskScheduleSlot>();
            for (int slotIndex = 0; slotIndex < slotCount; slotIndex++) {
                var slot = TaskScheduleSlot.builder()
                        .actionItem(actionItem)
                        .slotIndex(slotIndex)
                        .specificTime(String.format("%02d:00", 8 + random.nextInt(12)))
                        .createdVia(ScheduleSlotCreationType.values()[random.nextInt(ScheduleSlotCreationType.values().length)])
                        .effectiveFrom(LocalDate.now().minusDays(30))
                        .build();
                slots.add(taskScheduleSlotRepository.save(slot));
            }

            // Create completions for the past 14 days
            for (int dayOffset = 14; dayOffset >= 0; dayOffset--) {
                var scheduledDate = LocalDate.now().minusDays(dayOffset);
                var shouldComplete = random.nextDouble() < 0.75;

                var completion = TaskCompletion.builder()
                        .actionItem(actionItem)
                        .scheduleSlot(slots.isEmpty() ? null : slots.get(random.nextInt(slots.size())))
                        .periodStart(scheduledDate.withDayOfMonth(1))
                        .scheduledDate(scheduledDate)
                        .scheduledTime(String.format("%02d:00", 8 + random.nextInt(12)))
                        .status(shouldComplete ? CompletionStatus.COMPLETED :
                                (random.nextBoolean() ? CompletionStatus.MISSED : CompletionStatus.PENDING))
                        .completedAt(shouldComplete ? scheduledDate.atTime(10 + random.nextInt(10), random.nextInt(60)) : null)
                        .note(shouldComplete ? getRandomCompletionNote() : null)
                        .build();
                taskCompletionRepository.save(completion);
            }
        }
    }

    private String getRandomCompletionNote() {
        var notes = List.of(
                "Done! Feeling great today.",
                "Completed on time.",
                "Had to push through but made it!",
                "Easy session today.",
                "Challenging but rewarding.",
                "Better than expected!",
                "Kept it short but consistent.",
                "Great progress!"
        );
        return notes.get(random.nextInt(notes.size()));
    }

    private void createGoalReflections(List<User> users, List<Goal> goals) {
        log.info("Creating goal reflections...");

        var reflectionData = List.of(
                new String[]{"Maintained good consistency this week", "Time management was challenging", "Will wake up earlier"},
                new String[]{"Exceeded my targets!", "Some days were harder than others", "Need to prepare better"},
                new String[]{"Steady progress", "External distractions", "Minimize phone usage during tasks"},
                new String[]{"Feeling motivated", "Started strong but faded", "Set smaller daily goals"}
        );

        for (var goal : goals) {
            if (goal.getOwner().getEmail().equals(POLY_EMAIL) || random.nextDouble() < 0.3) {
                var reflectionCount = goal.getOwner().getEmail().equals(POLY_EMAIL) ? 4 : 1;

                for (int reflectionIndex = 0; reflectionIndex < reflectionCount; reflectionIndex++) {
                    var periodEnd = LocalDate.now().minusWeeks(reflectionIndex);
                    var periodStart = periodEnd.minusDays(6);
                    var data = reflectionData.get(random.nextInt(reflectionData.size()));

                    var reflection = GoalReflection.builder()
                            .goal(goal)
                            .user(goal.getOwner())
                            .periodStart(periodStart)
                            .periodEnd(periodEnd)
                            .rating(ReflectionRating.values()[random.nextInt(ReflectionRating.values().length)])
                            .wentWell(data[0])
                            .challenges(data[1])
                            .adjustments(data[2])
                            .moodNote("Feeling " + (random.nextBoolean() ? "positive" : "optimistic") + " about progress")
                            .willContinue(true)
                            .motivationLevel(6 + random.nextInt(5))
                            .build();
                    goalReflectionRepository.save(reflection);
                }
            }
        }
    }

    private void createGoalNotes(List<Goal> goals) {
        log.info("Creating goal notes...");

        var noteContents = List.of(
                new Object[]{"Remember to track progress daily!", GoalNote.NoteType.GENERAL},
                new Object[]{"Celebrating small wins along the way", GoalNote.NoteType.CELEBRATION},
                new Object[]{"Need to adjust my approach for better results", GoalNote.NoteType.REFLECTION},
                new Object[]{"Reached an important milestone today!", GoalNote.NoteType.MILESTONE},
                new Object[]{"Found a workaround for the challenge I was facing", GoalNote.NoteType.OBSTACLE},
                new Object[]{"Feeling proud of my consistency this week", GoalNote.NoteType.CELEBRATION},
                new Object[]{"Key insight: breaking down tasks helps a lot", GoalNote.NoteType.REFLECTION}
        );

        for (var goal : goals) {
            if (goal.getOwner().getEmail().equals(POLY_EMAIL) || random.nextDouble() < 0.4) {
                var noteCount = goal.getOwner().getEmail().equals(POLY_EMAIL) ? 3 : 1;

                for (int noteIndex = 0; noteIndex < noteCount; noteIndex++) {
                    var noteData = noteContents.get(random.nextInt(noteContents.size()));
                    var note = GoalNote.builder()
                            .goal(goal)
                            .content((String) noteData[0])
                            .noteType((GoalNote.NoteType) noteData[1])
                            .build();
                    goalNoteRepository.save(note);
                }
            }
        }
    }

    private void createGuardianNudges(List<GoalGuardian> guardians) {
        log.info("Creating guardian nudges...");

        var nudgeMessages = List.of(
                new Object[]{"Keep going! You're doing great!", NudgeType.ENCOURAGEMENT},
                new Object[]{"Don't forget to log your progress today!", NudgeType.REMINDER},
                new Object[]{"Congratulations on your streak!", NudgeType.CELEBRATION},
                new Object[]{"How's it going with your goal?", NudgeType.CHECK_IN},
                new Object[]{"You've got this! Stay focused!", NudgeType.ENCOURAGEMENT},
                new Object[]{"Remember your why!", NudgeType.REMINDER}
        );

        for (var guardian : guardians) {
            if (guardian.getStatus() != GuardianStatus.ACTIVE) {
                continue;
            }

            var nudgeCount = guardian.getGoal().getOwner().getEmail().equals(POLY_EMAIL) ? 3 : 1;

            for (int nudgeIndex = 0; nudgeIndex < nudgeCount; nudgeIndex++) {
                var nudgeData = nudgeMessages.get(random.nextInt(nudgeMessages.size()));
                var isRead = random.nextDouble() < 0.6;

                var nudge = GuardianNudge.builder()
                        .goalGuardian(guardian)
                        .message((String) nudgeData[0])
                        .nudgeType((NudgeType) nudgeData[1])
                        .readAt(isRead ? LocalDateTime.now().minusHours(random.nextInt(72)) : null)
                        .reaction(isRead && random.nextBoolean() ? "thumbs_up" : null)
                        .build();
                guardianNudgeRepository.save(nudge);
            }
        }
    }

    private List<Goal> createGoals(List<User> users) {
        log.info("Creating goals...");
        var goals = new ArrayList<Goal>();

        var maria = users.get(0);
        goals.addAll(createMariaGoals(maria));

        var joao = users.get(1);
        goals.addAll(createJoaoGoals(joao));

        var ana = users.get(2);
        goals.addAll(createAnaGoals(ana));

        var poly = users.get(3);
        goals.addAll(createPolyGoals(poly));

        return goals;
    }

    private List<Goal> createPolyGoals(User poly) {
        var goals = new ArrayList<Goal>();

        goals.add(goalRepository.save(Goal.builder()
                .owner(poly)
                .title("Build a successful SaaS product")
                .description("Develop and launch MetaSMART as a complete goal tracking platform")
                .goalCategory(GoalCategory.CAREER)
                .targetValue(new BigDecimal("100"))
                .unit("features")
                .currentProgress(new BigDecimal("65"))
                .motivation("Create something that helps people achieve their goals")
                .startDate(LocalDate.now().minusDays(90))
                .targetDate(LocalDate.now().plusDays(90))
                .goalStatus(GoalStatus.ACTIVE)
                .streak(28)
                .tags("saas,development,entrepreneurship")
                .build()));

        goals.add(goalRepository.save(Goal.builder()
                .owner(poly)
                .title("Exercise 5 times per week")
                .description("Maintain consistent workout routine for better health")
                .goalCategory(GoalCategory.HEALTH)
                .targetValue(new BigDecimal("260"))
                .unit("workouts")
                .currentProgress(new BigDecimal("156"))
                .motivation("Stay healthy and energetic for long coding sessions")
                .startDate(LocalDate.now().minusDays(180))
                .targetDate(LocalDate.now().plusDays(185))
                .goalStatus(GoalStatus.ACTIVE)
                .streak(15)
                .tags("fitness,health,exercise")
                .build()));

        goals.add(goalRepository.save(Goal.builder()
                .owner(poly)
                .title("Learn system design")
                .description("Master distributed systems and architecture patterns")
                .goalCategory(GoalCategory.EDUCATION)
                .targetValue(new BigDecimal("50"))
                .unit("chapters")
                .currentProgress(new BigDecimal("32"))
                .motivation("Become a better software architect")
                .startDate(LocalDate.now().minusDays(60))
                .targetDate(LocalDate.now().plusDays(120))
                .goalStatus(GoalStatus.ACTIVE)
                .streak(12)
                .tags("learning,architecture,tech")
                .build()));

        goals.add(goalRepository.save(Goal.builder()
                .owner(poly)
                .title("Save for investment portfolio")
                .description("Build emergency fund and start investing")
                .goalCategory(GoalCategory.FINANCE)
                .targetValue(new BigDecimal("50000"))
                .unit("BRL")
                .currentProgress(new BigDecimal("28500"))
                .motivation("Financial independence and security")
                .startDate(LocalDate.now().minusDays(240))
                .targetDate(LocalDate.now().plusDays(125))
                .goalStatus(GoalStatus.ACTIVE)
                .streak(8)
                .tags("savings,investment,financial-freedom")
                .build()));

        goals.add(goalRepository.save(Goal.builder()
                .owner(poly)
                .title("Daily meditation practice")
                .description("Meditate for at least 10 minutes every day")
                .goalCategory(GoalCategory.PERSONAL_DEVELOPMENT)
                .targetValue(new BigDecimal("365"))
                .unit("sessions")
                .currentProgress(new BigDecimal("89"))
                .motivation("Mental clarity and stress management")
                .startDate(LocalDate.now().minusDays(89))
                .targetDate(LocalDate.now().plusDays(276))
                .goalStatus(GoalStatus.ACTIVE)
                .streak(21)
                .tags("meditation,mindfulness,wellness")
                .build()));

        goals.add(goalRepository.save(Goal.builder()
                .owner(poly)
                .title("Read 30 books this year")
                .description("Mix of technical and personal development books")
                .goalCategory(GoalCategory.EDUCATION)
                .targetValue(new BigDecimal("30"))
                .unit("books")
                .currentProgress(new BigDecimal("18"))
                .motivation("Continuous learning and growth")
                .startDate(LocalDate.now().minusDays(200))
                .targetDate(LocalDate.now().plusDays(165))
                .goalStatus(GoalStatus.ACTIVE)
                .streak(7)
                .tags("reading,books,learning")
                .build()));

        return goals;
    }

    private List<Goal> createMariaGoals(User maria) {
        var goals = new ArrayList<Goal>();

        goals.add(goalRepository.save(Goal.builder()
                .owner(maria)
                .title("Correr 5km sem parar")
                .description("Treinar progressivamente ate conseguir correr 5km completos sem precisar andar")
                .goalCategory(GoalCategory.HEALTH)
                .targetValue(new BigDecimal("5"))
                .unit("km")
                .currentProgress(new BigDecimal("2.5"))
                .motivation("Quero melhorar minha saude cardiovascular e ter mais disposicao")
                .startDate(LocalDate.now().minusDays(45))
                .targetDate(LocalDate.now().plusDays(45))
                .goalStatus(GoalStatus.ACTIVE)
                .streak(12)
                .tags("saude,corrida,fitness")
                .build()));

        goals.add(goalRepository.save(Goal.builder()
                .owner(maria)
                .title("Economizar R$10.000 para viagem")
                .description("Guardar dinheiro mensalmente para realizar a viagem dos sonhos")
                .goalCategory(GoalCategory.FINANCE)
                .targetValue(new BigDecimal("10000"))
                .unit("reais")
                .currentProgress(new BigDecimal("6500"))
                .motivation("Realizar meu sonho de conhecer a Europa")
                .startDate(LocalDate.now().minusDays(120))
                .targetDate(LocalDate.now().plusDays(60))
                .goalStatus(GoalStatus.ACTIVE)
                .streak(8)
                .tags("financas,viagem,economia")
                .build()));

        goals.add(goalRepository.save(Goal.builder()
                .owner(maria)
                .title("Ler 24 livros este ano")
                .description("Ler 2 livros por mes para expandir conhecimentos e relaxar")
                .goalCategory(GoalCategory.EDUCATION)
                .targetValue(new BigDecimal("24"))
                .unit("livros")
                .currentProgress(new BigDecimal("14"))
                .motivation("Adoro ler e quero manter o habito constante")
                .startDate(LocalDate.now().minusDays(200))
                .targetDate(LocalDate.now().plusDays(165))
                .goalStatus(GoalStatus.ACTIVE)
                .streak(21)
                .tags("leitura,educacao,habito")
                .build()));

        goals.add(goalRepository.save(Goal.builder()
                .owner(maria)
                .title("Aprender ingles nivel B2")
                .description("Estudar ingles diariamente ate atingir fluencia intermediaria")
                .goalCategory(GoalCategory.EDUCATION)
                .targetValue(new BigDecimal("100"))
                .unit("licoes")
                .currentProgress(new BigDecimal("72"))
                .motivation("Preciso de ingles para crescer na carreira")
                .startDate(LocalDate.now().minusDays(90))
                .targetDate(LocalDate.now().plusDays(90))
                .goalStatus(GoalStatus.ACTIVE)
                .streak(15)
                .tags("ingles,idiomas,carreira")
                .build()));

        goals.add(goalRepository.save(Goal.builder()
                .owner(maria)
                .title("Meditar 30 dias seguidos")
                .description("Praticar meditacao diaria por 10 minutos")
                .goalCategory(GoalCategory.PERSONAL_DEVELOPMENT)
                .targetValue(new BigDecimal("30"))
                .unit("dias")
                .currentProgress(new BigDecimal("30"))
                .motivation("Reduzir ansiedade e melhorar foco")
                .startDate(LocalDate.now().minusDays(35))
                .targetDate(LocalDate.now().minusDays(5))
                .goalStatus(GoalStatus.COMPLETED)
                .streak(30)
                .tags("meditacao,mindfulness,saude-mental")
                .build()));

        return goals;
    }

    private List<Goal> createJoaoGoals(User joao) {
        var goals = new ArrayList<Goal>();

        goals.add(goalRepository.save(Goal.builder()
                .owner(joao)
                .title("Perder 15kg")
                .description("Emagrecer de forma saudavel com dieta equilibrada e exercicios")
                .goalCategory(GoalCategory.HEALTH)
                .targetValue(new BigDecimal("15"))
                .unit("kg")
                .currentProgress(new BigDecimal("7.5"))
                .motivation("Quero melhorar minha autoestima e saude geral")
                .startDate(LocalDate.now().minusDays(90))
                .targetDate(LocalDate.now().plusDays(90))
                .goalStatus(GoalStatus.ACTIVE)
                .streak(5)
                .tags("emagrecimento,saude,dieta")
                .build()));

        goals.add(goalRepository.save(Goal.builder()
                .owner(joao)
                .title("Conseguir promocao no trabalho")
                .description("Desenvolver habilidades e entregar resultados para ser promovido")
                .goalCategory(GoalCategory.CAREER)
                .targetValue(new BigDecimal("100"))
                .unit("pontos")
                .currentProgress(new BigDecimal("65"))
                .motivation("Crescer profissionalmente e ter melhor salario")
                .startDate(LocalDate.now().minusDays(60))
                .targetDate(LocalDate.now().plusDays(120))
                .goalStatus(GoalStatus.ACTIVE)
                .streak(3)
                .tags("carreira,promocao,trabalho")
                .build()));

        goals.add(goalRepository.save(Goal.builder()
                .owner(joao)
                .title("Quitar todas as dividas")
                .description("Pagar cartao de credito e emprestimos pendentes")
                .goalCategory(GoalCategory.FINANCE)
                .targetValue(new BigDecimal("8000"))
                .unit("reais")
                .currentProgress(new BigDecimal("5200"))
                .motivation("Ficar livre de dividas e ter paz financeira")
                .startDate(LocalDate.now().minusDays(150))
                .targetDate(LocalDate.now().plusDays(30))
                .goalStatus(GoalStatus.ACTIVE)
                .streak(10)
                .tags("dividas,financas,organizacao")
                .build()));

        goals.add(goalRepository.save(Goal.builder()
                .owner(joao)
                .title("Aprender a tocar violao")
                .description("Praticar violao 30 minutos por dia ate tocar musicas completas")
                .goalCategory(GoalCategory.HOBBIES)
                .targetValue(new BigDecimal("50"))
                .unit("musicas")
                .currentProgress(new BigDecimal("12"))
                .motivation("Sempre quis tocar um instrumento musical")
                .startDate(LocalDate.now().minusDays(60))
                .targetDate(LocalDate.now().plusDays(120))
                .goalStatus(GoalStatus.ACTIVE)
                .streak(7)
                .tags("musica,violao,hobby")
                .build()));

        goals.add(goalRepository.save(Goal.builder()
                .owner(joao)
                .title("Fazer academia 4x por semana")
                .description("Manter frequencia regular de treinos na academia")
                .goalCategory(GoalCategory.HEALTH)
                .targetValue(new BigDecimal("48"))
                .unit("treinos")
                .currentProgress(new BigDecimal("28"))
                .motivation("Complementar a perda de peso com ganho de massa muscular")
                .startDate(LocalDate.now().minusDays(84))
                .targetDate(LocalDate.now().plusDays(84))
                .goalStatus(GoalStatus.ACTIVE)
                .streak(2)
                .tags("academia,treino,musculacao")
                .build()));

        goals.add(goalRepository.save(Goal.builder()
                .owner(joao)
                .title("Terminar curso de programacao")
                .description("Completar bootcamp de desenvolvimento web full-stack")
                .goalCategory(GoalCategory.EDUCATION)
                .targetValue(new BigDecimal("120"))
                .unit("horas")
                .currentProgress(new BigDecimal("85"))
                .motivation("Mudar de carreira para area de tecnologia")
                .startDate(LocalDate.now().minusDays(75))
                .targetDate(LocalDate.now().plusDays(45))
                .goalStatus(GoalStatus.ACTIVE)
                .streak(14)
                .tags("programacao,curso,tecnologia")
                .build()));

        return goals;
    }

    private List<Goal> createAnaGoals(User ana) {
        var goals = new ArrayList<Goal>();

        goals.add(goalRepository.save(Goal.builder()
                .owner(ana)
                .title("Organizar a casa inteira")
                .description("Aplicar metodo de organizacao em todos os comodos")
                .goalCategory(GoalCategory.OTHER)
                .targetValue(new BigDecimal("8"))
                .unit("comodos")
                .currentProgress(new BigDecimal("5"))
                .motivation("Ter um ambiente mais agradavel e funcional")
                .startDate(LocalDate.now().minusDays(30))
                .targetDate(LocalDate.now().plusDays(30))
                .goalStatus(GoalStatus.ACTIVE)
                .streak(4)
                .tags("organizacao,casa,limpeza")
                .build()));

        goals.add(goalRepository.save(Goal.builder()
                .owner(ana)
                .title("Passar mais tempo com a familia")
                .description("Dedicar pelo menos 2 horas por dia para atividades em familia")
                .goalCategory(GoalCategory.RELATIONSHIPS)
                .targetValue(new BigDecimal("60"))
                .unit("horas")
                .currentProgress(new BigDecimal("42"))
                .motivation("Fortalecer os lacos familiares")
                .startDate(LocalDate.now().minusDays(30))
                .targetDate(LocalDate.now().plusDays(30))
                .goalStatus(GoalStatus.ACTIVE)
                .streak(18)
                .tags("familia,relacionamentos,qualidade-tempo")
                .build()));

        goals.add(goalRepository.save(Goal.builder()
                .owner(ana)
                .title("Comecar um negocio proprio")
                .description("Planejar e lancar loja online de artesanato")
                .goalCategory(GoalCategory.CAREER)
                .targetValue(new BigDecimal("100"))
                .unit("tarefas")
                .currentProgress(new BigDecimal("35"))
                .motivation("Ter independencia financeira e fazer o que amo")
                .startDate(LocalDate.now().minusDays(45))
                .targetDate(LocalDate.now().plusDays(135))
                .goalStatus(GoalStatus.ACTIVE)
                .streak(6)
                .tags("empreendedorismo,negocio,artesanato")
                .build()));

        goals.add(goalRepository.save(Goal.builder()
                .owner(ana)
                .title("Beber 2 litros de agua por dia")
                .description("Manter hidratacao adequada diariamente")
                .goalCategory(GoalCategory.HEALTH)
                .targetValue(new BigDecimal("90"))
                .unit("dias")
                .currentProgress(new BigDecimal("67"))
                .motivation("Melhorar a saude da pele e disposicao geral")
                .startDate(LocalDate.now().minusDays(67))
                .targetDate(LocalDate.now().plusDays(23))
                .goalStatus(GoalStatus.ACTIVE)
                .streak(25)
                .tags("agua,hidratacao,saude")
                .build()));

        goals.add(goalRepository.save(Goal.builder()
                .owner(ana)
                .title("Fazer curso de confeitaria")
                .description("Aprender tecnicas profissionais de confeitaria")
                .goalCategory(GoalCategory.EDUCATION)
                .targetValue(new BigDecimal("40"))
                .unit("aulas")
                .currentProgress(new BigDecimal("40"))
                .motivation("Complementar meu negocio de artesanato com doces")
                .startDate(LocalDate.now().minusDays(60))
                .targetDate(LocalDate.now().minusDays(5))
                .goalStatus(GoalStatus.COMPLETED)
                .streak(40)
                .tags("confeitaria,curso,gastronomia")
                .build()));

        goals.add(goalRepository.save(Goal.builder()
                .owner(ana)
                .title("Economizar para reformar a cozinha")
                .description("Juntar dinheiro para fazer a reforma desejada")
                .goalCategory(GoalCategory.FINANCE)
                .targetValue(new BigDecimal("15000"))
                .unit("reais")
                .currentProgress(new BigDecimal("8500"))
                .motivation("Ter uma cozinha funcional para o negocio de confeitaria")
                .startDate(LocalDate.now().minusDays(120))
                .targetDate(LocalDate.now().plusDays(60))
                .goalStatus(GoalStatus.ACTIVE)
                .streak(9)
                .tags("economia,reforma,investimento")
                .build()));

        return goals;
    }

    private void createProgressEntries(List<Goal> goals) {
        log.info("Creating progress entries...");

        var notes = List.of(
                "Progresso do dia, mantendo o foco!",
                "Dia produtivo, consegui avancar bastante",
                "Enfrentei algumas dificuldades mas nao desisti",
                "Excelente progresso hoje",
                "Pequeno avanco, mas consistencia e a chave",
                "Muito motivado(a) hoje!",
                "Dia desafiador, mas consegui contribuir",
                "Mantendo a regularidade",
                "Celebrando pequenas vitorias",
                "Focado(a) no objetivo final"
        );

        for (var goal : goals) {
            var entryCount = goal.getOwner().getEmail().equals(POLY_EMAIL) ? 15 : 8 + random.nextInt(8);
            var progressPerEntry = goal.getCurrentProgress()
                    .divide(BigDecimal.valueOf(entryCount), 2, RoundingMode.HALF_UP);

            for (int entryIndex = 0; entryIndex < entryCount; entryIndex++) {
                var variation = 0.7 + (random.nextDouble() * 0.6);
                var entry = ProgressEntry.builder()
                        .goal(goal)
                        .progressValue(progressPerEntry.multiply(BigDecimal.valueOf(variation)).setScale(2, RoundingMode.HALF_UP))
                        .note(notes.get(random.nextInt(notes.size())))
                        .build();
                progressEntryRepository.save(entry);
            }
        }
    }

    private void createMilestones(List<Goal> goals) {
        log.info("Creating milestones...");
        var milestoneData = List.of(
                new Object[]{25, "Primeiro quarto concluido!"},
                new Object[]{50, "Metade do caminho!"},
                new Object[]{75, "Tres quartos completos!"},
                new Object[]{100, "Meta alcancada!"}
        );

        for (var goal : goals) {
            var progressPercentage = goal.getCurrentProgress()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(goal.getTargetValue(), 0, RoundingMode.HALF_UP)
                    .intValue();

            for (var data : milestoneData) {
                var percentage = (Integer) data[0];
                var description = (String) data[1];
                var achieved = progressPercentage >= percentage;

                var milestone = Milestone.builder()
                        .goal(goal)
                        .percentage(percentage)
                        .description(description)
                        .achieved(achieved)
                        .achievedAt(achieved ? LocalDateTime.now().minusDays(random.nextInt(30)) : null)
                        .build();
                milestoneRepository.save(milestone);
            }
        }
    }

    private List<ActionItem> createActionItems(List<Goal> goals) {
        log.info("Creating action items...");
        var allActionItems = new ArrayList<ActionItem>();

        var actionItemsPerCategory = getActionItemsPerCategory();

        for (var goal : goals) {
            var items = actionItemsPerCategory.getOrDefault(goal.getGoalCategory(), getDefaultActionItems());
            var isPolyGoal = goal.getOwner().getEmail().equals(POLY_EMAIL);

            for (int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
                var itemData = items.get(itemIndex);
                var completed = random.nextDouble() < 0.4;

                // Create recurring tasks for Poly's goals
                TaskType taskType;
                TaskRecurrence recurrence = null;
                FrequencyGoal frequencyGoal = null;

                if (isPolyGoal && itemIndex < 2) {
                    taskType = TaskType.FREQUENCY_BASED;
                    recurrence = TaskRecurrence.builder()
                            .enabled(true)
                            .frequency(itemIndex == 0 ? RecurrenceFrequency.DAILY : RecurrenceFrequency.WEEKLY)
                            .interval(1)
                            .daysOfWeek(itemIndex == 1 ? "1,3,5" : null)
                            .endsAt(LocalDate.now().plusMonths(3))
                            .build();
                    frequencyGoal = FrequencyGoal.builder()
                            .count(itemIndex == 0 ? 7 : 3)
                            .period(FrequencyPeriod.WEEK)
                            .build();
                } else if (isPolyGoal && itemIndex == 2) {
                    taskType = TaskType.DAILY_HABIT;
                } else if (isPolyGoal && itemIndex == 3) {
                    taskType = TaskType.MILESTONE;
                } else {
                    taskType = TaskType.values()[random.nextInt(TaskType.values().length)];
                }

                var actionItem = ActionItem.builder()
                        .goal(goal)
                        .title(itemData[0])
                        .description(itemData[1])
                        .taskType(taskType)
                        .recurrence(recurrence)
                        .frequencyGoal(frequencyGoal)
                        .priority(TaskPriority.values()[random.nextInt(TaskPriority.values().length)])
                        .targetDate(LocalDate.now().plusDays(random.nextInt(60)))
                        .completed(completed)
                        .completedAt(completed ? LocalDateTime.now().minusDays(random.nextInt(14)) : null)
                        .orderIndex(itemIndex)
                        .impactScore(random.nextInt(5) + 6)
                        .effortEstimate(random.nextInt(5) + 3)
                        .notifyOnScheduledTime(isPolyGoal && itemIndex < 2)
                        .notifyMinutesBefore(isPolyGoal && itemIndex < 2 ? 15 : null)
                        .notes(isPolyGoal ? "Important task for tracking progress" : null)
                        .build();
                allActionItems.add(actionItemRepository.save(actionItem));
            }
        }

        return allActionItems;
    }

    private java.util.Map<GoalCategory, List<String[]>> getActionItemsPerCategory() {
        return java.util.Map.of(
                GoalCategory.HEALTH, List.of(
                        new String[]{"Agendar consulta medica", "Fazer check-up geral antes de iniciar"},
                        new String[]{"Comprar equipamentos necessarios", "Adquirir itens basicos para os exercicios"},
                        new String[]{"Montar plano de treino", "Definir dias e horarios fixos para treinar"},
                        new String[]{"Preparar refeicoes saudaveis", "Fazer meal prep no domingo"},
                        new String[]{"Registrar progresso diario", "Anotar metricas e como se sentiu"}
                ),
                GoalCategory.FINANCE, List.of(
                        new String[]{"Fazer planilha de gastos", "Listar todas as despesas mensais"},
                        new String[]{"Cortar gastos desnecessarios", "Identificar e eliminar desperdicios"},
                        new String[]{"Configurar transferencia automatica", "Automatizar poupanca mensal"},
                        new String[]{"Pesquisar investimentos", "Estudar opcoes de rendimento"},
                        new String[]{"Revisar assinaturas", "Cancelar servicos nao utilizados"}
                ),
                GoalCategory.EDUCATION, List.of(
                        new String[]{"Definir horario de estudo", "Reservar tempo fixo diario"},
                        new String[]{"Organizar material de estudo", "Preparar livros, anotacoes e recursos"},
                        new String[]{"Fazer resumos semanais", "Consolidar aprendizado da semana"},
                        new String[]{"Praticar exercicios", "Aplicar conhecimento com exercicios praticos"},
                        new String[]{"Revisar conteudo anterior", "Manter conhecimento fresco na memoria"}
                ),
                GoalCategory.CAREER, List.of(
                        new String[]{"Atualizar curriculo", "Incluir novas experiencias e habilidades"},
                        new String[]{"Fazer networking", "Conectar com profissionais da area"},
                        new String[]{"Solicitar feedback", "Pedir avaliacao do gestor"},
                        new String[]{"Desenvolver nova habilidade", "Fazer curso relevante para a area"},
                        new String[]{"Documentar conquistas", "Registrar resultados e entregas"}
                ),
                GoalCategory.RELATIONSHIPS, List.of(
                        new String[]{"Agendar tempo de qualidade", "Marcar atividades em conjunto"},
                        new String[]{"Praticar escuta ativa", "Prestar atencao total nas conversas"},
                        new String[]{"Expressar gratidao", "Agradecer regularmente"},
                        new String[]{"Planejar surpresas", "Preparar momentos especiais"},
                        new String[]{"Resolver pendencias", "Conversar sobre assuntos adiados"}
                ),
                GoalCategory.PERSONAL_DEVELOPMENT, List.of(
                        new String[]{"Criar rotina matinal", "Estabelecer habitos positivos ao acordar"},
                        new String[]{"Praticar journaling", "Escrever reflexoes diarias"},
                        new String[]{"Meditar diariamente", "Reservar tempo para mindfulness"},
                        new String[]{"Ler sobre o tema", "Estudar materiais de autodesenvolvimento"},
                        new String[]{"Aplicar aprendizados", "Colocar em pratica novos conhecimentos"}
                ),
                GoalCategory.HOBBIES, List.of(
                        new String[]{"Reservar tempo para pratica", "Definir horarios fixos semanais"},
                        new String[]{"Comprar materiais necessarios", "Adquirir itens para o hobby"},
                        new String[]{"Assistir tutoriais", "Aprender tecnicas novas online"},
                        new String[]{"Praticar regularmente", "Manter consistencia na pratica"},
                        new String[]{"Compartilhar progresso", "Mostrar evolucao para amigos/familia"}
                ),
                GoalCategory.OTHER, List.of(
                        new String[]{"Planejar proximos passos", "Definir acoes especificas"},
                        new String[]{"Organizar recursos", "Reunir o necessario para avancar"},
                        new String[]{"Executar tarefas pendentes", "Completar itens da lista"},
                        new String[]{"Avaliar progresso", "Verificar se esta no caminho certo"},
                        new String[]{"Ajustar estrategia", "Fazer correcoes se necessario"}
                )
        );
    }

    private List<String[]> getDefaultActionItems() {
        return List.of(
                new String[]{"Definir proximo passo", "Identificar acao imediata"},
                new String[]{"Revisar progresso", "Avaliar andamento da meta"},
                new String[]{"Ajustar plano", "Fazer correcoes necessarias"}
        );
    }

    private List<GoalGuardian> createGuardians(List<Goal> goals, List<User> users) {
        log.info("Creating guardians...");
        var allGuardians = new ArrayList<GoalGuardian>();

        for (var goal : goals) {
            // Poly's goals always have guardians
            if (!goal.getOwner().getEmail().equals(POLY_EMAIL) && random.nextDouble() < 0.4) {
                continue;
            }

            var potentialGuardians = users.stream()
                    .filter(u -> !u.getId().equals(goal.getOwner().getId()))
                    .toList();

            if (potentialGuardians.isEmpty()) {
                continue;
            }

            var guardian = potentialGuardians.get(random.nextInt(potentialGuardians.size()));
            var status = goal.getOwner().getEmail().equals(POLY_EMAIL) || random.nextDouble() < 0.8
                    ? GuardianStatus.ACTIVE : GuardianStatus.PENDING;

            var messages = List.of(
                    "Voce pode me ajudar a manter o foco nessa meta?",
                    "Preciso de alguem para me cobrar, aceita ser meu guardiao?",
                    "Quero sua ajuda para nao desistir dessa meta!",
                    "Voce e uma pessoa que admiro, aceita acompanhar meu progresso?"
            );

            var goalGuardian = GoalGuardian.builder()
                    .goal(goal)
                    .guardian(guardian)
                    .owner(goal.getOwner())
                    .status(status)
                    .permissions(Set.of(
                            GuardianPermission.VIEW_PROGRESS,
                            GuardianPermission.VIEW_STREAK,
                            GuardianPermission.SEND_NUDGE
                    ))
                    .inviteMessage(messages.get(random.nextInt(messages.size())))
                    .acceptedAt(status == GuardianStatus.ACTIVE ? LocalDateTime.now().minusDays(random.nextInt(30)) : null)
                    .build();

            allGuardians.add(goalGuardianRepository.save(goalGuardian));
        }

        return allGuardians;
    }

    private void createObstacles(List<Goal> goals) {
        log.info("Creating obstacles...");

        var obstacles = List.of(
                new String[]{"Falta de tempo devido ao trabalho", "Reorganizei minha agenda e acordei mais cedo"},
                new String[]{"Perdi a motivacao apos alguns dias", "Revi minhas razoes e lembrei do objetivo final"},
                new String[]{"Problemas pessoais atrapalharam", "Pedi ajuda da familia e consegui voltar ao foco"},
                new String[]{"Dificuldade financeira inesperada", "Ajustei o plano para se adequar a nova realidade"},
                new String[]{"Fiquei doente por alguns dias", "Descansei e retomei com calma"},
                new String[]{"Viagem atrapalhou a rotina", "Adaptei as atividades para fazer durante a viagem"},
                new String[]{"Cansaco excessivo", "Melhorei a qualidade do sono"},
                new String[]{"Falta de apoio de pessoas proximas", "Busquei comunidades online de apoio"}
        );

        for (var goal : goals) {
            // Poly's goals always have obstacles for testing
            if (!goal.getOwner().getEmail().equals(POLY_EMAIL) && random.nextDouble() < 0.4) {
                continue;
            }

            var obstacleCount = goal.getOwner().getEmail().equals(POLY_EMAIL) ? 3 : random.nextInt(3) + 1;
            for (int obstacleIndex = 0; obstacleIndex < obstacleCount; obstacleIndex++) {
                var obstacleData = obstacles.get(random.nextInt(obstacles.size()));
                var resolved = random.nextDouble() < 0.7;

                var obstacle = ObstacleEntry.builder()
                        .goal(goal)
                        .entryDate(LocalDate.now().minusDays(random.nextInt(30)))
                        .obstacle(obstacleData[0])
                        .solution(resolved ? obstacleData[1] : null)
                        .resolved(resolved)
                        .build();
                obstacleEntryRepository.save(obstacle);
            }
        }
    }

    private void createTemplates(List<User> users) {
        log.info("Creating goal templates...");

        var templates = List.of(
                new Object[]{GoalCategory.HEALTH, "Template Emagrecimento", "Template para metas de perda de peso", "Perder peso de forma saudavel", "Emagrecer com dieta e exercicios", new BigDecimal("10"), "kg", "Melhorar saude e autoestima", 90, true},
                new Object[]{GoalCategory.HEALTH, "Template Corrida", "Template para metas de corrida", "Correr distancia X", "Treinar progressivamente para corrida", new BigDecimal("5"), "km", "Melhorar condicionamento fisico", 60, true},
                new Object[]{GoalCategory.FINANCE, "Template Poupanca", "Template para metas de economia", "Economizar valor X", "Guardar dinheiro mensalmente", new BigDecimal("5000"), "reais", "Seguranca financeira", 180, true},
                new Object[]{GoalCategory.EDUCATION, "Template Leitura", "Template para metas de leitura", "Ler X livros", "Manter habito de leitura regular", new BigDecimal("12"), "livros", "Expandir conhecimento", 365, true},
                new Object[]{GoalCategory.CAREER, "Template Promocao", "Template para crescimento na carreira", "Conseguir promocao", "Desenvolver habilidades para crescer", new BigDecimal("100"), "pontos", "Crescimento profissional", 180, false},
                new Object[]{GoalCategory.PERSONAL_DEVELOPMENT, "Template Meditacao", "Template para pratica de meditacao", "Meditar X dias seguidos", "Praticar mindfulness diariamente", new BigDecimal("30"), "dias", "Paz interior e foco", 30, true}
        );

        for (var templateData : templates) {
            var owner = users.get(random.nextInt(users.size()));

            var template = GoalTemplate.builder()
                    .owner(owner)
                    .name((String) templateData[1])
                    .description((String) templateData[2])
                    .defaultTitle((String) templateData[3])
                    .defaultDescription((String) templateData[4])
                    .defaultCategory((GoalCategory) templateData[0])
                    .defaultTargetValue((BigDecimal) templateData[5])
                    .defaultUnit((String) templateData[6])
                    .defaultMotivation((String) templateData[7])
                    .defaultDurationDays((Integer) templateData[8])
                    .isPublic((Boolean) templateData[9])
                    .build();

            goalTemplateRepository.save(template);
        }
    }
}
