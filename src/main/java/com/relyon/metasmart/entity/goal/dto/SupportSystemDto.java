package com.relyon.metasmart.entity.goal.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportSystemDto {

    private List<AccountabilityPartnerDto> accountabilityPartners;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountabilityPartnerDto {
        private String name;
        private String contact;
        private String relation;
    }
}
