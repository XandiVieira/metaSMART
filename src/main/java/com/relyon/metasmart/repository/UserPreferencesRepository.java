package com.relyon.metasmart.repository;

import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.entity.user.UserPreferences;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Long> {

    Optional<UserPreferences> findByUser(User user);

    boolean existsByUser(User user);
}
