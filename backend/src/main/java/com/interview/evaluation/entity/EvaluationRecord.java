package com.interview.evaluation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("evaluation_record")
public class EvaluationRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private Long candidateId;

    private Long interviewerId;

    private Long dimensionId;

    private String dimensionCode;

    private Integer score;

    private String comment;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
