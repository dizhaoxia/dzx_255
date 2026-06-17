package com.interview.evaluation.controller;

import com.interview.evaluation.common.Result;
import com.interview.evaluation.dto.TemplateSaveDTO;
import com.interview.evaluation.entity.EvaluationDimension;
import com.interview.evaluation.entity.EvaluationTemplate;
import com.interview.evaluation.entity.Position;
import com.interview.evaluation.service.EvaluationDimensionService;
import com.interview.evaluation.service.EvaluationTemplateService;
import com.interview.evaluation.service.HrService;
import com.interview.evaluation.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hr")
public class HrController {

    @Autowired
    private HrService hrService;

    @Autowired
    private EvaluationDimensionService evaluationDimensionService;

    @Autowired
    private EvaluationTemplateService evaluationTemplateService;

    @GetMapping("/positions")
    public Result<List<Position>> getPositionList() {
        List<Position> positions = hrService.getPositionList();
        return Result.success(positions);
    }

    @GetMapping("/candidates/progress")
    public Result<List<CandidateProgressVO>> getCandidateProgress(@RequestParam(required = false) Long positionId) {
        List<CandidateProgressVO> list = hrService.getCandidateProgress(positionId);
        return Result.success(list);
    }

    @GetMapping("/candidate/{candidateId}/evaluation")
    public Result<EvaluationSummaryVO> getCandidateEvaluation(@PathVariable Long candidateId) {
        EvaluationSummaryVO summary = hrService.getCandidateEvaluationSummary(candidateId);
        if (summary == null) {
            return Result.error("候选人不存在");
        }
        return Result.success(summary);
    }

    @GetMapping("/evaluation-template")
    public Result<List<EvaluationDimension>> getEvaluationTemplate() {
        List<EvaluationDimension> dimensions = evaluationDimensionService.getDefaultDimensions();
        return Result.success(dimensions);
    }

    @GetMapping("/candidate/{candidateId}/export-csv")
    public void exportCsv(@PathVariable Long candidateId, HttpServletResponse response) throws IOException {
        String csvContent = hrService.generateCsvContent(candidateId);

        String fileName = "候选人评价报告_" + candidateId + ".csv";
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));

        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        OutputStream out = response.getOutputStream();
        out.write(bom);
        out.write(csvContent.getBytes(StandardCharsets.UTF_8));
        out.flush();
        out.close();
    }

    @GetMapping("/templates")
    public Result<List<EvaluationTemplate>> listTemplates() {
        List<EvaluationTemplate> templates = evaluationTemplateService.listTemplates();
        return Result.success(templates);
    }

    @GetMapping("/template/{templateId}")
    public Result<TemplateDetailVO> getTemplateDetail(@PathVariable Long templateId) {
        TemplateDetailVO detail = evaluationTemplateService.getTemplateDetail(templateId);
        if (detail == null) {
            return Result.error("模板不存在");
        }
        return Result.success(detail);
    }

    @PostMapping("/template")
    public Result<Void> saveTemplate(@RequestBody TemplateSaveDTO dto) {
        try {
            evaluationTemplateService.saveTemplate(dto);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/template/{templateId}/status")
    public Result<Void> toggleTemplateStatus(@PathVariable Long templateId, @RequestParam Integer status) {
        try {
            evaluationTemplateService.toggleTemplateStatus(templateId, status);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/template/bind")
    public Result<Void> bindPositionTemplate(@RequestParam Long positionId, @RequestParam Long templateId) {
        try {
            evaluationTemplateService.bindPositionTemplate(positionId, templateId);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/template/by-position")
    public Result<TemplateDetailVO> getTemplateByPosition(@RequestParam Long positionId) {
        TemplateDetailVO detail = evaluationTemplateService.getTemplateByPosition(positionId);
        return Result.success(detail);
    }

    @GetMapping("/candidate/{candidateId}/discrepancy")
    public Result<ScoreDiscrepancyVO> getCandidateDiscrepancy(@PathVariable Long candidateId) {
        ScoreDiscrepancyVO vo = hrService.detectScoreDiscrepancy(candidateId);
        return Result.success(vo);
    }

    @PostMapping("/candidates/compare")
    public Result<List<CandidateCompareVO>> getCandidateCompare(@RequestBody Map<String, List<Long>> request) {
        List<Long> candidateIds = request.get("candidateIds");
        try {
            List<CandidateCompareVO> list = hrService.getCandidateCompare(candidateIds);
            return Result.success(list);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/funnel-stats")
    public Result<List<FunnelStatVO>> getFunnelStats(@RequestParam(required = false) Long positionId) {
        List<FunnelStatVO> list = hrService.getFunnelStats(positionId);
        return Result.success(list);
    }
}
