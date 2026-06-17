package com.interview.evaluation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("evaluation_dimension")
public class EvaluationDimension {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String dimensionCode;

    private String dimensionName;

    private String dimensionDesc;

    private Integer maxScore;

    private Integer minScore;

    private Integer sortOrder;

    private Integer isDefault;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
