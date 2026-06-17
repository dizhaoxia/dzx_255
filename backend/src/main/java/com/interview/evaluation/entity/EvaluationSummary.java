package com.interview.evaluation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("evaluation_summary")
public class EvaluationSummary {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private Long candidateId;

    private Long interviewerId;

    private String overallComment;

    private String hireSuggestion;

    private BigDecimal totalScore;

    private String status;

    private LocalDateTime submitTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
