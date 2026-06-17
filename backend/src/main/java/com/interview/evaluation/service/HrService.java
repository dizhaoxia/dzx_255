package com.interview.evaluation.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.interview.evaluation.entity.*;
import com.interview.evaluation.mapper.*;
import com.interview.evaluation.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HrService {

    @Autowired
    private PositionMapper positionMapper;

    @Autowired
    private CandidateMapper candidateMapper;

    @Autowired
    private InterviewTaskMapper interviewTaskMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private EvaluationDimensionMapper evaluationDimensionMapper;

    @Autowired
    private EvaluationRecordMapper evaluationRecordMapper;

    @Autowired
    private EvaluationSummaryMapper evaluationSummaryMapper;

    public List<Position> getPositionList() {
        QueryWrapper<Position> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);
        queryWrapper.orderByDesc("create_time");
        return positionMapper.selectList(queryWrapper);
    }

    public List<CandidateProgressVO> getCandidateProgress(Long positionId) {
        QueryWrapper<Candidate> candidateQuery = new QueryWrapper<>();
        if (positionId != null) {
            candidateQuery.eq("position_id", positionId);
        }
        candidateQuery.orderByDesc("create_time");
        List<Candidate> candidates = candidateMapper.selectList(candidateQuery);

        List<CandidateProgressVO> result = new ArrayList<>();
        for (Candidate candidate : candidates) {
            CandidateProgressVO vo = new CandidateProgressVO();
            vo.setCandidateId(candidate.getId());
            vo.setCandidateName(candidate.getName());
            vo.setGender(candidate.getGender());
            vo.setAge(candidate.getAge());
            vo.setEducation(candidate.getEducation());
            vo.setWorkYears(candidate.getWorkYears());
            vo.setPhone(candidate.getPhone());
            vo.setEmail(candidate.getEmail());
            vo.setPositionId(candidate.getPositionId());

            Position position = positionMapper.selectById(candidate.getPositionId());
            if (position != null) {
                vo.setPositionName(position.getPositionName());
            }

            QueryWrapper<InterviewTask> taskQuery = new QueryWrapper<>();
            taskQuery.eq("candidate_id", candidate.getId());
            taskQuery.orderByAsc("interview_round");
            List<InterviewTask> tasks = interviewTaskMapper.selectList(taskQuery);

            List<InterviewTaskVO> taskVOS = new ArrayList<>();
            int submittedCount = 0;
            int draftCount = 0;
            int pendingCount = 0;

            for (InterviewTask task : tasks) {
                InterviewTaskVO taskVO = new InterviewTaskVO();
                taskVO.setTaskId(task.getId());
                taskVO.setInterviewRound(task.getInterviewRound());
                taskVO.setInterviewType(task.getInterviewType());
                taskVO.setInterviewTime(task.getInterviewTime());
                taskVO.setStatus(task.getStatus());

                SysUser interviewer = sysUserMapper.selectById(task.getInterviewerId());
                if (interviewer != null) {
                    taskVO.setInterviewerName(interviewer.getRealName());
                }

                taskVOS.add(taskVO);

                if ("SUBMITTED".equals(task.getStatus())) {
                    submittedCount++;
                } else if ("DRAFT".equals(task.getStatus())) {
                    draftCount++;
                } else {
                    pendingCount++;
                }
            }

            vo.setTotalTasks(tasks.size());
            vo.setSubmittedCount(submittedCount);
            vo.setDraftCount(draftCount);
            vo.setPendingCount(pendingCount);
            vo.setTasks(taskVOS);

            if (submittedCount == tasks.size() && tasks.size() > 0) {
                vo.setOverallStatus("ALL_COMPLETED");
            } else if (submittedCount > 0 || draftCount > 0) {
                vo.setOverallStatus("PARTIAL");
            } else {
                vo.setOverallStatus("PENDING");
            }

            result.add(vo);
        }

        return result;
    }

    public EvaluationSummaryVO getCandidateEvaluationSummary(Long candidateId) {
        Candidate candidate = candidateMapper.selectById(candidateId);
        if (candidate == null) {
            return null;
        }

        EvaluationSummaryVO result = new EvaluationSummaryVO();
        result.setCandidateId(candidateId);
        result.setCandidateName(candidate.getName());

        Position position = positionMapper.selectById(candidate.getPositionId());
        if (position != null) {
            result.setPositionName(position.getPositionName());
        }

        QueryWrapper<InterviewTask> taskQuery = new QueryWrapper<>();
        taskQuery.eq("candidate_id", candidateId);
        taskQuery.eq("status", "SUBMITTED");
        taskQuery.orderByAsc("interview_round");
        List<InterviewTask> tasks = interviewTaskMapper.selectList(taskQuery);

        List<InterviewerEvaluationVO> evaluationVOS = new ArrayList<>();
        Map<String, List<Integer>> dimensionScoresMap = new LinkedHashMap<>();

        List<EvaluationDimension> dimensions = evaluationDimensionMapper.selectList(
                new QueryWrapper<EvaluationDimension>().eq("is_default", 1).orderByAsc("sort_order")
        );
        for (EvaluationDimension dim : dimensions) {
            dimensionScoresMap.put(dim.getDimensionCode(), new ArrayList<>());
        }

        for (InterviewTask task : tasks) {
            InterviewerEvaluationVO evalVO = new InterviewerEvaluationVO();
            evalVO.setTaskId(task.getId());
            evalVO.setInterviewerId(task.getInterviewerId());
            evalVO.setInterviewRound(task.getInterviewRound());
            evalVO.setInterviewType(task.getInterviewType());

            SysUser interviewer = sysUserMapper.selectById(task.getInterviewerId());
            if (interviewer != null) {
                evalVO.setInterviewerName(interviewer.getRealName());
            }

            QueryWrapper<EvaluationSummary> summaryQuery = new QueryWrapper<>();
            summaryQuery.eq("task_id", task.getId());
            EvaluationSummary summary = evaluationSummaryMapper.selectOne(summaryQuery);
            if (summary != null) {
                evalVO.setOverallComment(summary.getOverallComment());
                evalVO.setHireSuggestion(summary.getHireSuggestion());
                evalVO.setSubmitTime(summary.getSubmitTime());
                if (summary.getTotalScore() != null) {
                    evalVO.setTotalScore(summary.getTotalScore().doubleValue());
                }
            }

            List<DimensionScoreVO> scoreVOS = new ArrayList<>();
            QueryWrapper<EvaluationRecord> recordQuery = new QueryWrapper<>();
            recordQuery.eq("task_id", task.getId());
            List<EvaluationRecord> records = evaluationRecordMapper.selectList(recordQuery);

            for (EvaluationDimension dim : dimensions) {
                DimensionScoreVO scoreVO = new DimensionScoreVO();
                scoreVO.setDimensionId(dim.getId());
                scoreVO.setDimensionCode(dim.getDimensionCode());
                scoreVO.setDimensionName(dim.getDimensionName());
                scoreVO.setMaxScore(dim.getMaxScore());
                scoreVO.setMinScore(dim.getMinScore());

                for (EvaluationRecord record : records) {
                    if (record.getDimensionId().equals(dim.getId())) {
                        scoreVO.setScore(record.getScore());
                        scoreVO.setComment(record.getComment());
                        if (record.getScore() != null && record.getScore() > 0) {
                            dimensionScoresMap.get(dim.getDimensionCode()).add(record.getScore());
                        }
                        break;
                    }
                }
                scoreVOS.add(scoreVO);
            }
            evalVO.setDimensionScores(scoreVOS);

            evaluationVOS.add(evalVO);
        }

        result.setInterviewerEvaluations(evaluationVOS);
        result.setEvaluationCount(evaluationVOS.size());

        Map<String, Double> dimensionAverages = new LinkedHashMap<>();
        double totalAll = 0;
        int dimCount = 0;

        for (Map.Entry<String, List<Integer>> entry : dimensionScoresMap.entrySet()) {
            List<Integer> scores = entry.getValue();
            if (!scores.isEmpty()) {
                double avg = scores.stream().mapToInt(Integer::intValue).average().getAsDouble();
                BigDecimal bd = BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP);
                dimensionAverages.put(entry.getKey(), bd.doubleValue());
                totalAll += bd.doubleValue();
                dimCount++;
            } else {
                dimensionAverages.put(entry.getKey(), 0.0);
            }
        }

        result.setDimensionAverages(dimensionAverages);

        if (dimCount > 0) {
            BigDecimal totalAvg = BigDecimal.valueOf(totalAll / dimCount).setScale(2, RoundingMode.HALF_UP);
            result.setTotalAverage(totalAvg.doubleValue());
        } else {
            result.setTotalAverage(0.0);
        }

        return result;
    }

    public String generateCsvContent(Long candidateId) {
        EvaluationSummaryVO summary = getCandidateEvaluationSummary(candidateId);
        if (summary == null) {
            return "";
        }

        StringBuilder csv = new StringBuilder();

        csv.append("候选人评价报告\n");
        csv.append("候选人,").append(summary.getCandidateName()).append("\n");
        csv.append("应聘岗位,").append(summary.getPositionName()).append("\n");
        csv.append("评价人数,").append(summary.getEvaluationCount()).append("\n");
        csv.append("总平均分,").append(summary.getTotalAverage()).append("\n\n");

        List<EvaluationDimension> dimensions = evaluationDimensionMapper.selectList(
                new QueryWrapper<EvaluationDimension>().eq("is_default", 1).orderByAsc("sort_order")
        );

        csv.append("各维度平均分\n");
        csv.append("维度,平均分\n");
        Map<String, Double> dimAverages = summary.getDimensionAverages();
        for (EvaluationDimension dim : dimensions) {
            Double avg = dimAverages.get(dim.getDimensionCode());
            csv.append(dim.getDimensionName()).append(",")
               .append(avg != null ? avg : 0).append("\n");
        }
        csv.append("\n");

        csv.append("各面试官评价明细\n");
        csv.append("面试官,面试轮次,面试类型");
        for (EvaluationDimension dim : dimensions) {
            csv.append(",").append(dim.getDimensionName());
        }
        csv.append(",总平均分,录用建议,综合评价\n");

        for (InterviewerEvaluationVO eval : summary.getInterviewerEvaluations()) {
            csv.append(eval.getInterviewerName()).append(",")
               .append(eval.getInterviewRound()).append(",")
               .append(eval.getInterviewType() != null ? eval.getInterviewType() : "");

            for (EvaluationDimension dim : dimensions) {
                Integer score = null;
                for (DimensionScoreVO dimScore : eval.getDimensionScores()) {
                    if (dimScore.getDimensionCode().equals(dim.getDimensionCode())) {
                        score = dimScore.getScore();
                        break;
                    }
                }
                csv.append(",").append(score != null ? score : "");
            }

            csv.append(",").append(eval.getTotalScore() != null ? eval.getTotalScore() : "")
               .append(",").append(eval.getHireSuggestion() != null ? eval.getHireSuggestion() : "")
               .append(",").append(eval.getOverallComment() != null ? eval.getOverallComment().replace(",", "，").replace("\n", " ") : "")
               .append("\n");
        }

        csv.append("\n各面试官评语详情\n");
        for (InterviewerEvaluationVO eval : summary.getInterviewerEvaluations()) {
            csv.append("【").append(eval.getInterviewerName()).append("】\n");
            for (DimensionScoreVO dimScore : eval.getDimensionScores()) {
                csv.append(dimScore.getDimensionName()).append("评语: ")
                   .append(dimScore.getComment() != null ? dimScore.getComment() : "").append("\n");
            }
            csv.append("综合评价: ").append(eval.getOverallComment() != null ? eval.getOverallComment() : "").append("\n\n");
        }

        return csv.toString();
    }
}
