package com.interview.evaluation.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.interview.evaluation.entity.EvaluationDimension;
import com.interview.evaluation.mapper.EvaluationDimensionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EvaluationDimensionService {

    @Autowired
    private EvaluationDimensionMapper evaluationDimensionMapper;

    public List<EvaluationDimension> getDefaultDimensions() {
        QueryWrapper<EvaluationDimension> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_default", 1);
        queryWrapper.orderByAsc("sort_order");
        return evaluationDimensionMapper.selectList(queryWrapper);
    }

    public List<EvaluationDimension> getAllDimensions() {
        QueryWrapper<EvaluationDimension> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("sort_order");
        return evaluationDimensionMapper.selectList(queryWrapper);
    }
}
