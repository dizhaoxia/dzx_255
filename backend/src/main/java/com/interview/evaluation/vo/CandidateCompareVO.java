package com.interview.evaluation.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class CandidateCompareVO {
    private Long candidateId;
    private String candidateName;
    private String positionName;
    private Double totalWeightedScore;
    private Map<String, Double> dimensionScores;
    private Map<String, Double> weightedDimensionScores;
}
