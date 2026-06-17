package com.interview.evaluation.dto;

import lombok.Data;
import java.util.List;

@Data
public class TemplateSaveDTO {
    private Long templateId;
    private String templateName;
    private String templateDesc;
    private Integer isDefault;
    private List<TemplateDimensionDTO> dimensions;
}
