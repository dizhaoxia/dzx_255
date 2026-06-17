package com.interview.evaluation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("template_dimension")
public class TemplateDimension {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long templateId;

    private Long dimensionId;

    private String dimensionCode;

    private String dimensionName;

    private String dimensionDesc;

    private Integer weightPercent;

    private Integer maxScore;

    private Integer minScore;

    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
