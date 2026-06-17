package com.interview.evaluation.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class InterviewerEvaluationVO {
    private Long taskId;
    private Long interviewerId;
    private String interviewerName;
    private Integer interviewRound;
    private String interviewType;
    private LocalDateTime submitTime;
    private String hireSuggestion;
    private String overallComment;
    private Double totalScore;
    private List<DimensionScoreVO> dimensionScores;
}
