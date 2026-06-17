package com.interview.evaluation.vo;

import com.interview.evaluation.entity.BehaviorTag;
import lombok.Data;
import java.util.List;

@Data
public class DimensionWithTagsVO {
    private Long dimensionId;
    private String dimensionCode;
    private String dimensionName;
    private String dimensionDesc;
    private Integer maxScore;
    private Integer minScore;
    private Integer weightPercent;
    private Integer sortOrder;
    private List<BehaviorTag> tags;
}
