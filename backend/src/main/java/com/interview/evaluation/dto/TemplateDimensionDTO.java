package com.interview.evaluation.dto;

import lombok.Data;

@Data
public class TemplateDimensionDTO {
    private Long dimensionId;
    private String dimensionCode;
    private String dimensionName;
    private String dimensionDesc;
    private Integer weightPercent;
    private Integer maxScore;
    private Integer minScore;
    private Integer sortOrder;
}
