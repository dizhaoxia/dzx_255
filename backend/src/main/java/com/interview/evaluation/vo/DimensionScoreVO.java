package com.interview.evaluation.vo;

import lombok.Data;

@Data
public class DimensionScoreVO {
    private Long dimensionId;
    private String dimensionCode;
    private String dimensionName;
    private Integer score;
    private String comment;
    private Integer maxScore;
    private Integer minScore;
}
