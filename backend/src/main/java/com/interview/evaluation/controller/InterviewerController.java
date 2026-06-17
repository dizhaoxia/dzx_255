package com.interview.evaluation.controller;

import com.interview.evaluation.common.Result;
import com.interview.evaluation.dto.EvaluationSubmitDTO;
import com.interview.evaluation.entity.EvaluationDimension;
import com.interview.evaluation.entity.SysUser;
import com.interview.evaluation.service.EvaluationDimensionService;
import com.interview.evaluation.service.EvaluationService;
import com.interview.evaluation.service.SysUserService;
import com.interview.evaluation.vo.EvaluationDetailVO;
import com.interview.evaluation.vo.InterviewTaskVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/interviewer")
public class InterviewerController {

    @Autowired
    private EvaluationService evaluationService;

    @Autowired
    private EvaluationDimensionService evaluationDimensionService;

    @Autowired
    private SysUserService sysUserService;

    @GetMapping("/tasks")
    public Result<List<InterviewTaskVO>> getTaskList(@RequestParam(required = false) String status) {
        Long interviewerId = getCurrentUserId();
        List<InterviewTaskVO> tasks = evaluationService.getInterviewerTasks(interviewerId, status);
        return Result.success(tasks);
    }

    @GetMapping("/evaluation/{taskId}")
    public Result<EvaluationDetailVO> getEvaluationDetail(@PathVariable Long taskId) {
        Long interviewerId = getCurrentUserId();
        EvaluationDetailVO detail = evaluationService.getEvaluationDetail(taskId, interviewerId);
        if (detail == null) {
            return Result.error("评价不存在或无权限查看");
        }
        return Result.success(detail);
    }

    @PostMapping("/evaluation/draft")
    public Result<String> saveDraft(@RequestBody EvaluationSubmitDTO dto) {
        Long interviewerId = getCurrentUserId();
        evaluationService.saveDraft(dto, interviewerId);
        return Result.success("草稿保存成功");
    }

    @PostMapping("/evaluation/submit")
    public Result<String> submitEvaluation(@RequestBody EvaluationSubmitDTO dto) {
        Long interviewerId = getCurrentUserId();
        evaluationService.submitEvaluation(dto, interviewerId);
        return Result.success("评价提交成功");
    }

    @GetMapping("/dimensions")
    public Result<List<EvaluationDimension>> getDimensions() {
        List<EvaluationDimension> dimensions = evaluationDimensionService.getDefaultDimensions();
        return Result.success(dimensions);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        SysUser user = sysUserService.getByUsername(username);
        return user != null ? user.getId() : null;
    }
}
