package com.relyon.metasmart.entity.guardian;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GuardianNudgeTest {

    @Test
    @DisplayName("Should return true when nudge is read")
    void shouldReturnTrueWhenRead() {
        var nudge = GuardianNudge.builder()
                .id(1L)
                .message("Keep going!")
                .nudgeType(NudgeType.ENCOURAGEMENT)
                .readAt(LocalDateTime.now())
                .build();

        assertThat(nudge.isRead()).isTrue();
    }

    @Test
    @DisplayName("Should return false when nudge is not read")
    void shouldReturnFalseWhenNotRead() {
        var nudge = GuardianNudge.builder()
                .id(1L)
                .message("Keep going!")
                .nudgeType(NudgeType.ENCOURAGEMENT)
                .readAt(null)
                .build();

        assertThat(nudge.isRead()).isFalse();
    }
}
