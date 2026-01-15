package com.relyon.metasmart.entity.guardian;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GoalGuardianTest {

    private GoalGuardian goalGuardian;

    @BeforeEach
    void setUp() {
        goalGuardian = GoalGuardian.builder()
                .id(1L)
                .status(GuardianStatus.ACTIVE)
                .permissions(Set.of(GuardianPermission.VIEW_PROGRESS, GuardianPermission.SEND_NUDGE))
                .build();
    }

    @Test
    @DisplayName("Should return true when guardian has permission")
    void shouldReturnTrueWhenHasPermission() {
        assertThat(goalGuardian.hasPermission(GuardianPermission.VIEW_PROGRESS)).isTrue();
        assertThat(goalGuardian.hasPermission(GuardianPermission.SEND_NUDGE)).isTrue();
    }

    @Test
    @DisplayName("Should return false when guardian does not have permission")
    void shouldReturnFalseWhenNoPermission() {
        assertThat(goalGuardian.hasPermission(GuardianPermission.VIEW_OBSTACLES)).isFalse();
    }

    @Test
    @DisplayName("Should return true when status is ACTIVE")
    void shouldReturnTrueWhenActive() {
        assertThat(goalGuardian.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should return false when status is PENDING")
    void shouldReturnFalseWhenPending() {
        goalGuardian.setStatus(GuardianStatus.PENDING);
        assertThat(goalGuardian.isActive()).isFalse();
    }

    @Test
    @DisplayName("Should return false when status is DECLINED")
    void shouldReturnFalseWhenDeclined() {
        goalGuardian.setStatus(GuardianStatus.DECLINED);
        assertThat(goalGuardian.isActive()).isFalse();
    }

    @Test
    @DisplayName("Should return false when status is REVOKED")
    void shouldReturnFalseWhenRevoked() {
        goalGuardian.setStatus(GuardianStatus.REVOKED);
        assertThat(goalGuardian.isActive()).isFalse();
    }
}
