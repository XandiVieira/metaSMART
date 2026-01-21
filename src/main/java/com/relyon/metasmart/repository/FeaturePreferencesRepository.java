package com.relyon.metasmart.repository;

import com.relyon.metasmart.entity.feature.FeaturePreferences;
import com.relyon.metasmart.entity.user.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeaturePreferencesRepository extends JpaRepository<FeaturePreferences, Long> {

    Optional<FeaturePreferences> findByUser(User user);

    boolean existsByUser(User user);
}
