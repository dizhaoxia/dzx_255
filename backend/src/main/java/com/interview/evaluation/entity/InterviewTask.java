package com.interview.evaluation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("interview_task")
public class InterviewTask {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long candidateId;

    private Long positionId;

    private Long interviewerId;

    private Integer interviewRound;

    private String interviewType;

    private LocalDateTime interviewTime;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
