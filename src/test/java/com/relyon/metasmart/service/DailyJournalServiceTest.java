package com.relyon.metasmart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.relyon.metasmart.constant.ErrorMessages;
import com.relyon.metasmart.entity.journal.DailyJournal;
import com.relyon.metasmart.entity.journal.Mood;
import com.relyon.metasmart.entity.journal.dto.DailyJournalRequest;
import com.relyon.metasmart.entity.journal.dto.DailyJournalResponse;
import com.relyon.metasmart.entity.journal.dto.UpdateDailyJournalRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.DuplicateResourceException;
import com.relyon.metasmart.exception.ResourceNotFoundException;
import com.relyon.metasmart.mapper.DailyJournalMapper;
import com.relyon.metasmart.repository.DailyJournalRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
class DailyJournalServiceTest {

    @Mock
    private DailyJournalRepository dailyJournalRepository;

    @Mock
    private DailyJournalMapper dailyJournalMapper;

    @Mock
    private UserStreakService userStreakService;

    @InjectMocks
    private DailyJournalService dailyJournalService;

    private User user;
    private DailyJournal journal;
    private DailyJournalRequest journalRequest;
    private DailyJournalResponse journalResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("John")
                .email("john@test.com")
                .build();

        journal = DailyJournal.builder()
                .id(1L)
                .user(user)
                .journalDate(LocalDate.now())
                .content("Today was productive")
                .mood(Mood.GOOD)
                .shieldUsed(false)
                .build();

        journalRequest = DailyJournalRequest.builder()
                .journalDate(LocalDate.now())
                .content("Today was productive")
                .mood(Mood.GOOD)
                .build();

        journalResponse = DailyJournalResponse.builder()
                .id(1L)
                .journalDate(LocalDate.now())
                .content("Today was productive")
                .mood(Mood.GOOD)
                .shieldUsed(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Create journal entry tests")
    class CreateJournalEntryTests {

        @Test
        @DisplayName("Should create journal entry successfully")
        void shouldCreateJournalEntrySuccessfully() {
            when(dailyJournalRepository.existsByUserAndJournalDate(user, journalRequest.getJournalDate()))
                    .thenReturn(false);
            when(dailyJournalMapper.toEntity(journalRequest)).thenReturn(journal);
            when(dailyJournalRepository.save(any(DailyJournal.class))).thenReturn(journal);
            when(dailyJournalMapper.toResponse(journal)).thenReturn(journalResponse);

            var response = dailyJournalService.createJournalEntry(journalRequest, user);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getContent()).isEqualTo("Today was productive");
            assertThat(response.getMood()).isEqualTo(Mood.GOOD);
            verify(userStreakService).checkAndAwardJournalShield(user, journalRequest.getJournalDate());
        }

        @Test
        @DisplayName("Should throw exception when journal already exists for date")
        void shouldThrowExceptionWhenJournalAlreadyExistsForDate() {
            when(dailyJournalRepository.existsByUserAndJournalDate(user, journalRequest.getJournalDate()))
                    .thenReturn(true);

            assertThatThrownBy(() -> dailyJournalService.createJournalEntry(journalRequest, user))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessage(ErrorMessages.DAILY_JOURNAL_ALREADY_EXISTS);

            verify(dailyJournalRepository, never()).save(any());
            verify(userStreakService, never()).checkAndAwardJournalShield(any(), any());
        }

        @Test
        @DisplayName("Should set shield used to false on creation")
        void shouldSetShieldUsedToFalseOnCreation() {
            when(dailyJournalRepository.existsByUserAndJournalDate(user, journalRequest.getJournalDate()))
                    .thenReturn(false);
            when(dailyJournalMapper.toEntity(journalRequest)).thenReturn(journal);
            when(dailyJournalRepository.save(any(DailyJournal.class))).thenReturn(journal);
            when(dailyJournalMapper.toResponse(journal)).thenReturn(journalResponse);

            dailyJournalService.createJournalEntry(journalRequest, user);

            verify(dailyJournalRepository).save(argThat(j ->
                    Boolean.FALSE.equals(j.getShieldUsed()) && j.getUser().equals(user)
            ));
        }
    }

    @Nested
    @DisplayName("Get journal entry tests")
    class GetJournalEntryTests {

        @Test
        @DisplayName("Should get journal entry by ID")
        void shouldGetJournalEntryById() {
            when(dailyJournalRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(journal));
            when(dailyJournalMapper.toResponse(journal)).thenReturn(journalResponse);

            var response = dailyJournalService.getJournalEntry(1L, user);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw exception when journal not found by ID")
        void shouldThrowExceptionWhenJournalNotFoundById() {
            when(dailyJournalRepository.findByIdAndUser(1L, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> dailyJournalService.getJournalEntry(1L, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.DAILY_JOURNAL_NOT_FOUND);
        }

        @Test
        @DisplayName("Should get journal entry by date")
        void shouldGetJournalEntryByDate() {
            var date = LocalDate.now();
            when(dailyJournalRepository.findByUserAndJournalDate(user, date)).thenReturn(Optional.of(journal));
            when(dailyJournalMapper.toResponse(journal)).thenReturn(journalResponse);

            var response = dailyJournalService.getJournalEntryByDate(date, user);

            assertThat(response).isPresent();
            assertThat(response.get().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should return empty when journal not found by date")
        void shouldReturnEmptyWhenJournalNotFoundByDate() {
            var date = LocalDate.now();
            when(dailyJournalRepository.findByUserAndJournalDate(user, date)).thenReturn(Optional.empty());

            var response = dailyJournalService.getJournalEntryByDate(date, user);

            assertThat(response).isEmpty();
        }
    }

    @Nested
    @DisplayName("Get journal history tests")
    class GetJournalHistoryTests {

        @Test
        @DisplayName("Should get journal history")
        void shouldGetJournalHistory() {
            var pageable = Pageable.unpaged();
            var page = new PageImpl<>(List.of(journal));
            when(dailyJournalRepository.findByUserOrderByJournalDateDesc(user, pageable)).thenReturn(page);
            when(dailyJournalMapper.toResponse(journal)).thenReturn(journalResponse);

            var response = dailyJournalService.getJournalHistory(user, pageable);

            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should get journal entries by date range")
        void shouldGetJournalEntriesByDateRange() {
            var startDate = LocalDate.now().minusDays(7);
            var endDate = LocalDate.now();
            when(dailyJournalRepository.findByUserAndJournalDateBetweenOrderByJournalDateDesc(user, startDate, endDate))
                    .thenReturn(List.of(journal));
            when(dailyJournalMapper.toResponse(journal)).thenReturn(journalResponse);

            var response = dailyJournalService.getJournalEntriesByDateRange(user, startDate, endDate);

            assertThat(response).hasSize(1);
            assertThat(response.get(0).getId()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("Update journal entry tests")
    class UpdateJournalEntryTests {

        @Test
        @DisplayName("Should update journal entry content")
        void shouldUpdateJournalEntryContent() {
            var updateRequest = UpdateDailyJournalRequest.builder()
                    .content("Updated content")
                    .build();

            when(dailyJournalRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(journal));
            when(dailyJournalRepository.save(any(DailyJournal.class))).thenReturn(journal);
            when(dailyJournalMapper.toResponse(journal)).thenReturn(journalResponse);

            var response = dailyJournalService.updateJournalEntry(1L, updateRequest, user);

            assertThat(response).isNotNull();
            verify(dailyJournalRepository).save(argThat(j -> j.getContent().equals("Updated content")));
        }

        @Test
        @DisplayName("Should update journal entry mood")
        void shouldUpdateJournalEntryMood() {
            var updateRequest = UpdateDailyJournalRequest.builder()
                    .mood(Mood.GREAT)
                    .build();

            when(dailyJournalRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(journal));
            when(dailyJournalRepository.save(any(DailyJournal.class))).thenReturn(journal);
            when(dailyJournalMapper.toResponse(journal)).thenReturn(journalResponse);

            dailyJournalService.updateJournalEntry(1L, updateRequest, user);

            verify(dailyJournalRepository).save(argThat(j -> j.getMood() == Mood.GREAT));
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent journal")
        void shouldThrowExceptionWhenUpdatingNonExistentJournal() {
            var updateRequest = UpdateDailyJournalRequest.builder()
                    .content("Updated content")
                    .build();

            when(dailyJournalRepository.findByIdAndUser(1L, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> dailyJournalService.updateJournalEntry(1L, updateRequest, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.DAILY_JOURNAL_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Delete journal entry tests")
    class DeleteJournalEntryTests {

        @Test
        @DisplayName("Should delete journal entry")
        void shouldDeleteJournalEntry() {
            when(dailyJournalRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(journal));

            dailyJournalService.deleteJournalEntry(1L, user);

            verify(dailyJournalRepository).delete(journal);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent journal")
        void shouldThrowExceptionWhenDeletingNonExistentJournal() {
            when(dailyJournalRepository.findByIdAndUser(1L, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> dailyJournalService.deleteJournalEntry(1L, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(ErrorMessages.DAILY_JOURNAL_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Utility method tests")
    class UtilityMethodTests {

        @Test
        @DisplayName("Should get journal count for month")
        void shouldGetJournalCountForMonth() {
            var monthStart = LocalDate.now().withDayOfMonth(1);
            when(dailyJournalRepository.countByUserAndMonth(eq(user), eq(monthStart), any()))
                    .thenReturn(15L);

            var count = dailyJournalService.getJournalCountForMonth(user, monthStart);

            assertThat(count).isEqualTo(15L);
        }

        @Test
        @DisplayName("Should check if journal exists on date")
        void shouldCheckIfJournalExistsOnDate() {
            var date = LocalDate.now();
            when(dailyJournalRepository.existsByUserAndJournalDate(user, date)).thenReturn(true);

            var exists = dailyJournalService.hasJournalEntryOnDate(user, date);

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should mark shield as used")
        void shouldMarkShieldAsUsed() {
            var date = LocalDate.now();
            when(dailyJournalRepository.findByUserAndJournalDate(user, date)).thenReturn(Optional.of(journal));

            dailyJournalService.markShieldUsed(user, date);

            verify(dailyJournalRepository).save(argThat(j -> Boolean.TRUE.equals(j.getShieldUsed())));
        }

        @Test
        @DisplayName("Should not fail when marking shield on non-existent journal")
        void shouldNotFailWhenMarkingShieldOnNonExistentJournal() {
            var date = LocalDate.now();
            when(dailyJournalRepository.findByUserAndJournalDate(user, date)).thenReturn(Optional.empty());

            dailyJournalService.markShieldUsed(user, date);

            verify(dailyJournalRepository, never()).save(any());
        }
    }
}
