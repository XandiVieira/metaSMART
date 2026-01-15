package com.relyon.metasmart.repository;

import com.relyon.metasmart.entity.notification.NotificationPreferences;
import com.relyon.metasmart.entity.user.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationPreferencesRepository extends JpaRepository<NotificationPreferences, Long> {

    Optional<NotificationPreferences> findByUser(User user);

    boolean existsByUser(User user);
}
