package com.relyon.metasmart.entity.goal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmotionalAnchorsDto {

    private String imageUrl;
    private String audioUrl;
    private String customMessage;
}
