package com.relyon.metasmart.repository;

import com.relyon.metasmart.entity.notification.NotificationPreferences;
import com.relyon.metasmart.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationPreferencesRepository extends JpaRepository<NotificationPreferences, Long> {

    Optional<NotificationPreferences> findByUser(User user);

    boolean existsByUser(User user);
}
