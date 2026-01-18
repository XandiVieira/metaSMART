package com.relyon.metasmart.config;

import com.relyon.metasmart.entity.actionplan.ActionItem;
import com.relyon.metasmart.entity.actionplan.TaskPriority;
import com.relyon.metasmart.entity.actionplan.TaskType;
import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.goal.GoalCategory;
import com.relyon.metasmart.entity.goal.GoalStatus;
import com.relyon.metasmart.entity.guardian.GoalGuardian;
import com.relyon.metasmart.entity.guardian.GuardianPermission;
import com.relyon.metasmart.entity.guardian.GuardianStatus;
import com.relyon.metasmart.entity.obstacle.ObstacleEntry;
import com.relyon.metasmart.entity.progress.Milestone;
import com.relyon.metasmart.entity.progress.ProgressEntry;
import com.relyon.metasmart.entity.template.GoalTemplate;
import com.relyon.metasmart.entity.user.User;
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

    private final UserRepository userRepository;
    private final GoalRepository goalRepository;
    private final ProgressEntryRepository progressEntryRepository;
    private final MilestoneRepository milestoneRepository;
    private final ActionItemRepository actionItemRepository;
    private final GoalGuardianRepository goalGuardianRepository;
    private final ObstacleEntryRepository obstacleEntryRepository;
    private final GoalTemplateRepository goalTemplateRepository;
    private final PasswordEncoder passwordEncoder;

    private final Random random = new Random();

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Banco de dados já populado, pulando seed...");
            return;
        }

        log.info("Iniciando população do banco de dados...");

        var users = createUsers();
        var goals = createGoals(users);
        createProgressEntries(goals);
        createMilestones(goals);
        createActionItems(goals);
        createGuardians(goals, users);
        createObstacles(goals);
        createTemplates(users);

        log.info("População do banco concluída!");
        log.info("Criados {} usuários, {} metas", users.size(), goals.size());
    }

    private List<User> createUsers() {
        log.info("Criando usuários...");
        var users = new ArrayList<User>();
        var encodedPassword = passwordEncoder.encode(DEFAULT_PASSWORD);

        var userData = List.of(
                new String[]{"Maria Silva", "maria@test.com"},
                new String[]{"João Santos", "joao@test.com"},
                new String[]{"Ana Oliveira", "ana@test.com"}
        );

        for (int userIndex = 0; userIndex < userData.size(); userIndex++) {
            var data = userData.get(userIndex);
            var user = User.builder()
                    .name(data[0])
                    .email(data[1])
                    .password(encodedPassword)
                    .role(userIndex == 0 ? User.Role.ADMIN : User.Role.USER)
                    .streakShields(random.nextInt(5))
                    .build();
            users.add(userRepository.save(user));
        }

        log.info("Criados {} usuários (senha padrão: {})", users.size(), DEFAULT_PASSWORD);
        return users;
    }

    private List<Goal> createGoals(List<User> users) {
        log.info("Criando metas...");
        var goals = new ArrayList<Goal>();

        var maria = users.get(0);
        goals.addAll(createMariaGoals(maria));

        var joao = users.get(1);
        goals.addAll(createJoaoGoals(joao));

        var ana = users.get(2);
        goals.addAll(createAnaGoals(ana));

        return goals;
    }

    private List<Goal> createMariaGoals(User maria) {
        var goals = new ArrayList<Goal>();

        goals.add(goalRepository.save(Goal.builder()
                .owner(maria)
                .title("Correr 5km sem parar")
                .description("Treinar progressivamente até conseguir correr 5km completos sem precisar andar")
                .goalCategory(GoalCategory.HEALTH)
                .targetValue(new BigDecimal("5"))
                .unit("km")
                .currentProgress(new BigDecimal("2.5"))
                .motivation("Quero melhorar minha saúde cardiovascular e ter mais disposição")
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
                .description("Ler 2 livros por mês para expandir conhecimentos e relaxar")
                .goalCategory(GoalCategory.EDUCATION)
                .targetValue(new BigDecimal("24"))
                .unit("livros")
                .currentProgress(new BigDecimal("14"))
                .motivation("Adoro ler e quero manter o hábito constante")
                .startDate(LocalDate.now().minusDays(200))
                .targetDate(LocalDate.now().plusDays(165))
                .goalStatus(GoalStatus.ACTIVE)
                .streak(21)
                .tags("leitura,educacao,habito")
                .build()));

        goals.add(goalRepository.save(Goal.builder()
                .owner(maria)
                .title("Aprender inglês nível B2")
                .description("Estudar inglês diariamente até atingir fluência intermediária")
                .goalCategory(GoalCategory.EDUCATION)
                .targetValue(new BigDecimal("100"))
                .unit("lições")
                .currentProgress(new BigDecimal("72"))
                .motivation("Preciso de inglês para crescer na carreira")
                .startDate(LocalDate.now().minusDays(90))
                .targetDate(LocalDate.now().plusDays(90))
                .goalStatus(GoalStatus.ACTIVE)
                .streak(15)
                .tags("ingles,idiomas,carreira")
                .build()));

        goals.add(goalRepository.save(Goal.builder()
                .owner(maria)
                .title("Meditar 30 dias seguidos")
                .description("Praticar meditação diária por 10 minutos")
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
                .description("Emagrecer de forma saudável com dieta equilibrada e exercícios")
                .goalCategory(GoalCategory.HEALTH)
                .targetValue(new BigDecimal("15"))
                .unit("kg")
                .currentProgress(new BigDecimal("7.5"))
                .motivation("Quero melhorar minha autoestima e saúde geral")
                .startDate(LocalDate.now().minusDays(90))
                .targetDate(LocalDate.now().plusDays(90))
                .goalStatus(GoalStatus.ACTIVE)
                .streak(5)
                .tags("emagrecimento,saude,dieta")
                .build()));

        goals.add(goalRepository.save(Goal.builder()
                .owner(joao)
                .title("Conseguir promoção no trabalho")
                .description("Desenvolver habilidades e entregar resultados para ser promovido")
                .goalCategory(GoalCategory.CAREER)
                .targetValue(new BigDecimal("100"))
                .unit("pontos")
                .currentProgress(new BigDecimal("65"))
                .motivation("Crescer profissionalmente e ter melhor salário")
                .startDate(LocalDate.now().minusDays(60))
                .targetDate(LocalDate.now().plusDays(120))
                .goalStatus(GoalStatus.ACTIVE)
                .streak(3)
                .tags("carreira,promocao,trabalho")
                .build()));

        goals.add(goalRepository.save(Goal.builder()
                .owner(joao)
                .title("Quitar todas as dívidas")
                .description("Pagar cartão de crédito e empréstimos pendentes")
                .goalCategory(GoalCategory.FINANCE)
                .targetValue(new BigDecimal("8000"))
                .unit("reais")
                .currentProgress(new BigDecimal("5200"))
                .motivation("Ficar livre de dívidas e ter paz financeira")
                .startDate(LocalDate.now().minusDays(150))
                .targetDate(LocalDate.now().plusDays(30))
                .goalStatus(GoalStatus.ACTIVE)
                .streak(10)
                .tags("dividas,financas,organizacao")
                .build()));

        goals.add(goalRepository.save(Goal.builder()
                .owner(joao)
                .title("Aprender a tocar violão")
                .description("Praticar violão 30 minutos por dia até tocar músicas completas")
                .goalCategory(GoalCategory.HOBBIES)
                .targetValue(new BigDecimal("50"))
                .unit("músicas")
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
                .description("Manter frequência regular de treinos na academia")
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
                .title("Terminar curso de programação")
                .description("Completar bootcamp de desenvolvimento web full-stack")
                .goalCategory(GoalCategory.EDUCATION)
                .targetValue(new BigDecimal("120"))
                .unit("horas")
                .currentProgress(new BigDecimal("85"))
                .motivation("Mudar de carreira para área de tecnologia")
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
                .description("Aplicar método de organização em todos os cômodos")
                .goalCategory(GoalCategory.OTHER)
                .targetValue(new BigDecimal("8"))
                .unit("cômodos")
                .currentProgress(new BigDecimal("5"))
                .motivation("Ter um ambiente mais agradável e funcional")
                .startDate(LocalDate.now().minusDays(30))
                .targetDate(LocalDate.now().plusDays(30))
                .goalStatus(GoalStatus.ACTIVE)
                .streak(4)
                .tags("organizacao,casa,limpeza")
                .build()));

        goals.add(goalRepository.save(Goal.builder()
                .owner(ana)
                .title("Passar mais tempo com a família")
                .description("Dedicar pelo menos 2 horas por dia para atividades em família")
                .goalCategory(GoalCategory.RELATIONSHIPS)
                .targetValue(new BigDecimal("60"))
                .unit("horas")
                .currentProgress(new BigDecimal("42"))
                .motivation("Fortalecer os laços familiares")
                .startDate(LocalDate.now().minusDays(30))
                .targetDate(LocalDate.now().plusDays(30))
                .goalStatus(GoalStatus.ACTIVE)
                .streak(18)
                .tags("familia,relacionamentos,qualidade-tempo")
                .build()));

        goals.add(goalRepository.save(Goal.builder()
                .owner(ana)
                .title("Começar um negócio próprio")
                .description("Planejar e lançar loja online de artesanato")
                .goalCategory(GoalCategory.CAREER)
                .targetValue(new BigDecimal("100"))
                .unit("tarefas")
                .currentProgress(new BigDecimal("35"))
                .motivation("Ter independência financeira e fazer o que amo")
                .startDate(LocalDate.now().minusDays(45))
                .targetDate(LocalDate.now().plusDays(135))
                .goalStatus(GoalStatus.ACTIVE)
                .streak(6)
                .tags("empreendedorismo,negocio,artesanato")
                .build()));

        goals.add(goalRepository.save(Goal.builder()
                .owner(ana)
                .title("Beber 2 litros de água por dia")
                .description("Manter hidratação adequada diariamente")
                .goalCategory(GoalCategory.HEALTH)
                .targetValue(new BigDecimal("90"))
                .unit("dias")
                .currentProgress(new BigDecimal("67"))
                .motivation("Melhorar a saúde da pele e disposição geral")
                .startDate(LocalDate.now().minusDays(67))
                .targetDate(LocalDate.now().plusDays(23))
                .goalStatus(GoalStatus.ACTIVE)
                .streak(25)
                .tags("agua,hidratacao,saude")
                .build()));

        goals.add(goalRepository.save(Goal.builder()
                .owner(ana)
                .title("Fazer curso de confeitaria")
                .description("Aprender técnicas profissionais de confeitaria")
                .goalCategory(GoalCategory.EDUCATION)
                .targetValue(new BigDecimal("40"))
                .unit("aulas")
                .currentProgress(new BigDecimal("40"))
                .motivation("Complementar meu negócio de artesanato com doces")
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
                .motivation("Ter uma cozinha funcional para o negócio de confeitaria")
                .startDate(LocalDate.now().minusDays(120))
                .targetDate(LocalDate.now().plusDays(60))
                .goalStatus(GoalStatus.ACTIVE)
                .streak(9)
                .tags("economia,reforma,investimento")
                .build()));

        return goals;
    }

    private void createProgressEntries(List<Goal> goals) {
        log.info("Criando registros de progresso...");

        var notes = List.of(
                "Progresso do dia, mantendo o foco!",
                "Dia produtivo, consegui avançar bastante",
                "Enfrentei algumas dificuldades mas não desisti",
                "Excelente progresso hoje",
                "Pequeno avanço, mas consistência é a chave",
                "Muito motivado(a) hoje!",
                "Dia desafiador, mas consegui contribuir",
                "Mantendo a regularidade",
                "Celebrando pequenas vitórias",
                "Focado(a) no objetivo final"
        );

        for (var goal : goals) {
            var entryCount = 8 + random.nextInt(8);
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
        log.info("Criando marcos...");
        var milestoneData = List.of(
                new Object[]{25, "Primeiro quarto concluído!"},
                new Object[]{50, "Metade do caminho!"},
                new Object[]{75, "Três quartos completos!"},
                new Object[]{100, "Meta alcançada!"}
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

    private void createActionItems(List<Goal> goals) {
        log.info("Criando itens de ação...");

        var actionItemsPerCategory = getActionItemsPerCategory();

        for (var goal : goals) {
            var items = actionItemsPerCategory.getOrDefault(goal.getGoalCategory(), getDefaultActionItems());

            for (int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
                var itemData = items.get(itemIndex);
                var completed = random.nextDouble() < 0.4;

                var actionItem = ActionItem.builder()
                        .goal(goal)
                        .title(itemData[0])
                        .description(itemData[1])
                        .taskType(TaskType.values()[random.nextInt(TaskType.values().length)])
                        .priority(TaskPriority.values()[random.nextInt(TaskPriority.values().length)])
                        .targetDate(LocalDate.now().plusDays(random.nextInt(60)))
                        .completed(completed)
                        .completedAt(completed ? LocalDateTime.now().minusDays(random.nextInt(14)) : null)
                        .orderIndex(itemIndex)
                        .impactScore(random.nextInt(5) + 6)
                        .effortEstimate(random.nextInt(5) + 3)
                        .build();
                actionItemRepository.save(actionItem);
            }
        }
    }

    private java.util.Map<GoalCategory, List<String[]>> getActionItemsPerCategory() {
        return java.util.Map.of(
                GoalCategory.HEALTH, List.of(
                        new String[]{"Agendar consulta médica", "Fazer check-up geral antes de iniciar"},
                        new String[]{"Comprar equipamentos necessários", "Adquirir itens básicos para os exercícios"},
                        new String[]{"Montar plano de treino", "Definir dias e horários fixos para treinar"},
                        new String[]{"Preparar refeições saudáveis", "Fazer meal prep no domingo"},
                        new String[]{"Registrar progresso diário", "Anotar métricas e como se sentiu"}
                ),
                GoalCategory.FINANCE, List.of(
                        new String[]{"Fazer planilha de gastos", "Listar todas as despesas mensais"},
                        new String[]{"Cortar gastos desnecessários", "Identificar e eliminar desperdícios"},
                        new String[]{"Configurar transferência automática", "Automatizar poupança mensal"},
                        new String[]{"Pesquisar investimentos", "Estudar opções de rendimento"},
                        new String[]{"Revisar assinaturas", "Cancelar serviços não utilizados"}
                ),
                GoalCategory.EDUCATION, List.of(
                        new String[]{"Definir horário de estudo", "Reservar tempo fixo diário"},
                        new String[]{"Organizar material de estudo", "Preparar livros, anotações e recursos"},
                        new String[]{"Fazer resumos semanais", "Consolidar aprendizado da semana"},
                        new String[]{"Praticar exercícios", "Aplicar conhecimento com exercícios práticos"},
                        new String[]{"Revisar conteúdo anterior", "Manter conhecimento fresco na memória"}
                ),
                GoalCategory.CAREER, List.of(
                        new String[]{"Atualizar currículo", "Incluir novas experiências e habilidades"},
                        new String[]{"Fazer networking", "Conectar com profissionais da área"},
                        new String[]{"Solicitar feedback", "Pedir avaliação do gestor"},
                        new String[]{"Desenvolver nova habilidade", "Fazer curso relevante para a área"},
                        new String[]{"Documentar conquistas", "Registrar resultados e entregas"}
                ),
                GoalCategory.RELATIONSHIPS, List.of(
                        new String[]{"Agendar tempo de qualidade", "Marcar atividades em conjunto"},
                        new String[]{"Praticar escuta ativa", "Prestar atenção total nas conversas"},
                        new String[]{"Expressar gratidão", "Agradecer regularmente"},
                        new String[]{"Planejar surpresas", "Preparar momentos especiais"},
                        new String[]{"Resolver pendências", "Conversar sobre assuntos adiados"}
                ),
                GoalCategory.PERSONAL_DEVELOPMENT, List.of(
                        new String[]{"Criar rotina matinal", "Estabelecer hábitos positivos ao acordar"},
                        new String[]{"Praticar journaling", "Escrever reflexões diárias"},
                        new String[]{"Meditar diariamente", "Reservar tempo para mindfulness"},
                        new String[]{"Ler sobre o tema", "Estudar materiais de autodesenvolvimento"},
                        new String[]{"Aplicar aprendizados", "Colocar em prática novos conhecimentos"}
                ),
                GoalCategory.HOBBIES, List.of(
                        new String[]{"Reservar tempo para prática", "Definir horários fixos semanais"},
                        new String[]{"Comprar materiais necessários", "Adquirir itens para o hobby"},
                        new String[]{"Assistir tutoriais", "Aprender técnicas novas online"},
                        new String[]{"Praticar regularmente", "Manter consistência na prática"},
                        new String[]{"Compartilhar progresso", "Mostrar evolução para amigos/família"}
                ),
                GoalCategory.OTHER, List.of(
                        new String[]{"Planejar próximos passos", "Definir ações específicas"},
                        new String[]{"Organizar recursos", "Reunir o necessário para avançar"},
                        new String[]{"Executar tarefas pendentes", "Completar itens da lista"},
                        new String[]{"Avaliar progresso", "Verificar se está no caminho certo"},
                        new String[]{"Ajustar estratégia", "Fazer correções se necessário"}
                )
        );
    }

    private List<String[]> getDefaultActionItems() {
        return List.of(
                new String[]{"Definir próximo passo", "Identificar ação imediata"},
                new String[]{"Revisar progresso", "Avaliar andamento da meta"},
                new String[]{"Ajustar plano", "Fazer correções necessárias"}
        );
    }

    private void createGuardians(List<Goal> goals, List<User> users) {
        log.info("Criando guardiões...");

        for (var goal : goals) {
            if (random.nextDouble() < 0.4) {
                continue;
            }

            var potentialGuardians = users.stream()
                    .filter(u -> !u.getId().equals(goal.getOwner().getId()))
                    .toList();

            if (potentialGuardians.isEmpty()) {
                continue;
            }

            var guardian = potentialGuardians.get(random.nextInt(potentialGuardians.size()));
            var status = random.nextDouble() < 0.8 ? GuardianStatus.ACTIVE : GuardianStatus.PENDING;

            var messages = List.of(
                    "Você pode me ajudar a manter o foco nessa meta?",
                    "Preciso de alguém para me cobrar, aceita ser meu guardião?",
                    "Quero sua ajuda para não desistir dessa meta!",
                    "Você é uma pessoa que admiro, aceita acompanhar meu progresso?"
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

            goalGuardianRepository.save(goalGuardian);
        }
    }

    private void createObstacles(List<Goal> goals) {
        log.info("Criando obstáculos...");

        var obstacles = List.of(
                new String[]{"Falta de tempo devido ao trabalho", "Reorganizei minha agenda e acordei mais cedo"},
                new String[]{"Perdi a motivação após alguns dias", "Revi minhas razões e lembrei do objetivo final"},
                new String[]{"Problemas pessoais atrapalharam", "Pedi ajuda da família e consegui voltar ao foco"},
                new String[]{"Dificuldade financeira inesperada", "Ajustei o plano para se adequar à nova realidade"},
                new String[]{"Fiquei doente por alguns dias", "Descansei e retomei com calma"},
                new String[]{"Viagem atrapalhou a rotina", "Adaptei as atividades para fazer durante a viagem"},
                new String[]{"Cansaço excessivo", "Melhorei a qualidade do sono"},
                new String[]{"Falta de apoio de pessoas próximas", "Busquei comunidades online de apoio"}
        );

        for (var goal : goals) {
            if (random.nextDouble() < 0.4) {
                continue;
            }

            var obstacleCount = random.nextInt(3) + 1;
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
        log.info("Criando templates de metas...");

        var templates = List.of(
                new Object[]{GoalCategory.HEALTH, "Template Emagrecimento", "Template para metas de perda de peso", "Perder peso de forma saudável", "Emagrecer com dieta e exercícios", new BigDecimal("10"), "kg", "Melhorar saúde e autoestima", 90, true},
                new Object[]{GoalCategory.HEALTH, "Template Corrida", "Template para metas de corrida", "Correr distância X", "Treinar progressivamente para corrida", new BigDecimal("5"), "km", "Melhorar condicionamento físico", 60, true},
                new Object[]{GoalCategory.FINANCE, "Template Poupança", "Template para metas de economia", "Economizar valor X", "Guardar dinheiro mensalmente", new BigDecimal("5000"), "reais", "Segurança financeira", 180, true},
                new Object[]{GoalCategory.EDUCATION, "Template Leitura", "Template para metas de leitura", "Ler X livros", "Manter hábito de leitura regular", new BigDecimal("12"), "livros", "Expandir conhecimento", 365, true},
                new Object[]{GoalCategory.CAREER, "Template Promoção", "Template para crescimento na carreira", "Conseguir promoção", "Desenvolver habilidades para crescer", new BigDecimal("100"), "pontos", "Crescimento profissional", 180, false},
                new Object[]{GoalCategory.PERSONAL_DEVELOPMENT, "Template Meditação", "Template para prática de meditação", "Meditar X dias seguidos", "Praticar mindfulness diariamente", new BigDecimal("30"), "dias", "Paz interior e foco", 30, true}
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
