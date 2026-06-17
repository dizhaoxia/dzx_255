package com.interview.evaluation.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class EvaluationSummaryVO {
    private Long candidateId;
    private String candidateName;
    private String positionName;
    private List<InterviewerEvaluationVO> interviewerEvaluations;
    private Map<String, Double> dimensionAverages;
    private Double totalAverage;
    private Integer evaluationCount;
}
