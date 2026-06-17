package com.interview.evaluation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("candidate")
public class Candidate {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String gender;

    private Integer age;

    private String phone;

    private String email;

    private Long positionId;

    private String education;

    private Integer workYears;

    private String resumeUrl;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
