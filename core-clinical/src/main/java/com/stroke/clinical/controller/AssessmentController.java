package com.stroke.clinical.controller;

import com.stroke.clinical.dto.assessment.NihssAssessmentVO;
import com.stroke.clinical.dto.assessment.NihssItemVO;
import com.stroke.clinical.dto.assessment.SubmitNihssRequest;
import com.stroke.clinical.service.AssessmentService;
import com.stroke.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 量表评估 API — 对应设计文档 5.1 节
 * <p>
 * NIHSS、mRS、ASPECTS、GCS 等量表的统一入口。
 */
@Tag(name = "量表评估管理")
@RestController
@RequestMapping("/assessments")
public class AssessmentController {

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @Operation(summary = "提交 NIHSS 评分",
            description = "提交 NIHSS 评分（15项），支持离线标记。"
                    + "每次提交生成新版本，不覆盖历史记录。")
    @PostMapping("/nihss")
    public Result<NihssAssessmentVO> submitNihss(
            @Valid @RequestBody SubmitNihssRequest request) {
        NihssAssessmentVO result = assessmentService.submitNihss(request);
        return Result.success("NIHSS评分提交成功", result);
    }

    @Operation(summary = "获取 NIHSS 评分历史",
            description = "按版本倒序返回所有 NIHSS 评分记录")
    @GetMapping("/{encounterId}/nihss")
    public Result<List<NihssAssessmentVO>> getNihssHistory(
            @Parameter(description = "就诊ID") @PathVariable Long encounterId) {
        List<NihssAssessmentVO> history = assessmentService.getNihssHistory(encounterId);
        return Result.success(history);
    }

    @Operation(summary = "获取最新 NIHSS 评分",
            description = "返回当前 active 的最新 NIHSS 评分")
    @GetMapping("/{encounterId}/nihss/latest")
    public Result<NihssAssessmentVO> getLatestNihss(
            @Parameter(description = "就诊ID") @PathVariable Long encounterId) {
        NihssAssessmentVO latest = assessmentService.getLatestNihss(encounterId);
        return Result.success(latest);
    }

    @Operation(summary = "查询 NIHSS 量表配置",
            description = "返回 NIHSS 15项评分标准（条目编码、名称、分值范围、描述）")
    @GetMapping("/nihss/items")
    public Result<List<NihssItemVO>> getScaleItems() {
        List<NihssItemVO> items = assessmentService.getScaleItems();
        return Result.success(items);
    }
}
