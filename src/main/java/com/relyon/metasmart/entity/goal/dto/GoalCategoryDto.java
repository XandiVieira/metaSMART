package com.relyon.metasmart.entity.goal.dto;

import com.relyon.metasmart.entity.goal.GoalCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalCategoryDto {

    private GoalCategory id;
    private String name;

    public static GoalCategoryDto fromCategory(GoalCategory category) {
        if (category == null) return null;
        return GoalCategoryDto.builder()
                .id(category)
                .name(category.getDisplayName())
                .build();
    }
}
