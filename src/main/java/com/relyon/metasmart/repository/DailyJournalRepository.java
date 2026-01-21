package com.relyon.metasmart.repository;

import com.relyon.metasmart.entity.journal.DailyJournal;
import com.relyon.metasmart.entity.user.User;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DailyJournalRepository extends JpaRepository<DailyJournal, Long> {

    Optional<DailyJournal> findByUserAndJournalDate(User user, LocalDate journalDate);

    Optional<DailyJournal> findByIdAndUser(Long id, User user);

    Page<DailyJournal> findByUserOrderByJournalDateDesc(User user, Pageable pageable);

    List<DailyJournal> findByUserAndJournalDateBetweenOrderByJournalDateDesc(
            User user, LocalDate startDate, LocalDate endDate);

    @Query("SELECT COUNT(d) FROM DailyJournal d WHERE d.user = :user AND d.shieldUsed = true " +
            "AND d.journalDate >= :weekStart AND d.journalDate <= :weekEnd")
    int countShieldsUsedInWeek(@Param("user") User user,
                               @Param("weekStart") LocalDate weekStart,
                               @Param("weekEnd") LocalDate weekEnd);

    @Query("SELECT d.journalDate FROM DailyJournal d WHERE d.user = :user " +
            "AND d.journalDate BETWEEN :startDate AND :endDate ORDER BY d.journalDate")
    List<LocalDate> findJournalDatesByUserAndDateRange(@Param("user") User user,
                                                       @Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);

    boolean existsByUserAndJournalDate(User user, LocalDate journalDate);

    @Query("SELECT COUNT(d) FROM DailyJournal d WHERE d.user = :user " +
            "AND d.journalDate >= :startDate AND d.journalDate <= :endDate")
    long countByUserAndMonth(@Param("user") User user,
                             @Param("startDate") LocalDate startDate,
                             @Param("endDate") LocalDate endDate);
}
