package com.relyon.metasmart.service;

import com.relyon.metasmart.constant.ErrorMessages;
import com.relyon.metasmart.entity.journal.DailyJournal;
import com.relyon.metasmart.entity.journal.dto.DailyJournalRequest;
import com.relyon.metasmart.entity.journal.dto.DailyJournalResponse;
import com.relyon.metasmart.entity.journal.dto.UpdateDailyJournalRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.exception.DuplicateResourceException;
import com.relyon.metasmart.exception.ResourceNotFoundException;
import com.relyon.metasmart.mapper.DailyJournalMapper;
import com.relyon.metasmart.repository.DailyJournalRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyJournalService {

    private final DailyJournalRepository dailyJournalRepository;
    private final DailyJournalMapper dailyJournalMapper;
    private final UserStreakService userStreakService;

    @Transactional
    public DailyJournalResponse createJournalEntry(DailyJournalRequest request, User user) {
        log.debug("Creating journal entry for user: {} on date: {}", user.getEmail(), request.getJournalDate());

        if (dailyJournalRepository.existsByUserAndJournalDate(user, request.getJournalDate())) {
            log.warn("Journal entry already exists for user: {} on date: {}", user.getEmail(), request.getJournalDate());
            throw new DuplicateResourceException(ErrorMessages.DAILY_JOURNAL_ALREADY_EXISTS);
        }

        var journal = dailyJournalMapper.toEntity(request);
        journal.setUser(user);
        journal.setShieldUsed(false);

        var savedJournal = dailyJournalRepository.save(journal);
        log.info("Journal entry created with ID: {} for user: {}", savedJournal.getId(), user.getEmail());

        userStreakService.checkAndAwardJournalShield(user, request.getJournalDate());

        return dailyJournalMapper.toResponse(savedJournal);
    }

    @Transactional(readOnly = true)
    public DailyJournalResponse getJournalEntry(Long id, User user) {
        log.debug("Fetching journal entry ID: {} for user: {}", id, user.getEmail());

        var journal = dailyJournalRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> {
                    log.warn("Journal entry not found with ID: {} for user: {}", id, user.getEmail());
                    return new ResourceNotFoundException(ErrorMessages.DAILY_JOURNAL_NOT_FOUND);
                });

        return dailyJournalMapper.toResponse(journal);
    }

    @Transactional(readOnly = true)
    public Optional<DailyJournalResponse> getJournalEntryByDate(LocalDate date, User user) {
        log.debug("Fetching journal entry for user: {} on date: {}", user.getEmail(), date);

        return dailyJournalRepository.findByUserAndJournalDate(user, date)
                .map(dailyJournalMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<DailyJournalResponse> getJournalHistory(User user, Pageable pageable) {
        log.debug("Fetching journal history for user: {}", user.getEmail());

        return dailyJournalRepository.findByUserOrderByJournalDateDesc(user, pageable)
                .map(dailyJournalMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<DailyJournalResponse> getJournalEntriesByDateRange(User user, LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching journal entries for user: {} between {} and {}", user.getEmail(), startDate, endDate);

        return dailyJournalRepository.findByUserAndJournalDateBetweenOrderByJournalDateDesc(user, startDate, endDate)
                .stream()
                .map(dailyJournalMapper::toResponse)
                .toList();
    }

    @Transactional
    public DailyJournalResponse updateJournalEntry(Long id, UpdateDailyJournalRequest request, User user) {
        log.debug("Updating journal entry ID: {} for user: {}", id, user.getEmail());

        var journal = dailyJournalRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> {
                    log.warn("Journal entry not found with ID: {} for user: {}", id, user.getEmail());
                    return new ResourceNotFoundException(ErrorMessages.DAILY_JOURNAL_NOT_FOUND);
                });

        Optional.ofNullable(request.getContent()).ifPresent(journal::setContent);
        Optional.ofNullable(request.getMood()).ifPresent(journal::setMood);

        var savedJournal = dailyJournalRepository.save(journal);
        log.info("Journal entry updated with ID: {} for user: {}", savedJournal.getId(), user.getEmail());

        return dailyJournalMapper.toResponse(savedJournal);
    }

    @Transactional
    public void deleteJournalEntry(Long id, User user) {
        log.debug("Deleting journal entry ID: {} for user: {}", id, user.getEmail());

        var journal = dailyJournalRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> {
                    log.warn("Journal entry not found with ID: {} for user: {}", id, user.getEmail());
                    return new ResourceNotFoundException(ErrorMessages.DAILY_JOURNAL_NOT_FOUND);
                });

        dailyJournalRepository.delete(journal);
        log.info("Journal entry deleted with ID: {} for user: {}", id, user.getEmail());
    }

    @Transactional(readOnly = true)
    public long getJournalCountForMonth(User user, LocalDate monthStart) {
        var monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
        return dailyJournalRepository.countByUserAndMonth(user, monthStart, monthEnd);
    }

    @Transactional(readOnly = true)
    public boolean hasJournalEntryOnDate(User user, LocalDate date) {
        return dailyJournalRepository.existsByUserAndJournalDate(user, date);
    }

    @Transactional
    public void markShieldUsed(User user, LocalDate date) {
        dailyJournalRepository.findByUserAndJournalDate(user, date)
                .ifPresent(journal -> {
                    journal.setShieldUsed(true);
                    dailyJournalRepository.save(journal);
                    log.info("Shield marked as used for user: {} on date: {}", user.getEmail(), date);
                });
    }

    List<DailyJournal> findByUserAndDateRange(User user, LocalDate startDate, LocalDate endDate) {
        return dailyJournalRepository.findByUserAndJournalDateBetweenOrderByJournalDateDesc(user, startDate, endDate);
    }
}
