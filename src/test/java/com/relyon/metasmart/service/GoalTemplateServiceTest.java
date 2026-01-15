package com.relyon.metasmart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.relyon.metasmart.constant.ErrorMessages;
import com.relyon.metasmart.entity.goal.GoalCategory;
import com.relyon.metasmart.entity.template.GoalTemplate;
import com.relyon.metasmart.entity.template.dto.GoalTemplateRequest;
import com.relyon.metasmart.entity.template.dto.GoalTemplateResponse;
import com.relyon.metasmart.entity.template.dto.UpdateGoalTemplateRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.ResourceNotFoundException;
import com.relyon.metasmart.mapper.GoalTemplateMapper;
import com.relyon.metasmart.repository.GoalTemplateRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class GoalTemplateServiceTest {

    @Mock
    private GoalTemplateRepository goalTemplateRepository;

    @Mock
    private GoalTemplateMapper goalTemplateMapper;

    @InjectMocks
    private GoalTemplateService goalTemplateService;

    private User user;
    private GoalTemplate template;
    private GoalTemplateRequest request;
    private GoalTemplateResponse response;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("John").email("john@test.com").build();

        template = GoalTemplate.builder()
                .id(1L)
                .owner(user)
                .name("Running Template")
                .description("Template for running goals")
                .defaultTitle("Run [distance]")
                .defaultCategory(GoalCategory.HEALTH)
                .defaultTargetValue("5")
                .defaultUnit("km")
                .defaultDurationDays(90)
                .isPublic(false)
                .build();

        request = GoalTemplateRequest.builder()
                .name("Running Template")
                .description("Template for running goals")
                .defaultTitle("Run [distance]")
                .defaultCategory(GoalCategory.HEALTH)
                .defaultTargetValue("5")
                .defaultUnit("km")
                .defaultDurationDays(90)
                .isPublic(false)
                .build();

        response = GoalTemplateResponse.builder()
                .id(1L)
                .name("Running Template")
                .description("Template for running goals")
                .defaultTitle("Run [distance]")
                .defaultCategory(GoalCategory.HEALTH)
                .defaultTargetValue("5")
                .defaultUnit("km")
                .defaultDurationDays(90)
                .isPublic(false)
                .build();
    }

    @Nested
    @DisplayName("Create template tests")
    class CreateTests {

        @Test
        @DisplayName("Should create template successfully")
        void shouldCreateTemplateSuccessfully() {
            when(goalTemplateMapper.toEntity(request)).thenReturn(template);
            when(goalTemplateRepository.save(any(GoalTemplate.class))).thenReturn(template);
            when(goalTemplateMapper.toResponse(template)).thenReturn(response);

            var result = goalTemplateService.create(request, user);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Running Template");
            verify(goalTemplateRepository).save(any(GoalTemplate.class));
        }

        @Test
        @DisplayName("Should set default values when not provided")
        void shouldSetDefaultValuesWhenNotProvided() {
            var templateWithoutDefaults = GoalTemplate.builder()
                    .id(1L)
                    .owner(user)
                    .name("Template")
                    .build();

            when(goalTemplateMapper.toEntity(request)).thenReturn(templateWithoutDefaults);
            when(goalTemplateRepository.save(any(GoalTemplate.class))).thenReturn(templateWithoutDefaults);
            when(goalTemplateMapper.toResponse(any())).thenReturn(response);

            goalTemplateService.create(request, user);

            verify(goalTemplateRepository).save(argThat(t ->
                    t.getDefaultDurationDays() == 90 && !t.getIsPublic()
            ));
        }
    }

    @Nested
    @DisplayName("Find template tests")
    class FindTests {

        @Test
        @DisplayName("Should find templates by owner")
        void shouldFindTemplatesByOwner() {
            var pageable = Pageable.unpaged();
            var templates = new PageImpl<>(List.of(template));

            when(goalTemplateRepository.findByOwnerOrderByCreatedAtDesc(user, pageable)).thenReturn(templates);
            when(goalTemplateMapper.toResponse(template)).thenReturn(response);

            var result = goalTemplateService.findByOwner(user, pageable);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should find available templates without category")
        void shouldFindAvailableTemplates() {
            var pageable = Pageable.unpaged();
            var templates = new PageImpl<>(List.of(template));

            when(goalTemplateRepository.findAvailableTemplates(user, pageable)).thenReturn(templates);
            when(goalTemplateMapper.toResponse(template)).thenReturn(response);

            var result = goalTemplateService.findAvailable(user, null, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(goalTemplateRepository).findAvailableTemplates(user, pageable);
        }

        @Test
        @DisplayName("Should find available templates filtered by category")
        void shouldFindAvailableTemplatesFilteredByCategory() {
            var pageable = Pageable.unpaged();
            var templates = new PageImpl<>(List.of(template));

            when(goalTemplateRepository.findAvailableTemplatesByCategory(user, GoalCategory.HEALTH, pageable)).thenReturn(templates);
            when(goalTemplateMapper.toResponse(template)).thenReturn(response);

            var result = goalTemplateService.findAvailable(user, GoalCategory.HEALTH, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(goalTemplateRepository).findAvailableTemplatesByCategory(user, GoalCategory.HEALTH, pageable);
        }

        @Test
        @DisplayName("Should find public templates without category")
        void shouldFindPublicTemplates() {
            var pageable = Pageable.unpaged();
            var templates = new PageImpl<>(List.of(template));

            when(goalTemplateRepository.findByIsPublicTrueOrderByCreatedAtDesc(pageable)).thenReturn(templates);
            when(goalTemplateMapper.toResponse(template)).thenReturn(response);

            var result = goalTemplateService.findPublic(null, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(goalTemplateRepository).findByIsPublicTrueOrderByCreatedAtDesc(pageable);
        }

        @Test
        @DisplayName("Should find public templates filtered by category")
        void shouldFindPublicTemplatesFilteredByCategory() {
            var pageable = Pageable.unpaged();
            var templates = new PageImpl<>(List.of(template));

            when(goalTemplateRepository.findByIsPublicTrueAndDefaultCategoryOrderByCreatedAtDesc(GoalCategory.HEALTH, pageable)).thenReturn(templates);
            when(goalTemplateMapper.toResponse(template)).thenReturn(response);

            var result = goalTemplateService.findPublic(GoalCategory.HEALTH, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(goalTemplateRepository).findByIsPublicTrueAndDefaultCategoryOrderByCreatedAtDesc(GoalCategory.HEALTH, pageable);
        }

        @Test
        @DisplayName("Should find template by id")
        void shouldFindTemplateById() {
            when(goalTemplateRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(template));
            when(goalTemplateMapper.toResponse(template)).thenReturn(response);

            var result = goalTemplateService.findById(1L, user);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Running Template");
        }

        @Test
        @DisplayName("Should throw exception when template not found")
        void shouldThrowExceptionWhenTemplateNotFound() {
            when(goalTemplateRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> goalTemplateService.findById(1L, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.GOAL_TEMPLATE_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Update template tests")
    class UpdateTests {

        @Test
        @DisplayName("Should update template successfully")
        void shouldUpdateTemplateSuccessfully() {
            var updateRequest = UpdateGoalTemplateRequest.builder()
                    .name("Updated Template")
                    .isPublic(true)
                    .build();

            when(goalTemplateRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(template));
            when(goalTemplateRepository.save(any(GoalTemplate.class))).thenReturn(template);
            when(goalTemplateMapper.toResponse(template)).thenReturn(response);

            var result = goalTemplateService.update(1L, updateRequest, user);

            assertThat(result).isNotNull();
            verify(goalTemplateRepository).save(any(GoalTemplate.class));
        }

        @Test
        @DisplayName("Should throw exception when template not found")
        void shouldThrowExceptionWhenTemplateNotFound() {
            var updateRequest = UpdateGoalTemplateRequest.builder().build();
            when(goalTemplateRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> goalTemplateService.update(1L, updateRequest, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.GOAL_TEMPLATE_NOT_FOUND);
        }

        @Test
        @DisplayName("Should update template with all fields")
        void shouldUpdateTemplateWithAllFields() {
            var updateRequest = UpdateGoalTemplateRequest.builder()
                    .name("Updated Name")
                    .description("Updated Description")
                    .defaultTitle("Updated Title")
                    .defaultDescription("Updated Default Description")
                    .defaultCategory(GoalCategory.FINANCE)
                    .defaultTargetValue("100")
                    .defaultUnit("dollars")
                    .defaultMotivation("Financial freedom")
                    .defaultDurationDays(180)
                    .isPublic(true)
                    .build();

            when(goalTemplateRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(template));
            when(goalTemplateRepository.save(any(GoalTemplate.class))).thenReturn(template);
            when(goalTemplateMapper.toResponse(template)).thenReturn(response);

            var result = goalTemplateService.update(1L, updateRequest, user);

            assertThat(result).isNotNull();
            verify(goalTemplateRepository).save(any(GoalTemplate.class));
        }
    }

    @Nested
    @DisplayName("Delete template tests")
    class DeleteTests {

        @Test
        @DisplayName("Should delete template successfully")
        void shouldDeleteTemplateSuccessfully() {
            when(goalTemplateRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(template));

            goalTemplateService.delete(1L, user);

            verify(goalTemplateRepository).delete(template);
        }

        @Test
        @DisplayName("Should throw exception when template not found")
        void shouldThrowExceptionWhenDeletingNonExistentTemplate() {
            when(goalTemplateRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> goalTemplateService.delete(1L, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.GOAL_TEMPLATE_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Create goal from template tests")
    class CreateGoalFromTemplateTests {

        @Test
        @DisplayName("Should create goal request from template")
        void shouldCreateGoalRequestFromTemplate() {
            when(goalTemplateRepository.findById(1L)).thenReturn(Optional.of(template));

            var result = goalTemplateService.createGoalFromTemplate(1L, user);

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(template.getDefaultTitle());
            assertThat(result.getGoalCategory()).isEqualTo(GoalCategory.HEALTH);
            assertThat(result.getTargetValue()).isEqualTo("5");
            assertThat(result.getUnit()).isEqualTo("km");
        }

        @Test
        @DisplayName("Should throw exception when template not accessible")
        void shouldThrowExceptionWhenTemplateNotAccessible() {
            var otherUser = User.builder().id(2L).email("other@test.com").build();
            var privateTemplate = GoalTemplate.builder()
                    .id(1L)
                    .owner(otherUser)
                    .isPublic(false)
                    .build();

            when(goalTemplateRepository.findById(1L)).thenReturn(Optional.of(privateTemplate));

            assertThatThrownBy(() -> goalTemplateService.createGoalFromTemplate(1L, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.GOAL_TEMPLATE_NOT_FOUND);
        }

        @Test
        @DisplayName("Should allow access to public template from other user")
        void shouldAllowAccessToPublicTemplate() {
            var otherUser = User.builder().id(2L).email("other@test.com").build();
            var publicTemplate = GoalTemplate.builder()
                    .id(1L)
                    .owner(otherUser)
                    .name("Public Template")
                    .defaultTitle("Public Goal")
                    .defaultDurationDays(90)
                    .isPublic(true)
                    .build();

            when(goalTemplateRepository.findById(1L)).thenReturn(Optional.of(publicTemplate));

            var result = goalTemplateService.createGoalFromTemplate(1L, user);

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Public Goal");
        }
    }
}
