package com.interview.evaluation.dto;

import lombok.Data;

@Data
public class DimensionScoreDTO {
    private Long dimensionId;
    private String dimensionCode;
    private Integer score;
    private String comment;
}
