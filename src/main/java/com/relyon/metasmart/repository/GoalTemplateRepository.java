package com.relyon.metasmart.repository;

import com.relyon.metasmart.entity.template.GoalTemplate;
import com.relyon.metasmart.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GoalTemplateRepository extends JpaRepository<GoalTemplate, Long> {

    Page<GoalTemplate> findByOwnerOrderByCreatedAtDesc(User owner, Pageable pageable);

    Optional<GoalTemplate> findByIdAndOwner(Long id, User owner);

    @Query("SELECT t FROM GoalTemplate t WHERE t.isPublic = true OR t.owner = :user ORDER BY t.createdAt DESC")
    Page<GoalTemplate> findAvailableTemplates(@Param("user") User user, Pageable pageable);

    Page<GoalTemplate> findByIsPublicTrueOrderByCreatedAtDesc(Pageable pageable);
}
