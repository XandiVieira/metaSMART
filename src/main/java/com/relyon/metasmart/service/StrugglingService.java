package com.relyon.metasmart.service;

import com.relyon.metasmart.constant.ErrorMessages;
import com.relyon.metasmart.entity.goal.Goal;
import com.relyon.metasmart.entity.guardian.*;
import com.relyon.metasmart.entity.struggling.StrugglingRequest;
import com.relyon.metasmart.entity.struggling.StrugglingType;
import com.relyon.metasmart.entity.struggling.dto.StrugglingHelpRequest;
import com.relyon.metasmart.entity.struggling.dto.StrugglingHelpResponse;
import com.relyon.metasmart.entity.struggling.dto.StrugglingStatusResponse;
import com.relyon.metasmart.entity.subscription.PurchaseType;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.BadRequestException;
import com.relyon.metasmart.exception.ResourceNotFoundException;
import com.relyon.metasmart.repository.GoalGuardianRepository;
import com.relyon.metasmart.repository.GoalRepository;
import com.relyon.metasmart.repository.GuardianNudgeRepository;
import com.relyon.metasmart.repository.StrugglingRequestRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StrugglingService {

    private static final int FREE_REQUESTS_PER_MONTH = 1;

    private final StrugglingRequestRepository strugglingRequestRepository;
    private final GoalRepository goalRepository;
    private final GoalGuardianRepository goalGuardianRepository;
    private final GuardianNudgeRepository guardianNudgeRepository;
    private final SubscriptionService subscriptionService;

    public StrugglingStatusResponse getStatus(User user) {
        log.debug("Getting struggling status for user ID: {}", user.getId());

        var usedThisMonth = countUsedThisMonth(user);
        var remaining = Math.max(0, FREE_REQUESTS_PER_MONTH - (int) usedThisMonth);
        var paidAvailable = getPaidRequestsAvailable(user);

        return StrugglingStatusResponse.builder()
                .freeRequestsUsedThisMonth((int) usedThisMonth)
                .freeRequestsRemaining(remaining)
                .paidRequestsAvailable(paidAvailable)
                .canRequestHelp(remaining > 0 || paidAvailable > 0)
                .build();
    }

    @Transactional
    public StrugglingHelpResponse requestHelp(Long goalId, StrugglingHelpRequest request, User user) {
        log.info("User {} requesting help for goal {}", user.getId(), goalId);

        var goal = goalRepository.findByIdAndOwner(goalId, user)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.GOAL_NOT_FOUND));

        var status = getStatus(user);
        if (Boolean.FALSE.equals(status.getCanRequestHelp())) {
            throw new BadRequestException(ErrorMessages.STRUGGLING_LIMIT_REACHED);
        }

        var suggestions = generateSuggestions(goal, request.getStrugglingType());

        var strugglingRequest = StrugglingRequest.builder()
                .goal(goal)
                .user(user)
                .strugglingType(request.getStrugglingType())
                .userMessage(request.getMessage())
                .aiSuggestion(String.join("\n", suggestions))
                .notifyGuardians(request.getNotifyGuardians())
                .guardiansNotified(false)
                .build();

        strugglingRequest = strugglingRequestRepository.save(strugglingRequest);
        log.info("Created struggling request ID: {} for goal ID: {}", strugglingRequest.getId(), goalId);

        if (Boolean.TRUE.equals(request.getNotifyGuardians())) {
            notifyGuardians(goal, request.getStrugglingType());
            strugglingRequest.setGuardiansNotified(true);
            strugglingRequestRepository.save(strugglingRequest);
        }

        var newStatus = getStatus(user);

        return StrugglingHelpResponse.builder()
                .id(strugglingRequest.getId())
                .goalId(goal.getId())
                .goalTitle(goal.getTitle())
                .strugglingType(request.getStrugglingType())
                .userMessage(request.getMessage())
                .suggestions(suggestions)
                .guardiansNotified(strugglingRequest.getGuardiansNotified())
                .freeRequestsRemaining(newStatus.getFreeRequestsRemaining())
                .createdAt(strugglingRequest.getCreatedAt())
                .build();
    }

    public Page<StrugglingHelpResponse> getHistory(User user, Pageable pageable) {
        log.debug("Getting struggling history for user ID: {}", user.getId());

        return strugglingRequestRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public void markHelpful(Long requestId, Boolean wasHelpful, User user) {
        log.debug("Marking struggling request {} as helpful: {}", requestId, wasHelpful);

        var request = strugglingRequestRepository.findById(requestId)
                .filter(r -> r.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.STRUGGLING_REQUEST_NOT_FOUND));

        request.setWasHelpful(wasHelpful);
        strugglingRequestRepository.save(request);
    }

    private long countUsedThisMonth(User user) {
        var startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        return strugglingRequestRepository.countByUserThisMonth(user, startOfMonth);
    }

    private int getPaidRequestsAvailable(User user) {
        if (subscriptionService.isPremium(user)) {
            return Integer.MAX_VALUE;
        }
        return subscriptionService.getAvailablePurchaseCount(user, PurchaseType.STRUGGLING_ASSIST);
    }

    private List<String> generateSuggestions(Goal goal, StrugglingType type) {
        var suggestions = new ArrayList<String>();

        switch (type) {
            case LACK_OF_TIME -> {
                suggestions.add("Break your goal into smaller 5-minute daily actions");
                suggestions.add("Schedule a specific time slot each day for this goal");
                suggestions.add("Consider extending your target date to reduce pressure");
                if (goal.getTargetDate() != null) {
                    suggestions.add("Current deadline: " + goal.getTargetDate() + " - Would an extra week help?");
                }
            }
            case LACK_OF_MOTIVATION -> {
                suggestions.add("Revisit your 'why' - what made you start this goal?");
                suggestions.add("Celebrate small wins - you've already made progress!");
                suggestions.add("Ask your accountability partner for encouragement");
                suggestions.add("Try the 2-minute rule: commit to just 2 minutes today");
            }
            case GOAL_TOO_AMBITIOUS -> {
                suggestions.add("Consider splitting this into 2-3 smaller goals");
                suggestions.add("Reduce your target value to something more achievable");
                suggestions.add("Focus on consistency over intensity");
                if (goal.getTargetValue() != null) {
                    suggestions.add("Current target: " + goal.getTargetValue() + " " + goal.getUnit() + " - What would be 50% of that?");
                }
            }
            case UNCLEAR_NEXT_STEPS -> {
                suggestions.add("Add specific action items to your goal");
                suggestions.add("Break down your next milestone into 3 concrete steps");
                suggestions.add("Research how others achieved similar goals");
                suggestions.add("Ask your guardian for advice on next steps");
            }
            case EXTERNAL_OBSTACLES -> {
                suggestions.add("Log this obstacle in your obstacles diary");
                suggestions.add("Identify what's within your control");
                suggestions.add("Create a contingency plan for this situation");
                suggestions.add("Consider pausing the goal temporarily if needed");
            }
            case LOST_INTEREST -> {
                suggestions.add("Reflect: Is this goal still aligned with your values?");
                suggestions.add("It's okay to abandon goals that no longer serve you");
                suggestions.add("Consider pivoting to a related goal you're excited about");
                suggestions.add("Talk to your guardian about how you're feeling");
            }
            case OTHER -> {
                suggestions.add("Take a moment to write down what's really blocking you");
                suggestions.add("Reach out to your accountability partner");
                suggestions.add("Remember: setbacks are part of the journey");
                suggestions.add("Consider adjusting your goal to match your current situation");
            }
        }

        return suggestions;
    }

    private void notifyGuardians(Goal goal, StrugglingType type) {
        var activeGuardians = goalGuardianRepository.findByGoalAndStatus(goal, GuardianStatus.ACTIVE);

        var message = String.format("%s is struggling with their goal '%s' and could use some support. They're experiencing: %s",
                goal.getOwner().getName(), goal.getTitle(), type.name().replace("_", " ").toLowerCase());

        for (GoalGuardian guardian : activeGuardians) {
            if (guardian.hasPermission(GuardianPermission.SEND_NUDGE)) {
                var nudge = GuardianNudge.builder()
                        .goalGuardian(guardian)
                        .message(message)
                        .nudgeType(NudgeType.CHECK_IN)
                        .build();
                guardianNudgeRepository.save(nudge);
                log.info("Notified guardian {} about struggling goal {}", guardian.getGuardian().getId(), goal.getId());
            }
        }
    }

    private StrugglingHelpResponse toResponse(StrugglingRequest request) {
        return StrugglingHelpResponse.builder()
                .id(request.getId())
                .goalId(request.getGoal().getId())
                .goalTitle(request.getGoal().getTitle())
                .strugglingType(request.getStrugglingType())
                .userMessage(request.getUserMessage())
                .suggestions(request.getAiSuggestion() != null ?
                        List.of(request.getAiSuggestion().split("\n")) : List.of())
                .guardiansNotified(request.getGuardiansNotified())
                .createdAt(request.getCreatedAt())
                .build();
    }
}
