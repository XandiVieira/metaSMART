package com.relyon.metasmart.entity.goal;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmotionalAnchors {

    @Column(name = "emotional_image_url", length = 500)
    private String imageUrl;

    @Column(name = "emotional_audio_url", length = 500)
    private String audioUrl;

    @Column(name = "emotional_custom_message", length = 1000)
    private String customMessage;
}
