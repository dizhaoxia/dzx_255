package com.interview.evaluation.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.interview.evaluation.entity.BehaviorTag;
import com.interview.evaluation.entity.EvaluationDimension;
import com.interview.evaluation.entity.PositionTemplate;
import com.interview.evaluation.entity.TemplateDimension;
import com.interview.evaluation.mapper.BehaviorTagMapper;
import com.interview.evaluation.mapper.EvaluationDimensionMapper;
import com.interview.evaluation.mapper.PositionTemplateMapper;
import com.interview.evaluation.mapper.TemplateDimensionMapper;
import com.interview.evaluation.vo.DimensionWithTagsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BehaviorTagService {

    @Autowired
    private BehaviorTagMapper behaviorTagMapper;

    @Autowired
    private PositionTemplateMapper positionTemplateMapper;

    @Autowired
    private TemplateDimensionMapper templateDimensionMapper;

    @Autowired
    private EvaluationDimensionMapper evaluationDimensionMapper;

    public List<BehaviorTag> getTagsByDimensionId(Long dimensionId) {
        QueryWrapper<BehaviorTag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dimension_id", dimensionId);
        queryWrapper.orderByAsc("sort_order");
        return behaviorTagMapper.selectList(queryWrapper);
    }

    public List<DimensionWithTagsVO> getTagsByPositionId(Long positionId) {
        QueryWrapper<PositionTemplate> ptQuery = new QueryWrapper<>();
        ptQuery.eq("position_id", positionId);
        PositionTemplate positionTemplate = positionTemplateMapper.selectOne(ptQuery);

        List<DimensionWithTagsVO> result = new ArrayList<>();

        if (positionTemplate != null && positionTemplate.getTemplateId() != null) {
            QueryWrapper<TemplateDimension> tdQuery = new QueryWrapper<>();
            tdQuery.eq("template_id", positionTemplate.getTemplateId());
            tdQuery.orderByAsc("sort_order");
            List<TemplateDimension> templateDimensions = templateDimensionMapper.selectList(tdQuery);

            if (!templateDimensions.isEmpty()) {
                for (TemplateDimension dim : templateDimensions) {
                    DimensionWithTagsVO vo = new DimensionWithTagsVO();
                    vo.setDimensionId(dim.getDimensionId());
                    vo.setDimensionCode(dim.getDimensionCode());
                    vo.setDimensionName(dim.getDimensionName());
                    vo.setDimensionDesc(dim.getDimensionDesc());
                    vo.setMaxScore(dim.getMaxScore());
                    vo.setMinScore(dim.getMinScore());
                    vo.setWeightPercent(dim.getWeightPercent());
                    vo.setSortOrder(dim.getSortOrder());
                    vo.setTags(getTagsByDimensionId(dim.getDimensionId()));
                    result.add(vo);
                }
                return result;
            }
        }

        List<EvaluationDimension> defaultDimensions = evaluationDimensionMapper.selectList(
                new QueryWrapper<EvaluationDimension>().eq("is_default", 1).orderByAsc("sort_order")
        );
        for (EvaluationDimension dim : defaultDimensions) {
            DimensionWithTagsVO vo = new DimensionWithTagsVO();
            vo.setDimensionId(dim.getId());
            vo.setDimensionCode(dim.getDimensionCode());
            vo.setDimensionName(dim.getDimensionName());
            vo.setDimensionDesc(dim.getDimensionDesc());
            vo.setMaxScore(dim.getMaxScore());
            vo.setMinScore(dim.getMinScore());
            vo.setWeightPercent(0);
            vo.setSortOrder(dim.getSortOrder());
            vo.setTags(getTagsByDimensionId(dim.getId()));
            result.add(vo);
        }
        return result;
    }
}
