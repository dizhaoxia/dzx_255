package com.interview.evaluation.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.interview.evaluation.dto.TemplateDimensionDTO;
import com.interview.evaluation.dto.TemplateSaveDTO;
import com.interview.evaluation.entity.EvaluationTemplate;
import com.interview.evaluation.entity.PositionTemplate;
import com.interview.evaluation.entity.TemplateDimension;
import com.interview.evaluation.mapper.EvaluationTemplateMapper;
import com.interview.evaluation.mapper.PositionTemplateMapper;
import com.interview.evaluation.mapper.TemplateDimensionMapper;
import com.interview.evaluation.vo.TemplateDetailVO;
import com.interview.evaluation.vo.TemplateDimensionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EvaluationTemplateService {

    @Autowired
    private EvaluationTemplateMapper evaluationTemplateMapper;

    @Autowired
    private TemplateDimensionMapper templateDimensionMapper;

    @Autowired
    private PositionTemplateMapper positionTemplateMapper;

    public List<EvaluationTemplate> listTemplates() {
        QueryWrapper<EvaluationTemplate> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        return evaluationTemplateMapper.selectList(queryWrapper);
    }

    public TemplateDetailVO getTemplateDetail(Long templateId) {
        EvaluationTemplate template = evaluationTemplateMapper.selectById(templateId);
        if (template == null) {
            return null;
        }

        TemplateDetailVO vo = new TemplateDetailVO();
        vo.setTemplateId(template.getId());
        vo.setTemplateName(template.getTemplateName());
        vo.setTemplateDesc(template.getTemplateDesc());
        vo.setIsDefault(template.getIsDefault());
        vo.setStatus(template.getStatus());
        vo.setCreateTime(template.getCreateTime());

        QueryWrapper<TemplateDimension> dimQuery = new QueryWrapper<>();
        dimQuery.eq("template_id", templateId);
        dimQuery.orderByAsc("sort_order");
        List<TemplateDimension> dimensions = templateDimensionMapper.selectList(dimQuery);

        List<TemplateDimensionVO> dimVOS = dimensions.stream().map(dim -> {
            TemplateDimensionVO dimVO = new TemplateDimensionVO();
            dimVO.setId(dim.getId());
            dimVO.setDimensionId(dim.getDimensionId());
            dimVO.setDimensionCode(dim.getDimensionCode());
            dimVO.setDimensionName(dim.getDimensionName());
            dimVO.setDimensionDesc(dim.getDimensionDesc());
            dimVO.setWeightPercent(dim.getWeightPercent());
            dimVO.setMaxScore(dim.getMaxScore());
            dimVO.setMinScore(dim.getMinScore());
            dimVO.setSortOrder(dim.getSortOrder());
            return dimVO;
        }).collect(Collectors.toList());

        vo.setDimensions(dimVOS);
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveTemplate(TemplateSaveDTO dto) {
        if (dto.getDimensions() == null || dto.getDimensions().isEmpty()) {
            throw new RuntimeException("模板维度不能为空");
        }

        int totalWeight = dto.getDimensions().stream()
                .mapToInt(TemplateDimensionDTO::getWeightPercent)
                .sum();
        if (totalWeight != 100) {
            throw new RuntimeException("维度权重之和必须等于100%，当前为" + totalWeight + "%");
        }

        EvaluationTemplate template;
        if (dto.getTemplateId() != null) {
            template = evaluationTemplateMapper.selectById(dto.getTemplateId());
            if (template == null) {
                throw new RuntimeException("模板不存在");
            }
            template.setTemplateName(dto.getTemplateName());
            template.setTemplateDesc(dto.getTemplateDesc());
            template.setIsDefault(dto.getIsDefault());
            evaluationTemplateMapper.updateById(template);

            QueryWrapper<TemplateDimension> deleteQuery = new QueryWrapper<>();
            deleteQuery.eq("template_id", dto.getTemplateId());
            templateDimensionMapper.delete(deleteQuery);
        } else {
            template = new EvaluationTemplate();
            template.setTemplateName(dto.getTemplateName());
            template.setTemplateDesc(dto.getTemplateDesc());
            template.setIsDefault(dto.getIsDefault());
            template.setStatus(1);
            evaluationTemplateMapper.insert(template);
        }

        for (TemplateDimensionDTO dimDTO : dto.getDimensions()) {
            TemplateDimension dim = new TemplateDimension();
            dim.setTemplateId(template.getId());
            dim.setDimensionId(dimDTO.getDimensionId());
            dim.setDimensionCode(dimDTO.getDimensionCode());
            dim.setDimensionName(dimDTO.getDimensionName());
            dim.setDimensionDesc(dimDTO.getDimensionDesc());
            dim.setWeightPercent(dimDTO.getWeightPercent());
            dim.setMaxScore(dimDTO.getMaxScore());
            dim.setMinScore(dimDTO.getMinScore());
            dim.setSortOrder(dimDTO.getSortOrder());
            templateDimensionMapper.insert(dim);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void toggleTemplateStatus(Long templateId, Integer status) {
        EvaluationTemplate template = evaluationTemplateMapper.selectById(templateId);
        if (template == null) {
            throw new RuntimeException("模板不存在");
        }
        template.setStatus(status);
        evaluationTemplateMapper.updateById(template);
    }

    @Transactional(rollbackFor = Exception.class)
    public void bindPositionTemplate(Long positionId, Long templateId) {
        QueryWrapper<PositionTemplate> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("position_id", positionId);
        PositionTemplate existing = positionTemplateMapper.selectOne(queryWrapper);

        if (existing != null) {
            existing.setTemplateId(templateId);
            positionTemplateMapper.updateById(existing);
        } else {
            PositionTemplate pt = new PositionTemplate();
            pt.setPositionId(positionId);
            pt.setTemplateId(templateId);
            positionTemplateMapper.insert(pt);
        }
    }

    public TemplateDetailVO getTemplateByPosition(Long positionId) {
        QueryWrapper<PositionTemplate> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("position_id", positionId);
        PositionTemplate pt = positionTemplateMapper.selectOne(queryWrapper);

        if (pt == null) {
            return null;
        }

        return getTemplateDetail(pt.getTemplateId());
    }
}
