package com.interview.evaluation.vo;

import com.interview.evaluation.entity.EvaluationRecord;
import com.interview.evaluation.entity.EvaluationSummary;
import lombok.Data;
import java.util.List;

@Data
public class EvaluationDetailVO {
    private Long taskId;
    private Long candidateId;
    private String candidateName;
    private String positionName;
    private String interviewType;
    private Integer interviewRound;
    private String status;
    private List<DimensionScoreVO> dimensionScores;
    private String overallComment;
    private String hireSuggestion;
    private Double totalScore;
}
