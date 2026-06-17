package com.interview.evaluation.vo;

import lombok.Data;

@Data
public class FunnelStatVO {
    private String stage;
    private String stageName;
    private Integer count;
    private Double conversionRate;
}
