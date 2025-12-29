package com.relyon.metasmart.entity.goal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GoalCategory {
    HEALTH("Saúde"),
    FINANCE("Finanças"),
    EDUCATION("Estudos"),
    CAREER("Carreira"),
    RELATIONSHIPS("Relacionamentos"),
    PERSONAL_DEVELOPMENT("Desenvolvimento Pessoal"),
    HOBBIES("Hobbies"),
    OTHER("Outros");

    private final String displayName;
}
