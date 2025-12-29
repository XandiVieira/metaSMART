package com.relyon.metasmart.entity.reflection.dto;

import com.relyon.metasmart.entity.reflection.ReflectionRating;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReflectionRequest {

    @NotNull(message = "Rating is required")
    private ReflectionRating rating;

    @Size(max = 1000, message = "What went well must be at most 1000 characters")
    private String wentWell;

    @Size(max = 1000, message = "Challenges must be at most 1000 characters")
    private String challenges;

    @Size(max = 1000, message = "Adjustments must be at most 1000 characters")
    private String adjustments;

    @Size(max = 500, message = "Mood note must be at most 500 characters")
    private String moodNote;

    private Boolean willContinue;

    @Min(value = 1, message = "Motivation level must be between 1 and 10")
    @Max(value = 10, message = "Motivation level must be between 1 and 10")
    private Integer motivationLevel;
}
