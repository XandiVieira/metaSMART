package com.relyon.metasmart.repository;

import com.relyon.metasmart.entity.user.User;
import com.relyon.metasmart.entity.user.UserPreferences;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Long> {

    Optional<UserPreferences> findByUser(User user);

    boolean existsByUser(User user);
}
