package com.interview.evaluation.controller;

import com.interview.evaluation.common.Result;
import com.interview.evaluation.entity.EvaluationDimension;
import com.interview.evaluation.entity.Position;
import com.interview.evaluation.service.EvaluationDimensionService;
import com.interview.evaluation.service.HrService;
import com.interview.evaluation.vo.CandidateProgressVO;
import com.interview.evaluation.vo.EvaluationSummaryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/hr")
public class HrController {

    @Autowired
    private HrService hrService;

    @Autowired
    private EvaluationDimensionService evaluationDimensionService;

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
}
