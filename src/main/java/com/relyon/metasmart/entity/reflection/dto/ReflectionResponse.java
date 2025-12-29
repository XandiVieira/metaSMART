package com.relyon.metasmart.entity.reflection.dto;

import com.relyon.metasmart.entity.reflection.ReflectionRating;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReflectionResponse {

    private Long id;
    private Long goalId;
    private String goalTitle;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private ReflectionRating rating;
    private String wentWell;
    private String challenges;
    private String adjustments;
    private String moodNote;
    private Boolean willContinue;
    private Integer motivationLevel;
    private LocalDateTime createdAt;
}
