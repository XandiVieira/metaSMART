package com.relyon.metasmart.repository;

import com.relyon.metasmart.entity.actionplan.ActionItem;
import com.relyon.metasmart.entity.actionplan.TaskScheduleSlot;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskScheduleSlotRepository extends JpaRepository<TaskScheduleSlot, Long> {

    List<TaskScheduleSlot> findByActionItemOrderBySlotIndexAsc(ActionItem actionItem);

    @Query("SELECT s FROM TaskScheduleSlot s WHERE s.actionItem = :actionItem " +
           "AND s.effectiveFrom <= :date " +
           "AND (s.effectiveUntil IS NULL OR s.effectiveUntil >= :date) " +
           "ORDER BY s.slotIndex ASC")
    List<TaskScheduleSlot> findActiveSlots(@Param("actionItem") ActionItem actionItem, @Param("date") LocalDate date);

    default List<TaskScheduleSlot> findActiveSlots(ActionItem actionItem) {
        return findActiveSlots(actionItem, LocalDate.now());
    }

    @Query("SELECT s FROM TaskScheduleSlot s WHERE s.actionItem = :actionItem " +
           "AND s.slotIndex = :slotIndex " +
           "AND s.effectiveFrom <= :date " +
           "AND (s.effectiveUntil IS NULL OR s.effectiveUntil >= :date)")
    List<TaskScheduleSlot> findActiveSlotByIndex(
            @Param("actionItem") ActionItem actionItem,
            @Param("slotIndex") Integer slotIndex,
            @Param("date") LocalDate date);

    long countByActionItem(ActionItem actionItem);

    void deleteByActionItem(ActionItem actionItem);
}
