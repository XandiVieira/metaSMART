package com.relyon.metasmart.controller;

import com.relyon.metasmart.constant.ApiPaths;
import com.relyon.metasmart.entity.journal.dto.DailyJournalRequest;
import com.relyon.metasmart.entity.journal.dto.DailyJournalResponse;
import com.relyon.metasmart.entity.journal.dto.UpdateDailyJournalRequest;
import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.service.DailyJournalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(ApiPaths.JOURNAL)
@RequiredArgsConstructor
@Tag(name = "Daily Journal")
public class DailyJournalController {

    private final DailyJournalService dailyJournalService;

    @PostMapping
    @Operation(summary = "Create a new journal entry")
    public ResponseEntity<DailyJournalResponse> createJournalEntry(
            @Valid @RequestBody DailyJournalRequest request,
            @AuthenticationPrincipal User user) {
        log.debug("Create journal entry request for user: {}", user.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dailyJournalService.createJournalEntry(request, user));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a journal entry by ID")
    public ResponseEntity<DailyJournalResponse> getJournalEntry(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        log.debug("Get journal entry request for ID: {}", id);
        return ResponseEntity.ok(dailyJournalService.getJournalEntry(id, user));
    }

    @GetMapping("/date/{date}")
    @Operation(summary = "Get journal entry by date")
    public ResponseEntity<DailyJournalResponse> getJournalEntryByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal User user) {
        log.debug("Get journal entry request for date: {}", date);
        return dailyJournalService.getJournalEntryByDate(date, user)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get journal history")
    public ResponseEntity<Page<DailyJournalResponse>> getJournalHistory(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20, sort = "journalDate", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("Get journal history request for user: {}", user.getEmail());
        return ResponseEntity.ok(dailyJournalService.getJournalHistory(user, pageable));
    }

    @GetMapping("/range")
    @Operation(summary = "Get journal entries by date range")
    public ResponseEntity<List<DailyJournalResponse>> getJournalEntriesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal User user) {
        log.debug("Get journal entries request for date range: {} to {}", startDate, endDate);
        return ResponseEntity.ok(dailyJournalService.getJournalEntriesByDateRange(user, startDate, endDate));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a journal entry")
    public ResponseEntity<DailyJournalResponse> updateJournalEntry(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDailyJournalRequest request,
            @AuthenticationPrincipal User user) {
        log.debug("Update journal entry request for ID: {}", id);
        return ResponseEntity.ok(dailyJournalService.updateJournalEntry(id, request, user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a journal entry")
    public ResponseEntity<Void> deleteJournalEntry(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        log.debug("Delete journal entry request for ID: {}", id);
        dailyJournalService.deleteJournalEntry(id, user);
        return ResponseEntity.noContent().build();
    }
}
