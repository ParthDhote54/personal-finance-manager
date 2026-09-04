package com.personal.finance.manager.category.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.personal.finance.manager.category.entity.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private CategoryType type;
    @JsonProperty("isCustom")
    private boolean isCustom;

    @JsonProperty("custom")
    public boolean isCustomCategory() {
        return isCustom;
    }
}
