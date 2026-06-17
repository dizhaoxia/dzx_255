package com.interview.evaluation.vo;

import lombok.Data;
import java.util.List;

@Data
public class CandidateProgressVO {
    private Long candidateId;
    private String candidateName;
    private String gender;
    private Integer age;
    private String education;
    private Integer workYears;
    private String phone;
    private String email;
    private Long positionId;
    private String positionName;
    private String overallStatus;
    private Integer totalTasks;
    private Integer submittedCount;
    private Integer draftCount;
    private Integer pendingCount;
    private List<InterviewTaskVO> tasks;
}
