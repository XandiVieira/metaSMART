package com.relyon.metasmart.repository;

import com.relyon.metasmart.entity.struggling.StrugglingRequest;
import com.relyon.metasmart.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StrugglingRequestRepository extends JpaRepository<StrugglingRequest, Long> {

    @Query("SELECT COUNT(sr) FROM StrugglingRequest sr WHERE sr.user = :user AND sr.createdAt >= :startOfMonth")
    long countByUserThisMonth(@Param("user") User user, @Param("startOfMonth") LocalDateTime startOfMonth);

    Page<StrugglingRequest> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    List<StrugglingRequest> findByGoalIdAndUserOrderByCreatedAtDesc(Long goalId, User user);
}
