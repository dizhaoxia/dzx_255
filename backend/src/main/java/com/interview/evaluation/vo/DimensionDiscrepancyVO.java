package com.interview.evaluation.vo;

import lombok.Data;
import java.util.List;

@Data
public class DimensionDiscrepancyVO {
    private String dimensionCode;
    private String dimensionName;
    private Integer maxScore;
    private Integer minScore;
    private Integer range;
    private List<InterviewerScoreVO> interviewerScores;
}
