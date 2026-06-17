package com.interview.evaluation.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ScoreDiscrepancyVO {
    private Long candidateId;
    private String candidateName;
    private Boolean hasDiscrepancy;
    private List<DimensionDiscrepancyVO> dimensionDiscrepancies;
}
