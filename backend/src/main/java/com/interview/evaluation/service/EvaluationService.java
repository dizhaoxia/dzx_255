package com.interview.evaluation.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.interview.evaluation.dto.DimensionScoreDTO;
import com.interview.evaluation.dto.EvaluationSubmitDTO;
import com.interview.evaluation.entity.*;
import com.interview.evaluation.mapper.*;
import com.interview.evaluation.vo.DimensionScoreVO;
import com.interview.evaluation.vo.EvaluationDetailVO;
import com.interview.evaluation.vo.InterviewTaskVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EvaluationService {

    @Autowired
    private InterviewTaskMapper interviewTaskMapper;

    @Autowired
    private CandidateMapper candidateMapper;

    @Autowired
    private PositionMapper positionMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private EvaluationDimensionMapper evaluationDimensionMapper;

    @Autowired
    private EvaluationRecordMapper evaluationRecordMapper;

    @Autowired
    private EvaluationSummaryMapper evaluationSummaryMapper;

    @Autowired
    private PositionTemplateMapper positionTemplateMapper;

    @Autowired
    private TemplateDimensionMapper templateDimensionMapper;

    public List<InterviewTaskVO> getInterviewerTasks(Long interviewerId, String status) {
        QueryWrapper<InterviewTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("interviewer_id", interviewerId);
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq("status", status);
        }
        queryWrapper.orderByDesc("interview_time");

        List<InterviewTask> tasks = interviewTaskMapper.selectList(queryWrapper);
        return tasks.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    private InterviewTaskVO convertToVO(InterviewTask task) {
        InterviewTaskVO vo = new InterviewTaskVO();
        vo.setTaskId(task.getId());
        vo.setInterviewRound(task.getInterviewRound());
        vo.setInterviewType(task.getInterviewType());
        vo.setInterviewTime(task.getInterviewTime());
        vo.setStatus(task.getStatus());
        vo.setVideoUrl(task.getVideoUrl());

        Candidate candidate = candidateMapper.selectById(task.getCandidateId());
        if (candidate != null) {
            vo.setCandidateId(candidate.getId());
            vo.setCandidateName(candidate.getName());
            vo.setGender(candidate.getGender());
            vo.setAge(candidate.getAge());
            vo.setEducation(candidate.getEducation());
            vo.setWorkYears(candidate.getWorkYears());
            vo.setPositionId(candidate.getPositionId());
        }

        Position position = positionMapper.selectById(task.getPositionId());
        if (position != null) {
            vo.setPositionName(position.getPositionName());
            vo.setDepartment(position.getDepartment());
        }

        SysUser interviewer = sysUserMapper.selectById(task.getInterviewerId());
        if (interviewer != null) {
            vo.setInterviewerName(interviewer.getRealName());
        }

        return vo;
    }

    public EvaluationDetailVO getEvaluationDetail(Long taskId, Long interviewerId) {
        InterviewTask task = interviewTaskMapper.selectById(taskId);
        if (task == null || !task.getInterviewerId().equals(interviewerId)) {
            return null;
        }

        EvaluationDetailVO vo = new EvaluationDetailVO();
        vo.setTaskId(task.getId());
        vo.setCandidateId(task.getCandidateId());
        vo.setStatus(task.getStatus());
        vo.setInterviewType(task.getInterviewType());
        vo.setInterviewRound(task.getInterviewRound());
        vo.setVideoUrl(task.getVideoUrl());

        Candidate candidate = candidateMapper.selectById(task.getCandidateId());
        if (candidate != null) {
            vo.setCandidateName(candidate.getName());
        }

        Position position = positionMapper.selectById(task.getPositionId());
        if (position != null) {
            vo.setPositionName(position.getPositionName());
        }

        List<TemplateDimension> templateDimensions = getTemplateDimensionsByPositionId(task.getPositionId());

        List<DimensionScoreVO> dimensionScoreVOS = new ArrayList<>();
        for (TemplateDimension dim : templateDimensions) {
            DimensionScoreVO scoreVO = new DimensionScoreVO();
            scoreVO.setDimensionId(dim.getDimensionId());
            scoreVO.setDimensionCode(dim.getDimensionCode());
            scoreVO.setDimensionName(dim.getDimensionName());
            scoreVO.setMaxScore(dim.getMaxScore());
            scoreVO.setMinScore(dim.getMinScore());
            scoreVO.setWeightPercent(dim.getWeightPercent());

            QueryWrapper<EvaluationRecord> recordQuery = new QueryWrapper<>();
            recordQuery.eq("task_id", taskId);
            recordQuery.eq("dimension_id", dim.getDimensionId());
            EvaluationRecord record = evaluationRecordMapper.selectOne(recordQuery);

            if (record != null) {
                scoreVO.setScore(record.getScore());
                scoreVO.setComment(record.getComment());
                scoreVO.setSelectedTags(record.getSelectedTags());
                if (record.getScore() != null && dim.getWeightPercent() != null) {
                    double weighted = record.getScore() * dim.getWeightPercent() / 100.0;
                    scoreVO.setWeightedScore(weighted);
                }
            }

            dimensionScoreVOS.add(scoreVO);
        }
        vo.setDimensionScores(dimensionScoreVOS);

        BigDecimal weightedScore = calculateWeightedScore(taskId);
        vo.setWeightedScore(weightedScore.doubleValue());

        QueryWrapper<EvaluationSummary> summaryQuery = new QueryWrapper<>();
        summaryQuery.eq("task_id", taskId);
        EvaluationSummary summary = evaluationSummaryMapper.selectOne(summaryQuery);
        if (summary != null) {
            vo.setOverallComment(summary.getOverallComment());
            vo.setHireSuggestion(summary.getHireSuggestion());
            if (summary.getTotalScore() != null) {
                vo.setTotalScore(summary.getTotalScore().doubleValue());
            }
        }

        return vo;
    }

    private List<TemplateDimension> getTemplateDimensionsByPositionId(Long positionId) {
        QueryWrapper<PositionTemplate> ptQuery = new QueryWrapper<>();
        ptQuery.eq("position_id", positionId);
        PositionTemplate positionTemplate = positionTemplateMapper.selectOne(ptQuery);

        if (positionTemplate != null && positionTemplate.getTemplateId() != null) {
            QueryWrapper<TemplateDimension> tdQuery = new QueryWrapper<>();
            tdQuery.eq("template_id", positionTemplate.getTemplateId());
            tdQuery.orderByAsc("sort_order");
            List<TemplateDimension> templateDimensions = templateDimensionMapper.selectList(tdQuery);
            if (!templateDimensions.isEmpty()) {
                return templateDimensions;
            }
        }

        List<EvaluationDimension> defaultDimensions = evaluationDimensionMapper.selectList(
                new QueryWrapper<EvaluationDimension>().eq("is_default", 1).orderByAsc("sort_order")
        );
        List<TemplateDimension> fallback = new ArrayList<>();
        for (EvaluationDimension dim : defaultDimensions) {
            TemplateDimension td = new TemplateDimension();
            td.setDimensionId(dim.getId());
            td.setDimensionCode(dim.getDimensionCode());
            td.setDimensionName(dim.getDimensionName());
            td.setDimensionDesc(dim.getDimensionDesc());
            td.setMaxScore(dim.getMaxScore());
            td.setMinScore(dim.getMinScore());
            td.setSortOrder(dim.getSortOrder());
            td.setWeightPercent(0);
            fallback.add(td);
        }
        return fallback;
    }

    public List<TemplateDimension> getDimensionsByPositionId(Long positionId) {
        return getTemplateDimensionsByPositionId(positionId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveDraft(EvaluationSubmitDTO dto, Long interviewerId) {
        saveOrUpdateEvaluation(dto, interviewerId, false);
    }

    @Transactional(rollbackFor = Exception.class)
    public void submitEvaluation(EvaluationSubmitDTO dto, Long interviewerId) {
        saveOrUpdateEvaluation(dto, interviewerId, true);
    }

    private void saveOrUpdateEvaluation(EvaluationSubmitDTO dto, Long interviewerId, boolean isSubmit) {
        InterviewTask task = interviewTaskMapper.selectById(dto.getTaskId());
        if (task == null || !task.getInterviewerId().equals(interviewerId)) {
            throw new RuntimeException("任务不存在或无权限");
        }

        if ("SUBMITTED".equals(task.getStatus())) {
            throw new RuntimeException("已提交的评价不能再修改");
        }

        if (dto.getDimensionScores() != null) {
            for (DimensionScoreDTO dimScore : dto.getDimensionScores()) {
                QueryWrapper<EvaluationRecord> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("task_id", dto.getTaskId());
                queryWrapper.eq("dimension_id", dimScore.getDimensionId());
                EvaluationRecord existing = evaluationRecordMapper.selectOne(queryWrapper);

                if (existing != null) {
                    existing.setScore(dimScore.getScore());
                    existing.setComment(dimScore.getComment());
                    existing.setSelectedTags(dimScore.getSelectedTags());
                    evaluationRecordMapper.updateById(existing);
                } else {
                    EvaluationRecord record = new EvaluationRecord();
                    record.setTaskId(dto.getTaskId());
                    record.setCandidateId(task.getCandidateId());
                    record.setInterviewerId(interviewerId);
                    record.setDimensionId(dimScore.getDimensionId());
                    record.setDimensionCode(dimScore.getDimensionCode());
                    record.setScore(dimScore.getScore());
                    record.setComment(dimScore.getComment());
                    record.setSelectedTags(dimScore.getSelectedTags());
                    evaluationRecordMapper.insert(record);
                }
            }
        }

        BigDecimal totalScore = calculateTotalScore(dto.getTaskId());

        QueryWrapper<EvaluationSummary> summaryQuery = new QueryWrapper<>();
        summaryQuery.eq("task_id", dto.getTaskId());
        EvaluationSummary summary = evaluationSummaryMapper.selectOne(summaryQuery);

        if (summary != null) {
            summary.setOverallComment(dto.getOverallComment());
            summary.setHireSuggestion(dto.getHireSuggestion());
            summary.setTotalScore(totalScore);
            if (isSubmit) {
                summary.setStatus("SUBMITTED");
                summary.setSubmitTime(LocalDateTime.now());
            }
            evaluationSummaryMapper.updateById(summary);
        } else {
            summary = new EvaluationSummary();
            summary.setTaskId(dto.getTaskId());
            summary.setCandidateId(task.getCandidateId());
            summary.setInterviewerId(interviewerId);
            summary.setOverallComment(dto.getOverallComment());
            summary.setHireSuggestion(dto.getHireSuggestion());
            summary.setTotalScore(totalScore);
            summary.setStatus(isSubmit ? "SUBMITTED" : "DRAFT");
            if (isSubmit) {
                summary.setSubmitTime(LocalDateTime.now());
            }
            evaluationSummaryMapper.insert(summary);
        }

        if (isSubmit) {
            task.setStatus("SUBMITTED");
        } else {
            task.setStatus("DRAFT");
        }
        interviewTaskMapper.updateById(task);
    }

    private BigDecimal calculateTotalScore(Long taskId) {
        QueryWrapper<EvaluationRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("task_id", taskId);
        List<EvaluationRecord> records = evaluationRecordMapper.selectList(queryWrapper);

        if (records.isEmpty()) {
            return BigDecimal.ZERO;
        }

        int total = 0;
        int count = 0;
        for (EvaluationRecord record : records) {
            if (record.getScore() != null && record.getScore() > 0) {
                total += record.getScore();
                count++;
            }
        }

        if (count == 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(total).divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateWeightedScore(Long taskId) {
        InterviewTask task = interviewTaskMapper.selectById(taskId);
        if (task == null) {
            return BigDecimal.ZERO;
        }

        List<TemplateDimension> templateDimensions = getTemplateDimensionsByPositionId(task.getPositionId());

        QueryWrapper<EvaluationRecord> recordQuery = new QueryWrapper<>();
        recordQuery.eq("task_id", taskId);
        List<EvaluationRecord> records = evaluationRecordMapper.selectList(recordQuery);

        BigDecimal totalWeighted = BigDecimal.ZERO;
        int totalWeight = 0;

        for (TemplateDimension dim : templateDimensions) {
            Integer score = null;
            for (EvaluationRecord record : records) {
                if (record.getDimensionId().equals(dim.getDimensionId())) {
                    score = record.getScore();
                    break;
                }
            }

            if (score != null && dim.getWeightPercent() != null) {
                BigDecimal weighted = BigDecimal.valueOf(score)
                        .multiply(BigDecimal.valueOf(dim.getWeightPercent()))
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                totalWeighted = totalWeighted.add(weighted);
                totalWeight += dim.getWeightPercent();
            }
        }

        if (totalWeight == 0) {
            return BigDecimal.ZERO;
        }

        return totalWeighted;
    }
}
