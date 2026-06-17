package com.interview.evaluation.vo;

import lombok.Data;

@Data
public class InterviewerScoreVO {
    private Long interviewerId;
    private String interviewerName;
    private Integer score;
}
