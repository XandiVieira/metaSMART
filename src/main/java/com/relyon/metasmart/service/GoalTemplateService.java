package com.relyon.metasmart.service;

import com.relyon.metasmart.constant.ErrorMessages;
import com.relyon.metasmart.entity.goal.GoalCategory;
import com.relyon.metasmart.entity.goal.dto.GoalRequest;
import com.relyon.metasmart.entity.template.dto.GoalTemplateRequest;
import com.relyon.metasmart.entity.template.dto.GoalTemplateResponse;
import com.relyon.metasmart.entity.template.dto.UpdateGoalTemplateRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.ResourceNotFoundException;
import com.relyon.metasmart.mapper.GoalTemplateMapper;
import com.relyon.metasmart.repository.GoalTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoalTemplateService {

    private final GoalTemplateRepository goalTemplateRepository;
    private final GoalTemplateMapper goalTemplateMapper;

    @Transactional
    public GoalTemplateResponse create(GoalTemplateRequest request, User user) {
        log.debug("Creating goal template for user ID: {}", user.getId());

        var template = goalTemplateMapper.toEntity(request);
        template.setOwner(user);

        if (template.getDefaultDurationDays() == null) {
            template.setDefaultDurationDays(90);
        }
        if (template.getIsPublic() == null) {
            template.setIsPublic(false);
        }

        var savedTemplate = goalTemplateRepository.save(template);
        log.info("Goal template created with ID: {} for user ID: {}", savedTemplate.getId(), user.getId());

        return goalTemplateMapper.toResponse(savedTemplate);
    }

    @Transactional(readOnly = true)
    public Page<GoalTemplateResponse> findByOwner(User user, Pageable pageable) {
        log.debug("Fetching goal templates for user ID: {}", user.getId());
        return goalTemplateRepository.findByOwnerOrderByCreatedAtDesc(user, pageable)
                .map(goalTemplateMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<GoalTemplateResponse> findAvailable(User user, GoalCategory category, Pageable pageable) {
        log.debug("Fetching available goal templates for user ID: {} with category: {}", user.getId(), category);
        if (category != null) {
            return goalTemplateRepository.findAvailableTemplatesByCategory(user, category, pageable)
                    .map(goalTemplateMapper::toResponse);
        }
        return goalTemplateRepository.findAvailableTemplates(user, pageable)
                .map(goalTemplateMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<GoalTemplateResponse> findPublic(GoalCategory category, Pageable pageable) {
        log.debug("Fetching public goal templates with category: {}", category);
        if (category != null) {
            return goalTemplateRepository.findByIsPublicTrueAndDefaultCategoryOrderByCreatedAtDesc(category, pageable)
                    .map(goalTemplateMapper::toResponse);
        }
        return goalTemplateRepository.findByIsPublicTrueOrderByCreatedAtDesc(pageable)
                .map(goalTemplateMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public GoalTemplateResponse findById(Long id, User user) {
        log.debug("Finding goal template ID: {} for user ID: {}", id, user.getId());
        return goalTemplateRepository.findByIdAndOwner(id, user)
                .map(goalTemplateMapper::toResponse)
                .orElseThrow(() -> {
                    log.warn("Goal template not found with ID: {} for user ID: {}", id, user.getId());
                    return new ResourceNotFoundException(ErrorMessages.GOAL_TEMPLATE_NOT_FOUND);
                });
    }

    @Transactional
    public GoalTemplateResponse update(Long id, UpdateGoalTemplateRequest request, User user) {
        log.debug("Updating goal template ID: {} for user ID: {}", id, user.getId());

        var template = goalTemplateRepository.findByIdAndOwner(id, user)
                .orElseThrow(() -> {
                    log.warn("Goal template not found with ID: {} for user ID: {}", id, user.getId());
                    return new ResourceNotFoundException(ErrorMessages.GOAL_TEMPLATE_NOT_FOUND);
                });

        Optional.ofNullable(request.getName()).ifPresent(template::setName);
        Optional.ofNullable(request.getDescription()).ifPresent(template::setDescription);
        Optional.ofNullable(request.getDefaultTitle()).ifPresent(template::setDefaultTitle);
        Optional.ofNullable(request.getDefaultDescription()).ifPresent(template::setDefaultDescription);
        Optional.ofNullable(request.getDefaultCategory()).ifPresent(template::setDefaultCategory);
        Optional.ofNullable(request.getDefaultTargetValue()).ifPresent(template::setDefaultTargetValue);
        Optional.ofNullable(request.getDefaultUnit()).ifPresent(template::setDefaultUnit);
        Optional.ofNullable(request.getDefaultMotivation()).ifPresent(template::setDefaultMotivation);
        Optional.ofNullable(request.getDefaultDurationDays()).ifPresent(template::setDefaultDurationDays);
        Optional.ofNullable(request.getIsPublic()).ifPresent(template::setIsPublic);

        var savedTemplate = goalTemplateRepository.save(template);
        log.info("Goal template updated with ID: {}", savedTemplate.getId());

        return goalTemplateMapper.toResponse(savedTemplate);
    }

    @Transactional
    public void delete(Long id, User user) {
        log.debug("Deleting goal template ID: {} for user ID: {}", id, user.getId());

        var template = goalTemplateRepository.findByIdAndOwner(id, user)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.GOAL_TEMPLATE_NOT_FOUND));

        goalTemplateRepository.delete(template);
        log.info("Goal template deleted with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public GoalRequest createGoalFromTemplate(Long templateId, User user) {
        log.debug("Creating goal request from template ID: {} for user ID: {}", templateId, user.getId());

        var template = goalTemplateRepository.findById(templateId)
                .filter(t -> t.getIsPublic() || t.getOwner().getId().equals(user.getId()))
                .orElseThrow(() -> {
                    log.warn("Goal template not found or not accessible with ID: {} for user ID: {}", templateId, user.getId());
                    return new ResourceNotFoundException(ErrorMessages.GOAL_TEMPLATE_NOT_FOUND);
                });

        var startDate = LocalDate.now();
        var targetDate = startDate.plusDays(template.getDefaultDurationDays());

        return GoalRequest.builder()
                .title(template.getDefaultTitle())
                .description(template.getDefaultDescription())
                .goalCategory(template.getDefaultCategory())
                .targetValue(template.getDefaultTargetValue())
                .unit(template.getDefaultUnit())
                .motivation(template.getDefaultMotivation())
                .startDate(startDate)
                .targetDate(targetDate)
                .build();
    }
}
