package com.interview.evaluation.dto;

import lombok.Data;
import java.util.List;

@Data
public class EvaluationSubmitDTO {
    private Long taskId;
    private List<DimensionScoreDTO> dimensionScores;
    private String overallComment;
    private String hireSuggestion;
}
