package com.interview.evaluation.vo;

import lombok.Data;

@Data
public class TemplateDimensionVO {
    private Long id;
    private Long dimensionId;
    private String dimensionCode;
    private String dimensionName;
    private String dimensionDesc;
    private Integer weightPercent;
    private Integer maxScore;
    private Integer minScore;
    private Integer sortOrder;
}
