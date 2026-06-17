package com.interview.evaluation.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TemplateDetailVO {
    private Long templateId;
    private String templateName;
    private String templateDesc;
    private Integer isDefault;
    private Integer status;
    private LocalDateTime createTime;
    private List<TemplateDimensionVO> dimensions;
}
