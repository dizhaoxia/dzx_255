package com.interview.evaluation.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InterviewTaskVO {
    private Long taskId;
    private Long candidateId;
    private String candidateName;
    private String gender;
    private Integer age;
    private String education;
    private Integer workYears;
    private Long positionId;
    private String positionName;
    private String department;
    private Integer interviewRound;
    private String interviewType;
    private LocalDateTime interviewTime;
    private String status;
    private String interviewerName;
    private String videoUrl;
}
